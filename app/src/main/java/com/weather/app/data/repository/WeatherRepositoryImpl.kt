package com.weather.app.data.repository

import com.weather.app.data.mapper.WeatherMapper
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.data.remote.dto.OpenMeteoForecastResponse
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Базова реалізація [WeatherRepository] для роботи з Open-Meteo API.
 * 
 * Обов'язкова ліцензійна вимога:
 * Джерело погодних даних: Open-Meteo (https://open-meteo.com/), ліцензія CC BY 4.0.
 * Згідно з ADR-005, безкоштовний ендпоінт дозволений для некомерційного використання та тестів.
 * Для релізу з рекламою потрібна комерційна підписка або комерційний постачальник.
 */
class WeatherRepositoryImpl(
    private val fetchForecastApi: (suspend (latitude: Double, longitude: Double, timezone: String) -> Result<OpenMeteoForecastResponse>)? = null,
    private val cacheTtlMinutes: Long = 30L
) : WeatherRepository {

    private val cache = ConcurrentHashMap<String, CachedForecast>()
    private val _currentForecastFlow = MutableStateFlow<FullWeatherForecast?>(null)

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

        // Перевіряємо кеш
        if (!forceRefresh && cached != null) {
            val isExpired = cached.timestamp.plusMinutes(cacheTtlMinutes).isBefore(now)
            if (!isExpired) {
                _currentForecastFlow.value = cached.forecast
                return Result.success(cached.forecast)
            }
        }

        // Якщо API клієнт переданий, робимо запит
        if (fetchForecastApi != null) {
            val apiResult = fetchForecastApi.invoke(location.latitude, location.longitude, location.timezone)
            if (apiResult.isSuccess) {
                val response = apiResult.getOrThrow()
                val domainForecast = WeatherMapper.toDomain(response, location, now)
                cache[cacheKey] = CachedForecast(domainForecast, now)
                _currentForecastFlow.value = domainForecast
                return Result.success(domainForecast)
            } else {
                // Якщо помилка мережі, але є застарілий кеш — повертаємо його з позначкою isStale
                if (cached != null) {
                    val staleForecast = cached.forecast.copy(isStale = true)
                    _currentForecastFlow.value = staleForecast
                    return Result.success(staleForecast)
                }
                return Result.failure(apiResult.exceptionOrNull() ?: Exception("Помилка завантаження погоди"))
            }
        }

        // Якщо клієнт не підключений (dev/offline fallback), повертаємо дані з мок-генератора
        val fallbackForecast = when {
            location.name.contains("Warsaw", ignoreCase = true) || location.name.contains("Варшава", ignoreCase = true) -> {
                MockWeatherRepository.createWarsawForecast(location)
            }
            else -> {
                MockWeatherRepository.createKyivForecast(location)
            }
        }
        cache[cacheKey] = CachedForecast(fallbackForecast, now)
        _currentForecastFlow.value = fallbackForecast
        return Result.success(fallbackForecast)
    }

    override fun observeCurrentForecast(): Flow<FullWeatherForecast?> {
        return _currentForecastFlow.asStateFlow()
    }
}
