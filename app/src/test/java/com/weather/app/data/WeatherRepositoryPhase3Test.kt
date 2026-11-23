package com.weather.app.data

import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.remote.WeatherApiException
import com.weather.app.data.remote.WeatherMetroRemoteDataSource
import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherDataSource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Обов'язкові тести Phase 3 для [WeatherRepositoryImpl] згідно з вимогами Власника:
 * 1. HTTP 500 + немає кешу → Error (Result.failure).
 * 2. HTTP 429 + немає кешу → Error (Result.failure).
 * 3. HTTP 500 + є кеш → Stale (Result.success, isStale = true).
 * 4. HTTP 200 → тільки справжні дані (isStale = false, dataSource = WEATHER_METRO).
 * 5. Перезапуск процесу → прогноз читається з диска.
 * 6. Місто A → ніколи не отримує кеш міста B.
 * 7. У production немає непозначених або автоматичних Mock-даних.
 * 8. Сувора заборона fallback на Open-Meteo у релізі.
 */
class WeatherRepositoryPhase3Test {

    private val kyivLocation = LocationModel(
        id = 100L,
        name = "Київ",
        country = "Україна",
        admin1 = "Київ",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv"
    )

    private val warsawLocation = LocationModel(
        id = 200L,
        name = "Варшава",
        country = "Польща",
        admin1 = "Мазовецьке воєводство",
        latitude = 52.2297,
        longitude = 21.0122,
        timezone = "Europe/Warsaw"
    )

    @Before
    fun setUp() {
        WeatherLocalCache.clearAll()
    }

    @After
    fun tearDown() {
        WeatherLocalCache.clearAll()
    }

    private fun createFakeMetroResponse(lat: Double = 50.4501, lon: Double = 30.5234): WeatherMetroResponse {
        return WeatherMetroResponse(
            latitude = lat,
            longitude = lon,
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
    fun testHttp500WithoutCacheReturnsError() = runBlocking {
        // Вимога 1: HTTP 500 + немає кешу → Error (не повертає вигадані або мок-дані)
        val failingDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                Result.failure(WeatherApiException(500, "Internal Server Error"))
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = failingDataSource)
        val result = repository.getFullForecast(kyivLocation, forceRefresh = true)

        assertTrue("При помилці 500 без кешу має повертатися failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception?.message?.contains("500") == true || exception?.message?.contains("недоступний") == true)
    }

    @Test
    fun testHttp429WithoutCacheReturnsError() = runBlocking {
        // Вимога 2: HTTP 429 + немає кешу → Error
        val rateLimitDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                Result.failure(WeatherApiException(429, "Rate limit exceeded (Too Many Requests)"))
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = rateLimitDataSource)
        val result = repository.getFullForecast(kyivLocation, forceRefresh = true)

        assertTrue("При помилці 429 без кешу має повертатися failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception?.message?.contains("429") == true || exception?.message?.contains("недоступний") == true)
    }

    @Test
    fun testHttp500WithCacheReturnsStale() = runBlocking {
        // Вимога 3: HTTP 500 + є кеш → Stale
        var shouldFail = false
        val mockableDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { lat, lon, _, _ ->
                if (shouldFail) {
                    Result.failure(WeatherApiException(500, "Internal Server Error"))
                } else {
                    Result.success(createFakeMetroResponse(lat, lon))
                }
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = mockableDataSource)

        // 1. Успішний запит (наповнює кеш)
        val firstResult = repository.getFullForecast(kyivLocation, forceRefresh = true)
        assertTrue(firstResult.isSuccess)
        assertFalse(firstResult.getOrThrow().isStale)

        // 2. Тепер сервер повертає 500
        shouldFail = true
        val secondResult = repository.getFullForecast(kyivLocation, forceRefresh = true)

        assertTrue("При наявності кешу та помилці 500 повертається застарілий кеш", secondResult.isSuccess)
        val forecast = secondResult.getOrThrow()
        assertTrue(forecast.isStale)
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
    }

    @Test
    fun testHttp200ReturnsRealDataOnly() = runBlocking {
        // Вимога 4: HTTP 200 → тільки справжні дані
        val successDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { lat, lon, _, _ ->
                Result.success(createFakeMetroResponse(lat, lon))
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = successDataSource)
        val result = repository.getFullForecast(kyivLocation, forceRefresh = true)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()
        assertFalse(forecast.isStale)
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
        assertEquals("Київ", forecast.location.name)
    }

    @Test
    fun testProcessRestartReadsFromDiskCache() = runBlocking {
        // Вимога 5: Перезапуск процесу → прогноз читається з диска
        val successDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { lat, lon, _, _ ->
                Result.success(createFakeMetroResponse(lat, lon))
            }
        )

        // Перший екземпляр репозиторію зберігає дані на диск
        val repository1 = WeatherRepositoryImpl(remoteDataSource = successDataSource)
        val result1 = repository1.getFullForecast(kyivLocation, forceRefresh = true)
        assertTrue(result1.isSuccess)

        // Симулюємо перезапуск процесу: новий екземпляр репозиторію з порожньою пам'яттю
        // та повністю вимкненою мережею (HTTP 500)
        val deadNetworkDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { _, _, _, _ ->
                Result.failure(WeatherApiException(500, "Network completely unavailable"))
            }
        )

        val repository2 = WeatherRepositoryImpl(remoteDataSource = deadNetworkDataSource)
        val result2 = repository2.getFullForecast(kyivLocation, forceRefresh = false)

        assertTrue("Після перезапуску процесу прогноз успішно відновлено з дискового кешу", result2.isSuccess)
        val restoredForecast = result2.getOrThrow()
        assertEquals(WeatherDataSource.WEATHER_METRO, restoredForecast.dataSource)
        assertEquals("Київ", restoredForecast.location.name)
    }

