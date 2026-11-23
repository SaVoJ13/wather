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
 * визначення поточної погоди та атрибуції джерела WeatherMetro.
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

        val domain = WeatherMetroMapper.toDomain(response, testLocation, updatedAt = now)

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
        assertEquals(10.0, domain.daily14Days.first().maxTemperatureCelsius, 0.01)
        assertEquals(2.0, domain.daily14Days.first().minTemperatureCelsius, 0.01)
        assertNotNull(domain.daily14Days.first().sunrise)
        assertNotNull(domain.daily14Days.first().sunset)
    }

    @Test
    fun testParseDateTimeAndDate() {
        val dt = WeatherMetroMapper.parseDateTime("2026-11-23T14:30:00")
        assertNotNull(dt)
        assertEquals(2026, dt!!.year)
        assertEquals(11, dt.monthValue)
        assertEquals(23, dt.dayOfMonth)
        assertEquals(14, dt.hour)
        assertEquals(30, dt.minute)

        val d = WeatherMetroMapper.parseDate("2026-11-23")
        assertNotNull(d)
        assertEquals(2026, d!!.year)
        assertEquals(11, d.monthValue)
        assertEquals(23, d.dayOfMonth)
    }
}
