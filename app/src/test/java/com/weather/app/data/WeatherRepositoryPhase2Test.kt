package com.weather.app.data

import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.remote.WeatherRemoteDataSource
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class WeatherRepositoryPhase2Test {

    private val testLocation = LocationModel(
        id = 100L,
        name = "Тестове Місто",
        country = "Україна",
        admin1 = "Область",
        latitude = 50.45,
        longitude = 30.52,
        timezone = "Europe/Kyiv"
    )

    @Before
    fun setUp() {
        WeatherLocalCache.clearAll()
    }

    @After
    fun tearDown() {
        WeatherLocalCache.clearAll()
    }

    private fun createDummyForecast(location: LocationModel, ds: WeatherDataSource = WeatherDataSource.WEATHER_METRO): FullWeatherForecast {
        val now = LocalDateTime.now()
        return FullWeatherForecast(
            location = location,
            current = CurrentWeather(
                temperatureCelsius = 20.0,
                apparentTemperatureCelsius = 19.5,
                condition = WeatherCondition.CLEAR_SKY,
                relativeHumidityPercent = 50,
                windSpeedKmh = 12.0,
                surfacePressureHpa = 1013.0,
                uvIndex = 3.0,
                time = now
            ),
            hourly24h = emptyList(),
            daily14Days = emptyList(),
            updatedAt = now,
            isStale = false,
            dataSource = ds
        )
    }

    @Test
    fun getFullForecast_successfulFetch_returnsForecastAndCachesIt() = runBlocking {
        var fetchCount = 0
        val fakeDataSource = object : WeatherRemoteDataSource {
            override val source: WeatherDataSource = WeatherDataSource.WEATHER_METRO
            override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
                fetchCount++
                return Result.success(createDummyForecast(location, source))
            }
        }

        val repository = WeatherRepositoryImpl(remoteDataSource = fakeDataSource)

        // Перший виклик
        val result1 = repository.getFullForecast(testLocation, forceRefresh = false)
        assertTrue(result1.isSuccess)
        assertEquals(1, fetchCount)
        assertEquals(WeatherDataSource.WEATHER_METRO, result1.getOrThrow().dataSource)
        assertFalse(result1.getOrThrow().isStale)

        // Другий виклик (без forceRefresh) — береться з кешу в пам'яті
        val result2 = repository.getFullForecast(testLocation, forceRefresh = false)
        assertTrue(result2.isSuccess)
        assertEquals(1, fetchCount) // Лічильник не збільшився
    }

    @Test
    fun getFullForecast_onHttp429QuotaError_returnsStaleCacheWithFlag() = runBlocking {
        var shouldFailWith429 = false
        val fakeDataSource = object : WeatherRemoteDataSource {
            override val source: WeatherDataSource = WeatherDataSource.WEATHER_METRO
            override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
                return if (shouldFailWith429) {
                    Result.failure(Exception("HTTP 429 Too Many Requests: Monthly quota exceeded"))
                } else {
                    Result.success(createDummyForecast(location, source))
                }
            }
        }

        val repository = WeatherRepositoryImpl(remoteDataSource = fakeDataSource)

        // 1. Успішно наповнюємо кеш
        val resultSuccess = repository.getFullForecast(testLocation)
        assertTrue(resultSuccess.isSuccess)
        assertFalse(resultSuccess.getOrThrow().isStale)

        // 2. Вмикаємо 429 помилку та примусовий рефреш
        shouldFailWith429 = true
        val resultAfter429 = repository.getFullForecast(testLocation, forceRefresh = true)

        // 3. Замість крашу репозиторій повертає застарілий кеш з позначкою isStale = true
        assertTrue(resultAfter429.isSuccess)
        assertTrue("Forecast must be marked as stale after 429", resultAfter429.getOrThrow().isStale)
    }

    @Test
    fun getFullForecast_inFlightDeduplication_joinsConcurrentRequests() = runBlocking {
        val networkCallCount = AtomicInteger(0)
        val slowDataSource = object : WeatherRemoteDataSource {
            override val source: WeatherDataSource = WeatherDataSource.WEATHER_METRO
            override suspend fun fetchForecast(location: LocationModel): Result<FullWeatherForecast> {
                networkCallCount.incrementAndGet()
                delay(100) // імітація мережевої затримки
                return Result.success(createDummyForecast(location, source))
            }
        }

        val repository = WeatherRepositoryImpl(remoteDataSource = slowDataSource)

        // Запускаємо 3 паралельні запити одночасно для однієї локації
        val deferred1 = async { repository.getFullForecast(testLocation, forceRefresh = true) }
        val deferred2 = async { repository.getFullForecast(testLocation, forceRefresh = true) }
        val deferred3 = async { repository.getFullForecast(testLocation, forceRefresh = true) }

        val r1 = deferred1.await()
        val r2 = deferred2.await()
        val r3 = deferred3.await()

        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertTrue(r3.isSuccess)

        // Дедуплікація повинна виконати рівно 1 мережевий виклик для паралельних запитів
        assertEquals("Parallel requests for same coordinates must join into 1 network call", 1, networkCallCount.get())
    }
}
