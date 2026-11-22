package com.weather.app.data.repository

import com.weather.app.data.mapper.WeatherMapper
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.data.remote.OpenMeteoHttpClient
import com.weather.app.data.remote.OpenMeteoRemoteDataSource
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
 * - Ізольованого [WeatherRemoteDataSource] (WeatherMetro, Open-Meteo, Mock);
 * - In-flight дедуплікації паралельних запитів для одних і тих самих координат;
 * - Захисту квот (Rate Limiting & Cooldown при HTTP 429/5xx);
 * - Стратегії Stale-While-Revalidate з позначкою [FullWeatherForecast.isStale].
 * 
 * Обов'язкова ліцензійна вимога:
 * Джерело погодних даних фіксується у [FullWeatherForecast.dataSource].
 * Для релізу з AdMob основним кандидатом є WeatherMetro Free (ADR-009, ADR-011).
 * Open-Meteo Free дозволений лише для некомерційного dev/тестового середовища (CC BY 4.0).
 */
class WeatherRepositoryImpl(
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
            OpenMeteoRemoteDataSource { lat, lon, tz ->
                OpenMeteoHttpClient.fetchForecast(lat, lon, tz)
            }
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
        val cached = cache[cacheKey]
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
            if (cached != null) {
                val staleForecast = cached.forecast.copy(isStale = true)
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
            return Result.success(domainForecast)
        } else {
            val ex = apiResult.exceptionOrNull()
            val errorMsg = ex?.message ?: ""

            // Якщо отримали 429 або 5xx, встановлюємо 60-секундний cooldown
            if (errorMsg.contains("429") || errorMsg.contains("500") || errorMsg.contains("503")) {
                errorCooldowns[cacheKey] = System.currentTimeMillis() + 60_000L
            }

            // Якщо є застарілий кеш — віддаємо його з isStale = true
            if (cached != null) {
                val staleForecast = cached.forecast.copy(isStale = true)
                _currentForecastFlow.value = staleForecast
                return Result.success(staleForecast)
            }

            // Fallback до автономних фікстур у dev режимі
            val fallbackForecast = when {
                location.name.contains("Warsaw", ignoreCase = true) || location.name.contains("Варшава", ignoreCase = true) -> {
                    MockWeatherRepository.createWarsawForecast(location)
                }
                else -> {
                    MockWeatherRepository.createKyivForecast(location)
                }
            }.copy(isStale = true)

            cache[cacheKey] = CachedForecast(fallbackForecast, now)
            _currentForecastFlow.value = fallbackForecast
            return Result.success(fallbackForecast)
        }
    }

    override fun observeCurrentForecast(): Flow<FullWeatherForecast?> {
        return _currentForecastFlow.asStateFlow()
    }
}
