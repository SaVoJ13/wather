package com.weather.app.data

import com.weather.app.data.mapper.WeatherMetroMapper
import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Тести для [WeatherMetroMapper].
 * Перевіряє коректність мапінгу 16-денного та 384-годинного прогнозу,
 * визначення поточної погоди, атрибуції джерела WeatherMetro,
 * а також суворе відхилення відповідей із відсутніми або порожніми обов'язковими полями.
 */
class WeatherMetroMapperTest {

    private val testLocation = LocationModel(
        id = 1L,
        name = "Київ",
        country = "Україна",
        admin1 = "Київ",
        latitude = 50.45,
        longitude = 30.52,
        timezone = "Europe/Kyiv"
    )

    @Test
    fun testToDomainMapsFullForecastCorrectly() {
        val now = LocalDateTime.of(2026, 11, 23, 14, 0)

        // Створюємо 48 годин погодинного прогнозу
        val hourlyTimes = (0 until 48).map { i ->
            val dt = LocalDateTime.of(2026, 11, 23, 0, 0).plusHours(i.toLong())
            dt.toString()
        }
        val hourlyTemps = (0 until 48).map { 5.0 + (it % 10) }
        val hourlyPop = (0 until 48).map { (it * 2) % 100 }
        val hourlyCodes = (0 until 48).map { if (it % 2 == 0) 1 else 3 }
        val hourlyHumidity = (0 until 48).map { 70 + (it % 20) }
        val hourlyWind = (0 until 48).map { 10.0 + it * 0.5 }

        // Створюємо 16 днів щоденного прогнозу
        val dailyTimes = (0 until 16).map { i ->
            val dt = LocalDateTime.of(2026, 11, 23, 0, 0).plusDays(i.toLong())
            dt.toLocalDate().toString()
        }
        val dailyMaxTemps = (0 until 16).map { 10.0 + it }
        val dailyMinTemps = (0 until 16).map { 2.0 + it }
        val dailyPop = (0 until 16).map { (it * 5) % 100 }
        val dailyCodes = (0 until 16).map { if (it == 0) 1 else 61 }
        val dailyWindMax = (0 until 16).map { 15.0 + it }
        val dailySunrise = (0 until 16).map { "2026-11-23T07:22:00" }
        val dailySunset = (0 until 16).map { "2026-11-23T16:06:00" }

        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv",
            hourly = WeatherMetroHourlyDto(
                time = hourlyTimes,
                temperature2m = hourlyTemps,
                precipitationProbability = hourlyPop,
                weathercode = hourlyCodes,
                relativeHumidity2m = hourlyHumidity,
                windSpeed10m = hourlyWind
            ),
            daily = WeatherMetroDailyDto(
                time = dailyTimes,
                temperature2mMax = dailyMaxTemps,
                temperature2mMin = dailyMinTemps,
                precipitationProbabilityMax = dailyPop,
                weathercode = dailyCodes,
                sunrise = dailySunrise,
                sunset = dailySunset,
                windSpeed10mMax = dailyWindMax
            )
        )

        val domain = WeatherMetroMapper.toDomain(
            response = response,
            location = testLocation,
            updatedAt = now,
            sourceZoneId = java.time.ZoneId.of("Europe/Kyiv")
        )

        // 1. Джерело даних — WeatherMetro
        assertEquals(WeatherDataSource.WEATHER_METRO, domain.dataSource)
        assertEquals(false, domain.isStale)
        assertEquals(testLocation, domain.location)

        // 2. Поточна погода відповідає поточній годині (14:00)
        assertEquals(14, domain.current.time.hour)
        assertEquals(WeatherCondition.MAINLY_CLEAR, domain.current.condition)

        // 3. 24 години погодинного прогнозу
        assertEquals(24, domain.hourly24h.size)
        assertEquals(14, domain.hourly24h.first().time.hour)

