package com.weather.app.data

import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.mapper.WeatherMetroMapper
import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Тести достовірності та точності метеоданих (TASK 4 — Data Integrity Tests).
 * 
 * Перевіряє суворе дотримання вимог:
 * 1. Відсутній тиск → не відображається 1013.25 гПа (повертається null).
 * 2. Відсутній UV → не відображається UV 0.0 (повертається null).
 * 3. Відсутня вологість → не відображається 50% (повертається null).
 * 4. Відсутня сума опадів → не відображається 0 мм (повертається null).
 * 5. Відсутня "відчувається як" → не підміняється фактичною температурою (повертається null).
 * 6. Відсутня ймовірність опадів → не підміняється 0% (повертається null).
 * 7. Реальні нульові показники з API (0.0 мм, 0%, UV 0.0) коректно зберігаються як 0.
 * 8. Кеш зберігає відсутність значень без фальшивих підмін.
 * 9. Ізоляція кешу: місто А ніколи не отримує кеш міста Б.
 */
class WeatherDataIntegrityTest {

    private val kyivLocation = LocationModel(
        id = 703448L,
        name = "Київ",
        country = "Україна",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv",
        isCurrentLocation = true
    )

    private val warsawLocation = LocationModel(
        id = 756135L,
        name = "Варшава",
        country = "Польща",
        latitude = 52.2297,
        longitude = 21.0122,
        timezone = "Europe/Warsaw",
        isCurrentLocation = false
    )

    @Before
    fun setUp() {
        WeatherLocalCache.clearAll()
    }

    @After
    fun tearDown() {
        WeatherLocalCache.clearAll()
    }

    @Test
    fun testMissingMetricsDoNotSubstituteFakeValues() {
        val now = LocalDateTime.now()
        val timeStr = "2026-11-23T12:00:00"
        val dateStr = "2026-11-23"

        // Відповідь API з відсутніми (null) додатковими метеопоказниками
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            hourly = WeatherMetroHourlyDto(
                time = listOf(timeStr),
                temperature2m = listOf(14.0),
                apparentTemperature = listOf(null),
                precipitationProbability = listOf(null),
                weathercode = listOf(1),
                relativeHumidity2m = listOf(null),
                windSpeed10m = listOf(null),
                surfacePressure = listOf(null),
                uvIndex = listOf(null)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf(dateStr),
                temperature2mMax = listOf(18.0),
                temperature2mMin = listOf(8.0),
                precipitationSum = listOf(null),
                precipitationProbabilityMax = listOf(null),
                weathercode = listOf(1),
                sunrise = listOf("2026-11-23T07:20:00"),
                sunset = listOf("2026-11-23T16:15:00"),
                windSpeed10mMax = listOf(null)
            )
        )

        val domain = WeatherMetroMapper.toDomain(response, kyivLocation, now)

        // 1. Відсутній тиск → не 1013.25
        assertNull("Відсутній тиск повинен бути null, а не 1013.25 гПа", domain.current.surfacePressureHpa)

        // 2. Відсутній UV → не 0.0
        assertNull("Відсутній УФ-індекс повинен бути null, а не 0.0", domain.current.uvIndex)

        // 3. Відсутня вологість → не 50%
        assertNull("Відсутня вологість повинна бути null, а не 50%", domain.current.relativeHumidityPercent)

        // 4. Відсутня "відчувається як" → не підставляти temp (14.0)
        assertNull("Відсутня температура 'відчувається як' повинна бути null", domain.current.apparentTemperatureCelsius)

        // 5. Відсутній вітер → null
        assertNull("Відсутня швидкість вітру повинна бути null", domain.current.windSpeedKmh)

        // 6. Відсутня ймовірність опадів у погодинному прогнозі → null
        assertNull("Відсутня ймовірність опадів повинна бути null", domain.hourly24h.first().precipitationProbabilityPercent)

        // 7. Відсутня сума опадів у щоденному прогнозі → не 0.0 мм
        assertNull("Відсутня сума опадів повинна бути null, а не 0.0 мм", domain.daily14Days.first().precipitationSumMm)

