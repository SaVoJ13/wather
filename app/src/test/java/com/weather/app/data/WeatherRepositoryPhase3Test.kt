package com.weather.app.data

import com.weather.app.data.remote.WeatherApiException
import com.weather.app.data.remote.WeatherMetroRemoteDataSource
import com.weather.app.data.remote.WeatherRemoteDataSource
import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Тести Phase 3 для [WeatherRepositoryImpl]:
 * - Підключення WeatherMetro Free як основного постачальника даних;
 * - Сувора заборона fallback на Open-Meteo;
 * - Обробка HTTP 429 з поверненням застарілого кешу (isStale = true).
 */
class WeatherRepositoryPhase3Test {

    private val testLocation = LocationModel(
        id = 100L,
        name = "Київ",
        country = "Україна",
        admin1 = "Київ",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv"
    )

    private fun createFakeMetroResponse(): WeatherMetroResponse {
        return WeatherMetroResponse(
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv",
            hourly = WeatherMetroHourlyDto(
                time = listOf("2026-11-23T12:00", "2026-11-23T13:00"),
                temperature2m = listOf(7.0, 8.0),
                precipitationProbability = listOf(10, 15),
                weathercode = listOf(1, 2),
                relativeHumidity2m = listOf(75, 70),
                windSpeed10m = listOf(12.0, 14.0)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf("2026-11-23", "2026-11-24"),
                temperature2mMax = listOf(9.0, 10.0),
                temperature2mMin = listOf(2.0, 3.0),
                precipitationProbabilityMax = listOf(20, 25),
                weathercode = listOf(1, 2),
                sunrise = listOf("2026-11-23T07:22"),
                sunset = listOf("2026-11-23T16:06"),
                windSpeed10mMax = listOf(18.0, 20.0)
            )
        )
    }

    @Test
    fun testDefaultDataSourceIsWeatherMetro() = runBlocking {
        // За замовчуванням WeatherRepositoryImpl використовує WeatherMetro
        val repository = WeatherRepositoryImpl()
        val result = repository.getFullForecast(testLocation, forceRefresh = false)
        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
    }

    @Test
    fun testWeatherMetroSuccessReturnsLiveForecast() = runBlocking {
        val fakeDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                Result.success(createFakeMetroResponse())
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = fakeDataSource)
        val result = repository.getFullForecast(testLocation, forceRefresh = true)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
        assertFalse(forecast.isStale)
    }

    @Test
    fun testWeatherMetroHttp429EntersCooldownAndReturnsStaleForecast() = runBlocking {
        var callCount = 0
        val fakeDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                callCount++
                if (callCount == 1) {
                    Result.success(createFakeMetroResponse())
                } else {
                    Result.failure(WeatherApiException(429, "Rate limit exceeded (Too Many Requests)"))
                }
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = fakeDataSource)

        // 1. Перший запит успішний — наповнює кеш
        val firstResult = repository.getFullForecast(testLocation, forceRefresh = true)
        assertTrue(firstResult.isSuccess)
        assertFalse(firstResult.getOrThrow().isStale)

        // 2. Другий запит повертає 429 — репозиторій повертає закешований прогноз із isStale = true
        val secondResult = repository.getFullForecast(testLocation, forceRefresh = true)
        assertTrue(secondResult.isSuccess)
        val staleForecast = secondResult.getOrThrow()
        assertTrue(staleForecast.isStale)
        assertEquals(WeatherDataSource.WEATHER_METRO, staleForecast.dataSource)
    }

    @Test
    fun testStrictProhibitionOfOpenMeteoFallbackInRelease() = runBlocking {
        // Коли WeatherMetro зазнає повної помилки, застосунок ніколи не перемикається на Open-Meteo
        val failingDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                Result.failure(WeatherApiException(500, "Internal Server Error"))
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = failingDataSource)
        val result = repository.getFullForecast(testLocation, forceRefresh = true)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()
        // Джерело даних суворо лишається WeatherMetro
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
        assertTrue(forecast.isStale)
    }
}