        // 4. До 16 днів щоденного прогнозу
        assertEquals(16, domain.daily14Days.size)
        assertNotNull(domain.daily14Days.first().sunrise)
        assertNotNull(domain.daily14Days.first().sunset)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMissingHourlyThrowsIllegalArgumentException() {
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv",
            hourly = null,
            daily = WeatherMetroDailyDto(
                time = listOf("2026-11-23"),
                temperature2mMax = listOf(10.0),
                temperature2mMin = listOf(2.0),
                precipitationProbabilityMax = listOf(20),
                weathercode = listOf(1),
                sunrise = listOf("2026-11-23T07:22"),
                sunset = listOf("2026-11-23T16:06"),
                windSpeed10mMax = listOf(15.0)
            )
        )
        WeatherMetroMapper.toDomain(response, testLocation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMissingDailyThrowsIllegalArgumentException() {
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv",
            hourly = WeatherMetroHourlyDto(
                time = listOf("2026-11-23T12:00"),
                temperature2m = listOf(5.0),
                precipitationProbability = listOf(10),
                weathercode = listOf(1),
                relativeHumidity2m = listOf(75),
                windSpeed10m = listOf(12.0)
            ),
            daily = null
        )
        WeatherMetroMapper.toDomain(response, testLocation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyHourlyTemperatureThrowsIllegalArgumentException() {
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv",
            hourly = WeatherMetroHourlyDto(
                time = listOf("2026-11-23T12:00"),
                temperature2m = emptyList(), // порожній масив не повинен перетворюватись у 0.0
                precipitationProbability = listOf(10),
                weathercode = listOf(1),
                relativeHumidity2m = listOf(75),
                windSpeed10m = listOf(12.0)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf("2026-11-23"),
                temperature2mMax = listOf(10.0),
                temperature2mMin = listOf(2.0),
                precipitationProbabilityMax = listOf(20),
                weathercode = listOf(1),
                sunrise = listOf("2026-11-23T07:22"),
                sunset = listOf("2026-11-23T16:06"),
                windSpeed10mMax = listOf(15.0)
            )
        )
        WeatherMetroMapper.toDomain(response, testLocation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyDailyMaxTemperatureThrowsIllegalArgumentException() {
        val response = WeatherMetroResponse(
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv",
            hourly = WeatherMetroHourlyDto(
                time = listOf("2026-11-23T12:00"),
                temperature2m = listOf(5.0),
                precipitationProbability = listOf(10),
                weathercode = listOf(1),
                relativeHumidity2m = listOf(75),
                windSpeed10m = listOf(12.0)
            ),
            daily = WeatherMetroDailyDto(
                time = listOf("2026-11-23"),
                temperature2mMax = emptyList(), // порожній масив макс температур
                temperature2mMin = listOf(2.0),
                precipitationProbabilityMax = listOf(20),
                weathercode = listOf(1),
                sunrise = listOf("2026-11-23T07:22"),
                sunset = listOf("2026-11-23T16:06"),
                windSpeed10mMax = listOf(15.0)
            )
        )
        WeatherMetroMapper.toDomain(response, testLocation)
    }

    @Test
    fun testTrailingTimezoneShiftNullTemperaturesAreFilteredSafelyWithoutFailingRequest() {
        val now = LocalDateTime.of(2026, 11, 24, 3, 0)
        // Останні 4 години 16-го дня містять null через зсув часового поясу UTC+1/+2
        val hourlyTimes = listOf("2026-11-24T03:00", "2026-11-24T04:00", "2026-12-09T22:00", "2026-12-09T23:00")
        val hourlyTemps: List<Double?> = listOf(1.3, 1.4, null, null)

        val dailyTimes = listOf("2026-11-24", "2026-11-25", "2026-12-09")
        val dailyMaxTemps: List<Double?> = listOf(2.9, 2.4, null)
        val dailyMinTemps: List<Double?> = listOf(0.4, -1.9, null)

        val response = WeatherMetroResponse(
            latitude = 53.3745,
            longitude = 14.4725,
            timezone = "Europe/Warsaw",
            hourly = WeatherMetroHourlyDto(
                time = hourlyTimes,
                temperature2m = hourlyTemps,
                weathercode = listOf(3, 3, null, null)
            ),
            daily = WeatherMetroDailyDto(
                time = dailyTimes,
                temperature2mMax = dailyMaxTemps,
                temperature2mMin = dailyMinTemps,
                weathercode = listOf(3, 2, null)
            )
        )

        val domain = WeatherMetroMapper.toDomain(
            response = response,
            location = testLocation,
            updatedAt = now,
            sourceZoneId = java.time.ZoneId.of("Europe/Warsaw")
        )

        assertEquals("isStale must be false for fresh API response", false, domain.isStale)
        assertEquals("Only valid non-null hourly entries must be mapped", 2, domain.hourly24h.size)
        assertEquals("Only valid non-null daily entries must be mapped", 2, domain.daily14Days.size)
        assertEquals(1.3, domain.current.temperatureCelsius, 0.01)
    }
}
