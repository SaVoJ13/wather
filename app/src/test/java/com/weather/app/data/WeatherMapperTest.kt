package com.weather.app.data

import com.weather.app.data.mapper.WeatherMapper
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.data.remote.dto.OpenMeteoCurrentDto
import com.weather.app.data.remote.dto.OpenMeteoDailyDto
import com.weather.app.data.remote.dto.OpenMeteoForecastResponse
import com.weather.app.data.remote.dto.OpenMeteoHourlyDto
import com.weather.app.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class WeatherMapperTest {

    @Test
    fun parseDateTime_validIso_returnsLocalDateTime() {
        val dt = WeatherMapper.parseDateTime("2026-11-17T14:30")
        assertNotNull(dt)
        assertEquals(2026, dt?.year)
        assertEquals(11, dt?.monthValue)
        assertEquals(17, dt?.dayOfMonth)
        assertEquals(14, dt?.hour)
        assertEquals(30, dt?.minute)
    }

    @Test
    fun parseDateTime_dateOnly_returnsStartOfDay() {
        val dt = WeatherMapper.parseDateTime("2026-11-17")
        assertNotNull(dt)
        assertEquals(2026, dt?.year)
        assertEquals(0, dt?.hour)
        assertEquals(0, dt?.minute)
    }

    @Test
    fun parseDateTime_invalidOrNull_returnsNull() {
        assertNull(WeatherMapper.parseDateTime(null))
        assertNull(WeatherMapper.parseDateTime(""))
        assertNull(WeatherMapper.parseDateTime("invalid-date"))
    }

    @Test
    fun toDomain_mapsFullResponseCorrectly() {
        val location = MockWeatherRepository.createKyivLocation()
        val fixedNow = LocalDateTime.of(2026, 11, 17, 12, 0)

        val response = OpenMeteoForecastResponse(
            latitude = 50.45,
            longitude = 30.52,
            utc_offset_seconds = 7200,
            timezone = "Europe/Kyiv",
            current = OpenMeteoCurrentDto(
                time = "2026-11-17T12:00",
                temperature_2m = 18.5,
                apparent_temperature = 17.8,
                relative_humidity_2m = 65,
                weather_code = 1,
                surface_pressure = 1014.2,
                wind_speed_10m = 15.0,
                uv_index = 3.5
            ),
            hourly = OpenMeteoHourlyDto(
                time = listOf("2026-11-17T12:00", "2026-11-17T13:00"),
                temperature_2m = listOf(18.5, 19.0),
                weather_code = listOf(1, 2),
                precipitation_probability = listOf(5, 15)
            ),
            daily = OpenMeteoDailyDto(
                time = listOf("2026-11-17", "2026-11-18"),
                weather_code = listOf(1, 61),
                temperature_2m_max = listOf(20.0, 16.5),
                temperature_2m_min = listOf(10.0, 9.0),
                precipitation_probability_max = listOf(10, 80),
                precipitation_sum = listOf(0.0, 4.5),
                wind_speed_10m_max = listOf(18.0, 25.0),
                sunrise = listOf("2026-11-17T06:00", "2026-11-18T06:01"),
                sunset = listOf("2026-11-17T18:00", "2026-11-18T17:58")
            )
        )

        val domain = WeatherMapper.toDomain(response, location, fixedNow)

        assertEquals("Київ", domain.location.name)
        assertEquals(18.5, domain.current.temperatureCelsius, 0.001)
        assertEquals(17.8, domain.current.apparentTemperatureCelsius, 0.001)
        assertEquals(WeatherCondition.MAINLY_CLEAR, domain.current.condition)
        assertEquals(65, domain.current.relativeHumidityPercent)

        assertEquals(2, domain.hourly24h.size)
        assertEquals(WeatherCondition.MAINLY_CLEAR, domain.hourly24h[0].condition)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, domain.hourly24h[1].condition)

        assertEquals(2, domain.daily14Days.size)
        assertEquals(WeatherCondition.MAINLY_CLEAR, domain.daily14Days[0].condition)
        assertEquals(WeatherCondition.RAIN, domain.daily14Days[1].condition)
        assertEquals(4.5, domain.daily14Days[1].precipitationSumMm, 0.001)
        assertNotNull(domain.daily14Days[0].sunrise)
    }

    @Test
    fun toDomain_hourlyForecastStartsFromCurrentHourNotMidnight() {
        val location = MockWeatherRepository.createKyivLocation()
        // Зараз 14:35 17 листопада 2026
        val fixedNow = LocalDateTime.of(2026, 11, 17, 14, 35)

        // Open-Meteo повертає масив від 00:00 17 листопада до 23:00 18 листопада (48 годин)
        val times = (0 until 48).map { i ->
            val dt = LocalDateTime.of(2026, 11, 17, 0, 0).plusHours(i.toLong())
            dt.toString()
        }
        val temps = (0 until 48).map { i -> 10.0 + i * 0.2 }
        val codes = (0 until 48).map { 1 }

        val response = OpenMeteoForecastResponse(
            latitude = 50.45,
            longitude = 30.52,
            utc_offset_seconds = 7200,
            timezone = "Europe/Kyiv",
            current = null,
            hourly = OpenMeteoHourlyDto(
                time = times,
                temperature_2m = temps,
                weather_code = codes
            ),
            daily = null
        )

        val domain = WeatherMapper.toDomain(response, location, fixedNow)

        // Повинно бути рівно 24 години
        assertEquals(24, domain.hourly24h.size)
        // Перший елемент повинен відповідати поточній годині (14:00)
        assertEquals(14, domain.hourly24h[0].time.hour)
        assertEquals(17, domain.hourly24h[0].time.dayOfMonth)
        // Останній 24-й елемент повинен бути через 23 години (13:00 наступного дня 18 листопада)
        assertEquals(13, domain.hourly24h[23].time.hour)
        assertEquals(18, domain.hourly24h[23].time.dayOfMonth)
    }
}
