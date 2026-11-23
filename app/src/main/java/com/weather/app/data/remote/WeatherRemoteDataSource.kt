package com.weather.app.data.remote

import com.weather.app.data.mapper.WeatherMapper
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherDataSource
import java.time.LocalDateTime

/**
 * Інтерфейс віддаленого джерела погодних даних.
 * Відповідно до ADR-012 та DATA_CONTRACTS.md забезпечує повну ізоляцію мережевих DTO
 * конкретних постачальників від бізнес-логіки застосунку.
 */
interface WeatherRemoteDataSource {
    val source: WeatherDataSource

    suspend fun fetchForecast(
        location: LocationModel
    ): Result<FullWeatherForecast>
}

/**
 * Реалізація джерела даних Open-Meteo для некомерційного dev/тестового середовища (CC BY 4.0).
 * Суворо заборонено використовувати як автоматичний резервний провайдер у комерційному релізі з AdMob.
 */
class OpenMeteoRemoteDataSource(
    private val fetchApi: suspend (latitude: Double, longitude: Double, timezone: String) -> Result<com.weather.app.data.remote.dto.OpenMeteoForecastResponse> = { lat, lon, tz ->
        OpenMeteoHttpClient.fetchForecast(lat, lon, tz)
    }
) : WeatherRemoteDataSource {

    override val source: WeatherDataSource = WeatherDataSource.OPEN_METEO

    override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
        val now = LocalDateTime.now()
        val apiResult = fetchApi(location.latitude, location.longitude, location.timezone)
        return apiResult.map { response ->
            WeatherMapper.toDomain(response, location, now)
        }
    }
}

/**
 * Реалізація джерела даних WeatherMetro Free (основний комерційний кандидат v1).
 * Дозволяє комерційне використання з обов'язковою атрибуцією ("Weather data by WeatherMetro").
 * 16 днів щоденного та 384 години погодинного прогнозу.
 */
class WeatherMetroRemoteDataSource(
    private val apiKeyProvider: () -> String = {
        try {
            com.weather.app.BuildConfig.WEATHERMETRO_API_KEY
        } catch (e: Throwable) {
            ""
        }
    },
    private val fetchMetroApi: (suspend (latitude: Double, longitude: Double, timezone: String, apiKey: String) -> Result<com.weather.app.data.remote.dto.WeatherMetroResponse>)? = null
) : WeatherRemoteDataSource {

    override val source: WeatherDataSource = WeatherDataSource.WEATHER_METRO

    override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
        val now = LocalDateTime.now()
        val key = apiKeyProvider()

        val apiResult = if (fetchMetroApi != null) {
            fetchMetroApi.invoke(location.latitude, location.longitude, location.timezone, key)
        } else {
            WeatherMetroHttpClient.fetchForecast(location.latitude, location.longitude, location.timezone, key)
        }

        return apiResult.map { response ->
            com.weather.app.data.mapper.WeatherMetroMapper.toDomain(response, location, now)
        }
    }
}