    @Test
    fun testCityANeverGetsCityBCache() = runBlocking {
        // Вимога 6: Місто A → ніколи не отримує кеш міста B
        val successDataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { lat, lon, _, _ ->
                Result.success(createFakeMetroResponse(lat, lon))
            }
        )

        // Наповнюємо кеш виключно для міста Київ
        val repository = WeatherRepositoryImpl(remoteDataSource = successDataSource)
        val kyivResult = repository.getFullForecast(kyivLocation, forceRefresh = true)
        assertTrue(kyivResult.isSuccess)

        // Тепер вимикаємо мережу і запитуємо Варшаву (для якої кешу немає)
        val failingWarsawRepo = WeatherRepositoryImpl(
            remoteDataSource = WeatherMetroRemoteDataSource(
                fetchMetroApi = { _, _, _, _ ->
                    Result.failure(WeatherApiException(500, "Server Error for Warsaw"))
                }
            )
        )

        val warsawResult = failingWarsawRepo.getFullForecast(warsawLocation, forceRefresh = true)

        // Варшава не повинна отримати дані Києва! Має бути помилка, оскільки кешу для Варшави немає.
        assertTrue("Для міста без кешу при збої мережі має бути Result.failure", warsawResult.isFailure)
    }

    @Test
    fun testNoUnmarkedMockDataInProductionRepository() {
        // Вимога 7: У production немає мок-даних
        // Перевіряємо, що клас WeatherRepositoryImpl не містить посилань на MockWeatherRepository
        val classes = WeatherRepositoryImpl::class.java.declaredFields.map { it.type.name }
        assertFalse(
            "WeatherRepositoryImpl не повинен містити полів типу MockWeatherRepository",
            classes.any { it.contains("MockWeatherRepository") }
        )
    }

    @Test
    fun testStrictProhibitionOfOpenMeteoFallbackInRelease() = runBlocking {
        // Вимога 8: Сувора заборона fallback на Open-Meteo у релізі
        var shouldFail = false
        val dataSource = WeatherMetroRemoteDataSource(
            fetchMetroApi = { lat, lon, _, _ ->
                if (shouldFail) {
                    Result.failure(WeatherApiException(503, "Service Unavailable"))
                } else {
                    Result.success(createFakeMetroResponse(lat, lon))
                }
            }
        )

        val repository = WeatherRepositoryImpl(remoteDataSource = dataSource)
        repository.getFullForecast(kyivLocation, forceRefresh = true) // populate cache

        shouldFail = true
        val result = repository.getFullForecast(kyivLocation, forceRefresh = true)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()
        // Джерело даних суворо лишається WeatherMetro, ніколи не змінюється на Open-Meteo
        assertEquals(WeatherDataSource.WEATHER_METRO, forecast.dataSource)
        assertNotEquals(WeatherDataSource.OPEN_METEO, forecast.dataSource)
        assertTrue(forecast.isStale)
    }
}
