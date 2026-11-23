package com.weather.app.data.repository

import android.content.Context
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.remote.OpenMeteoRemoteDataSource
import com.weather.app.data.remote.WeatherMetroRemoteDataSource
import com.weather.app.data.remote.WeatherRemoteDataSource
import com.weather.app.data.remote.dto.OpenMeteoForecastResponse
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.WeatherRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Базова реалізація [WeatherRepository] із підтримкою:
 * - Ізольованого [WeatherRemoteDataSource] (WeatherMetro Free як основний комерційний постачальник);
 * - In-flight дедуплікації паралельних запитів для одних і тих самих координат;
 * - Захисту квот (Rate Limiting & Cooldown при HTTP 429/5xx);
 * - Стратегії Stale-While-Revalidate з позначкою [FullWeatherForecast.isStale];
 * - Дискового кешування мультиміст через [WeatherLocalCache];
 * - Повної відсутності вигаданих або тестових Mock-даних у виробничому контурі.
 * 
 * Обов'язкова ліцензійна вимога:
 * Джерело погодних даних фіксується у [FullWeatherForecast.dataSource].
 * Для комерційного релізу з AdMob основним провайдером є WeatherMetro Free (ADR-009, ADR-011, ADR-014).
 * Open-Meteo Free суворо заборонено як fallback у комерційному контурі.
 * Якщо сервіс недоступний і немає кешу — повертається чесна помилка (Result.failure).
 */
class WeatherRepositoryImpl(
    private val context: Context? = null,
    private val remoteDataSource: WeatherRemoteDataSource? = null,
    private val fetchForecastApi: (suspend (latitude: Double, longitude: Double, timezone: String) -> Result<OpenMeteoForecastResponse>)? = null,
    private val cacheTtlMinutes: Long = 30L
) : WeatherRepository {

    companion object {
        @Volatile
        var lastKnownForecast: FullWeatherForecast? = null
    }

    private val effectiveDataSource: WeatherRemoteDataSource by lazy {
        remoteDataSource ?: if (fetchForecastApi != null) {
            OpenMeteoRemoteDataSource(fetchForecastApi)
        } else {
            WeatherMetroRemoteDataSource()
        }
    }

    private val cache = ConcurrentHashMap<String, CachedForecast>()
    private val _currentForecastFlow = MutableStateFlow<FullWeatherForecast?>(null)

    // Захист квот: блокування повторних запитів після 429/5xx
    private val errorCooldowns = ConcurrentHashMap<String, Long>()
    private val inFlightMutex = Mutex()
    private val inFlightMap = ConcurrentHashMap<String, Deferred<Result<FullWeatherForecast>>>()

    private data class CachedForecast(
        val forecast: FullWeatherForecast,
        val timestamp: LocalDateTime
    )

    override suspend fun getFullForecast(
        location: LocationModel,
        forceRefresh: Boolean
    ): Result<FullWeatherForecast> {
        val cacheKey = "${location.latitude},${location.longitude}"
        val cached = cache[cacheKey] ?: loadFromDiskCache(location)
        val now = LocalDateTime.now()

        // 1. Перевіряємо валідність кешу в пам'яті
        if (!forceRefresh && cached != null) {
            val isExpired = cached.timestamp.plusMinutes(cacheTtlMinutes).isBefore(now)
            if (!isExpired) {
                _currentForecastFlow.value = cached.forecast
                return Result.success(cached.forecast)
            }
        }

        // 2. Захист квот: перевірка чи не перебуває ендпоінт у періоді cooldown після 429/5xx
        val cooldownUntil = errorCooldowns[cacheKey] ?: 0L
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis < cooldownUntil) {
            val fallbackCache = cached ?: loadFromDiskCache(location)
            if (fallbackCache != null) {
                val staleForecast = fallbackCache.forecast.copy(isStale = true)
                _currentForecastFlow.value = staleForecast
                return Result.success(staleForecast)
            }
            return Result.failure(Exception("Квоту запитів тимчасово перевищено (HTTP 429). Спробуйте пізніше."))
        }

        // 3. In-flight дедуплікація запитів: об'єднуємо одночасні виклики до одного мережевого запиту
        return coroutineScope {
            val deferred = inFlightMutex.withLock {
                inFlightMap.getOrPut(cacheKey) {
                    async {
                        executeNetworkFetch(location, cacheKey, cached, now)
                    }
                }
            }

            try {
                deferred.await()
            } finally {
                inFlightMutex.withLock {
                    inFlightMap.remove(cacheKey)
                }
            }
        }
    }

    private suspend fun executeNetworkFetch(
        location: LocationModel,
        cacheKey: String,
        cached: CachedForecast?,
        now: LocalDateTime
    ): Result<FullWeatherForecast> {
        val apiResult = effectiveDataSource.fetchForecast(location)

        if (apiResult.isSuccess) {
            val domainForecast = apiResult.getOrThrow()
            cache[cacheKey] = CachedForecast(domainForecast, now)
            lastKnownForecast = domainForecast
            errorCooldowns.remove(cacheKey) // очищаємо cooldown при успіху
            _currentForecastFlow.value = domainForecast

            // Зберігаємо у дисковий кеш для підтримки офлайн-режиму
            try {
                WeatherLocalCache.saveForecastForLocation(context, location, domainForecast)
            } catch (ignored: Throwable) {
                // безпечне ігнорування збоїв запису на диск
            }

            return Result.success(domainForecast)
        } else {
            val ex = apiResult.exceptionOrNull()
            val errorMsg = ex?.message ?: ""

            // Якщо отримали 429 або 5xx, встановлюємо 60-секундний cooldown
            if (errorMsg.contains("429") || errorMsg.contains("500") || errorMsg.contains("502") || errorMsg.contains("503")) {
                errorCooldowns[cacheKey] = System.currentTimeMillis() + 60_000L
            }

            // Якщо є застарілий кеш у пам'яті — віддаємо його з isStale = true
            if (cached != null) {
                val staleForecast = cached.forecast.copy(isStale = true)
                _currentForecastFlow.value = staleForecast
                return Result.success(staleForecast)
            }

            // Якщо є застарілий кеш на диску для ЦІЄЇ локації — віддаємо його з isStale = true
            val diskForecast = loadFromDiskCache(location)
            if (diskForecast != null) {
                val staleForecast = diskForecast.forecast.copy(isStale = true)
                _currentForecastFlow.value = staleForecast
                return Result.success(staleForecast)
            }

            // Сувора вимога якості: ЖОДНОГО вигаданого або тестового прогнозу у production!
            // Якщо сервер недоступний і кешу немає — повертаємо чесну помилку.
            val finalException = ex ?: Exception("Сервіс погоди тимчасово недоступний, закешовані дані відсутні")
            return Result.failure(finalException)
        }
    }

    private fun loadFromDiskCache(location: LocationModel): CachedForecast? {
        return try {
            // Завантажуємо кеш СУВОРО для вказаної локації. Не використовуємо кеш інших міст!
            val diskForecast = WeatherLocalCache.loadCachedForecastForLocation(context, location)
            if (diskForecast != null) {
                val cached = CachedForecast(diskForecast, diskForecast.updatedAt)
                val cacheKey = "${location.latitude},${location.longitude}"
                cache[cacheKey] = cached
                cached
            } else {
                null
            }
        } catch (ignored: Throwable) {
            null
        }
    }

    override fun observeCurrentForecast(): Flow<FullWeatherForecast?> {
        return _currentForecastFlow.asStateFlow()
    }
}
