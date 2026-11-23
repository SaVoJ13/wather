package com.weather.app.data

import com.weather.app.data.mapper.WeatherMetroMapper
import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.domain.model.LocationModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Тести обчислення часу та часових поясів (TASK C):
 * - Warsaw -> Kyiv (+1 година)
 * - Warsaw -> Tokyo (перетин півночі, наступний день)
 * - Зміна літнього/зимового часу (DST)
 * - Стійкість до невалідного або відсутнього часового поясу (fallback)
 */
class WeatherLocationTimezoneTest {

    private val warsawZone = ZoneId.of("Europe/Warsaw")
    private val kyivZone = ZoneId.of("Europe/Kyiv")
    private val tokyoZone = ZoneId.of("Asia/Tokyo")

    private fun createHourlyDto(startDateTime: LocalDateTime, count: Int = 48): WeatherMetroHourlyDto {
        val times = (0 until count).map { i ->
            startDateTime.plusHours(i.toLong()).toString()
        }
        val temps = (0 until count).map { 15.0 + it }
        val codes = (0 until count).map { 0 }
        return WeatherMetroHourlyDto(
            time = times,
            temperature2m = temps,
            weathercode = codes
        )
    }

    private fun createDailyDto(startDate: LocalDateTime, count: Int = 14): WeatherMetroDailyDto {
        val times = (0 until count).map { i ->
            startDate.plusDays(i.toLong()).toLocalDate().toString()
        }
        return WeatherMetroDailyDto(
            time = times,
            temperature2mMax = (0 until count).map { 20.0 },
            temperature2mMin = (0 until count).map { 10.0 },
            weathercode = (0 until count).map { 0 }
        )
    }

    @Test
    fun testWarsawToKyivTimezoneHourSelection() {
        // Пристрій у Варшаві (UTC+1 узимку). Час на пристрої: 18:30 (23 листопада 2026).
        val deviceWarsawTime = LocalDateTime.of(2026, 11, 23, 18, 30, 0)
        // У Києві (UTC+2 узимку) у цей момент 19:30.
        // Відповідно, поточна година в Києві має бути 19:00, а не 18:00!

        val kyivLocation = LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv"
        )

