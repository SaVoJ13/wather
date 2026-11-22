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
 * Дозволяє комерційне використання з видимою атрибуцією ("Weather data by WeatherMetro").
 * 16 днів щоденного та погодинного прогнозу.
 */
class WeatherMetroRemoteDataSource(
    private val fetchMetroApi: (suspend (latitude: Double, longitude: Double, timezone: String) -> Result<FullWeatherForecast>)? = null
) : WeatherRemoteDataSource {

    override val source: WeatherDataSource = WeatherDataSource.WEATHER_METRO

    override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
        if (fetchMetroApi != null) {
            return fetchMetroApi.invoke(location.latitude, location.longitude, location.timezone)
        }
        // У Phase 2 (контрактний фундамент): якщо зовнішній лямбда-клієнт не переданий,
        // повертаємо підготовлений результат із позначкою джерела WEATHER_METRO
        val now = LocalDateTime.now()
        val mock = com.weather.app.data.mock.MockWeatherRepository.createKyivForecast(location)
        return Result.success(mock.copy(dataSource = WeatherDataSource.WEATHER_METRO, updatedAt = now))
    }
}