        // 8. Відсутня ймовірність опадів у щоденному прогнозі → null
        assertNull("Відсутня максимальна ймовірність опадів повинна бути null", domain.daily14Days.first().precipitationProbabilityPercent)
    }

    @Test
    fun testRealZeroValuesFromApiArePreservedAsZero() {
        val now = LocalDateTime.now()
        val timeStr = "2026-11-23T12:00:00"
        val dateStr = "2026-11-23"

        // Відповідь API з фактичними нулями (ясний день без опадів і вітру)
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            hourly = WeatherMetroHourlyDto(
                time = listOf(timeStr),
                temperature2m = listOf(0.0),
                apparentTemperature = listOf(0.0),
                precipitationProbability = listOf(0),
                weathercode = listOf(0),
                relativeHumidity2m = listOf(20),
                windSpeed10m = listOf(0.0),
                surfacePressure = listOf(1000.0),
                uvIndex = listOf(0.0)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf(dateStr),
                temperature2mMax = listOf(5.0),
                temperature2mMin = listOf(-2.0),
                precipitationSum = listOf(0.0),
                precipitationProbabilityMax = listOf(0),
                weathercode = listOf(0),
                sunrise = listOf("2026-11-23T07:20:00"),
                sunset = listOf("2026-11-23T16:15:00"),
                windSpeed10mMax = listOf(0.0)
            )
        )

        val domain = WeatherMetroMapper.toDomain(response, kyivLocation, now)

        assertEquals("Реальний нуль опадів повинен залишатися 0.0", 0.0, domain.daily14Days.first().precipitationSumMm ?: -1.0, 0.001)
        assertEquals("Реальний нуль ймовірності опадів повинен залишатися 0", 0, domain.daily14Days.first().precipitationProbabilityPercent)
        assertEquals("Реальний нуль UV повинен залишатися 0.0", 0.0, domain.current.uvIndex ?: -1.0, 0.001)
        assertEquals("Реальний нуль вітру повинен залишатися 0.0", 0.0, domain.current.windSpeedKmh ?: -1.0, 0.001)
    }

    @Test
    fun testCachePreservesAbsenceOfValuesWithoutSubstitution() {
        val now = LocalDateTime.now()
        val forecast = FullWeatherForecast(
            location = kyivLocation,
            current = CurrentWeather(
                temperatureCelsius = 12.0,
                apparentTemperatureCelsius = null,
                condition = WeatherCondition.CLEAR_SKY,
                relativeHumidityPercent = null,
                windSpeedKmh = null,
                surfacePressureHpa = null,
                uvIndex = null,
                time = now
            ),
            hourly24h = listOf(
                HourlyForecastItem(
                    time = now,
                    temperatureCelsius = 12.0,
                    condition = WeatherCondition.CLEAR_SKY,
                    precipitationProbabilityPercent = null
                )
            ),
            daily14Days = listOf(
                DailyForecastItem(
                    date = now,
                    minTemperatureCelsius = 5.0,
                    maxTemperatureCelsius = 15.0,
                    condition = WeatherCondition.CLEAR_SKY,
                    precipitationProbabilityPercent = null,
                    precipitationSumMm = null,
                    maxWindSpeedKmh = null
                )
            ),
            updatedAt = now,
            isStale = false,
            dataSource = WeatherDataSource.WEATHER_METRO
        )

        WeatherLocalCache.saveForecast(null, forecast)
        val cached = WeatherLocalCache.loadCachedForecastForLocation(null, kyivLocation)

        assertNotNull("Кеш повинен зчитуватися", cached)
        assertNull("Кеш не повинен підставляти фіктивний apparentTemperature", cached?.current?.apparentTemperatureCelsius)
        assertNull("Кеш не повинен підставляти фіктивний тиск", cached?.current?.surfacePressureHpa)
        assertNull("Кеш не повинен підставляти фіктивний UV", cached?.current?.uvIndex)
        assertNull("Кеш не повинен підставляти фіктивну вологість", cached?.current?.relativeHumidityPercent)
        assertNull("Кеш не повинен підставляти фіктивний вітер", cached?.current?.windSpeedKmh)
        assertNull("Кеш не повинен підставляти фіктивні опади", cached?.daily14Days?.first()?.precipitationSumMm)
    }

    @Test
    fun testCityCacheIsolationCityANeverGetsCityBCache() {
        val now = LocalDateTime.now()
        val kyivForecast = FullWeatherForecast(
            location = kyivLocation,
            current = CurrentWeather(
                temperatureCelsius = 10.0,
                apparentTemperatureCelsius = 10.0,
                condition = WeatherCondition.RAIN,
                relativeHumidityPercent = 80,
                windSpeedKmh = 15.0,
                surfacePressureHpa = 1010.0,
                uvIndex = 1.0,
                time = now
            ),
            hourly24h = emptyList(),
            daily14Days = emptyList(),
            updatedAt = now,
            isStale = false,
            dataSource = WeatherDataSource.WEATHER_METRO
        )

        WeatherLocalCache.saveForecast(null, kyivForecast)

        val warsawResult = WeatherLocalCache.loadCachedForecastForLocation(null, warsawLocation)
        assertNull("Запит для Варшави не повинен повертати кеш Києва", warsawResult)

        val kyivResult = WeatherLocalCache.loadCachedForecastForLocation(null, kyivLocation)
        assertNotNull("Запит для Києва повинен повернути збережений прогноз", kyivResult)
        assertEquals("Київ", kyivResult?.location?.name)
    }

    @Test
    fun testWmoCodeIntegrityZeroVsNullVsInvalid() {
        val now = LocalDateTime.now()
        val timeStr = "2026-11-23T12:00:00"
        val dateStr = "2026-11-23"

        // 1. Справжній числовий 0 з API -> CLEAR_SKY
        val responseWithZero = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            hourly = WeatherMetroHourlyDto(
                time = listOf(timeStr),
                temperature2m = listOf(15.0),
                weathercode = listOf(0)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf(dateStr),
                temperature2mMax = listOf(18.0),
                temperature2mMin = listOf(8.0),
                weathercode = listOf(0)
            )
        )
        val forecastWithZero = WeatherMetroMapper.toDomain(responseWithZero, kyivLocation, now)
        assertEquals(WeatherCondition.CLEAR_SKY, forecastWithZero.current.condition)
        assertEquals(WeatherCondition.CLEAR_SKY, forecastWithZero.hourly24h.first().condition)
        assertEquals(WeatherCondition.CLEAR_SKY, forecastWithZero.daily14Days.first().condition)

        // 2. Відсутній код (null) -> UNKNOWN (не перетворюється на CLEAR_SKY!)
        val responseWithNull = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            hourly = WeatherMetroHourlyDto(
                time = listOf(timeStr),
                temperature2m = listOf(15.0),
                weathercode = listOf(null)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf(dateStr),
                temperature2mMax = listOf(18.0),
                temperature2mMin = listOf(8.0),
                weathercode = listOf(null)
            )
        )
        val forecastWithNull = WeatherMetroMapper.toDomain(responseWithNull, kyivLocation, now)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithNull.current.condition)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithNull.hourly24h.first().condition)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithNull.daily14Days.first().condition)

        // 3. Невалідні коди (-1, 999) -> UNKNOWN
        val responseWithInvalid = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            hourly = WeatherMetroHourlyDto(
                time = listOf(timeStr),
                temperature2m = listOf(15.0),
                weathercode = listOf(-1)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf(dateStr),
                temperature2mMax = listOf(18.0),
                temperature2mMin = listOf(8.0),
                weathercode = listOf(999)
            )
        )
        val forecastWithInvalid = WeatherMetroMapper.toDomain(responseWithInvalid, kyivLocation, now)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithInvalid.current.condition)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithInvalid.hourly24h.first().condition)
        assertEquals(WeatherCondition.UNKNOWN, forecastWithInvalid.daily14Days.first().condition)
    }
}