        val hourlyDto = createHourlyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 48)
        val dailyDto = createDailyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 14)

        val response = WeatherMetroResponse(
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv",
            hourly = hourlyDto,
            daily = dailyDto
        )

        val forecast = WeatherMetroMapper.toDomain(
            response = response,
            location = kyivLocation,
            updatedAt = deviceWarsawTime,
            sourceZoneId = warsawZone
        )

        // Поточна погода повинна вибрати 19:00 за часом Києва
        assertEquals(LocalDateTime.of(2026, 11, 23, 19, 0, 0), forecast.current.time)
        // Погодинний зріз 24h повинен починатися з 19:00 за часом Києва
        assertEquals(LocalDateTime.of(2026, 11, 23, 19, 0, 0), forecast.hourly24h.first().time)
        assertEquals(24, forecast.hourly24h.size)
    }

    @Test
    fun testWarsawToTokyoMidnightCrossing() {
        // Пристрій у Варшаві (UTC+1). Час на пристрої: 18:30 (23 листопада 2026).
        val deviceWarsawTime = LocalDateTime.of(2026, 11, 23, 18, 30, 0)
        // У Токіо (UTC+9) різниця з Варшавою становить +8 годин:
        // 18:30 + 8 годин = 02:30 вже наступного дня (24 листопада 2026)!

        val tokyoLocation = LocationModel(
            id = 1850147L,
            name = "Tokyo",
            country = "Japan",
            latitude = 35.6895,
            longitude = 139.6917,
            timezone = "Asia/Tokyo"
        )

        // Створюємо погодинний масив за місцевим часом Токіо починаючи з 23 листопада 00:00
        val hourlyDto = createHourlyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 72)
        val dailyDto = createDailyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 14)

        val response = WeatherMetroResponse(
            latitude = 35.6895,
            longitude = 139.6917,
            timezone = "Asia/Tokyo",
            hourly = hourlyDto,
            daily = dailyDto
        )

        val forecast = WeatherMetroMapper.toDomain(
            response = response,
            location = tokyoLocation,
            updatedAt = deviceWarsawTime,
            sourceZoneId = warsawZone
        )

        // Перевіряємо, що перетин півночі відбувся коректно: година має бути 02:00 наступного дня (24 листопада)
        val expectedTokyoHour = LocalDateTime.of(2026, 11, 24, 2, 0, 0)
        assertEquals(expectedTokyoHour, forecast.current.time)
        assertEquals(expectedTokyoHour, forecast.hourly24h.first().time)
        assertEquals(24, forecast.hourly24h.size)
    }

    @Test
    fun testDstTransitionCalculation() {
        // Влітку (15 липня) Варшава в UTC+2 (CEST)
        val summerWarsawTime = LocalDateTime.of(2026, 7, 15, 12, 0, 0)
        // Лондон у UTC+1 (BST)
        val londonZone = ZoneId.of("Europe/London")

        val londonLocation = LocationModel(
            id = 2643743L,
            name = "London",
            country = "United Kingdom",
            latitude = 51.5074,
            longitude = -0.1278,
            timezone = "Europe/London"
        )

        val hourlyDto = createHourlyDto(LocalDateTime.of(2026, 7, 15, 0, 0, 0), count = 48)
        val dailyDto = createDailyDto(LocalDateTime.of(2026, 7, 15, 0, 0, 0), count = 14)

        val response = WeatherMetroResponse(
            latitude = 51.5074,
            longitude = -0.1278,
            timezone = "Europe/London",
            hourly = hourlyDto,
            daily = dailyDto
        )

        val forecast = WeatherMetroMapper.toDomain(
            response = response,
            location = londonLocation,
            updatedAt = summerWarsawTime,
            sourceZoneId = warsawZone
        )

        // У Лондоні о 12:00 за Варшавою має бути 11:00
        assertEquals(LocalDateTime.of(2026, 7, 15, 11, 0, 0), forecast.current.time)
        assertEquals(LocalDateTime.of(2026, 7, 15, 11, 0, 0), forecast.hourly24h.first().time)
    }

    @Test
    fun testInvalidOrMissingTimezoneFallback() {
        val testTime = LocalDateTime.of(2026, 11, 23, 14, 15, 0)

        // Випадок 1: Невалідний часовий пояс в API та null в локації
        val fallbackZone = WeatherMetroMapper.resolveZoneId("Invalid/NonExistent_Zone", fallback = warsawZone)
        assertEquals(warsawZone, fallbackZone)

        // Випадок 2: "auto" в API
        val autoZone = WeatherMetroMapper.resolveZoneId("auto", fallback = kyivZone)
        assertEquals(kyivZone, autoZone)

        // Випадок 3: Порожній або null рядок
        val nullZone = WeatherMetroMapper.resolveZoneId(null, fallback = tokyoZone)
        assertEquals(tokyoZone, nullZone)

        val blankZone = WeatherMetroMapper.resolveZoneId("   ", fallback = warsawZone)
        assertEquals(warsawZone, blankZone)

        // Випадок 4: Повноцінний мапінг із невалідним часовим поясом не повинен викидати виняток
        val invalidLocation = LocationModel(
            id = 1L,
            name = "Test",
            country = "Test",
            latitude = 0.0,
            longitude = 0.0,
            timezone = "Malformed/Zone"
        )
        val hourlyDto = createHourlyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 48)
        val dailyDto = createDailyDto(LocalDateTime.of(2026, 11, 23, 0, 0, 0), count = 14)
        val response = WeatherMetroResponse(
            latitude = 0.0,
            longitude = 0.0,
            timezone = "Unknown/Broken",
            hourly = hourlyDto,
            daily = dailyDto
        )

        val forecast = WeatherMetroMapper.toDomain(
            response = response,
            location = invalidLocation,
            updatedAt = testTime,
            sourceZoneId = warsawZone
        )

        // При fallback має використатись час sourceZoneId (14:00)
        assertEquals(LocalDateTime.of(2026, 11, 23, 14, 0, 0), forecast.current.time)
    }
}
