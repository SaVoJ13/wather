package com.weather.app.ui

import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.SunriseSunsetCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SunriseSunsetTest {

    private val testDate = LocalDate.of(2026, 11, 17)
    private val sunrise = testDate.atTime(7, 20)
    private val sunset = testDate.atTime(16, 15)

    @Test
    fun beforeSunrise_returnsZeroProgress_andSunriseStatus() {
        val currentTime = testDate.atTime(6, 0)
        val progress = SunriseSunsetCalculator.calculateSunProgress(currentTime, sunrise, sunset)
        val status = SunriseSunsetCalculator.calculateStatus(currentTime, sunrise, sunset)

        assertEquals(0f, progress, 0.001f)
        assertTrue("Status should indicate time until sunrise: $status", status.contains("Схід через 1 год 20 хв"))
    }

    @Test
    fun exactSunrise_returnsZeroProgress() {
        val currentTime = sunrise
        val progress = SunriseSunsetCalculator.calculateSunProgress(currentTime, sunrise, sunset)

        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun midday_returnsAccurateProgress_andSunsetStatus() {
        // Sunrise 07:20 -> Sunset 16:15 is 8h 55m = 535m
        // Midpoint is 267.5m after 07:20 = 11:47:30
        val midTime = testDate.atTime(11, 47, 30)
        val progress = SunriseSunsetCalculator.calculateSunProgress(midTime, sunrise, sunset)
        val status = SunriseSunsetCalculator.calculateStatus(midTime, sunrise, sunset)

        assertEquals(0.5f, progress, 0.01f)
        assertTrue("Status should indicate sunset time: $status", status.contains("Захід через"))
    }

    @Test
    fun exactSunset_returnsOneProgress() {
        val currentTime = sunset
        val progress = SunriseSunsetCalculator.calculateSunProgress(currentTime, sunrise, sunset)

        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun afterSunset_returnsOneProgress_andNightStatus() {
        val currentTime = testDate.atTime(21, 30)
        val progress = SunriseSunsetCalculator.calculateSunProgress(currentTime, sunrise, sunset)
        val status = SunriseSunsetCalculator.calculateStatus(currentTime, sunrise, sunset)

        assertEquals(1f, progress, 0.001f)
        assertTrue("Status should indicate night: $status", status.contains("Ніч"))
        assertTrue("Status should mention sunrise: $status", status.contains("07:20"))
    }

    @Test
    fun dayDuration_formattedCorrectly() {
        val durationText = SunriseSunsetCalculator.formatDuration(sunrise, sunset)
        assertEquals("8 год 55 хв", durationText)
    }

    @Test
    fun fallbackTimes_whenForecastIsNull() {
        val currentTime = testDate.atTime(12, 0)
        val (calcSunrise, calcSunset) = SunriseSunsetCalculator.calculateTimes(null, currentTime)

        assertEquals(testDate.atTime(7, 20), calcSunrise)
        assertEquals(testDate.atTime(16, 15), calcSunset)
    }

    @Test
    fun usesForecastTimes_whenProvided() {
        val customSunrise = testDate.atTime(6, 45)
        val customSunset = testDate.atTime(18, 30)
        val forecastItem = DailyForecastItem(
            date = testDate.atStartOfDay(),
            minTemperatureCelsius = 5.0,
            maxTemperatureCelsius = 15.0,
            condition = WeatherCondition.CLEAR_SKY,
            precipitationProbabilityPercent = 0,
            precipitationSumMm = 0.0,
            maxWindSpeedKmh = 10.0,
            sunrise = customSunrise,
            sunset = customSunset
        )

        val currentTime = testDate.atTime(12, 0)
        val (calcSunrise, calcSunset) = SunriseSunsetCalculator.calculateTimes(forecastItem, currentTime)

        assertEquals(customSunrise, calcSunrise)
        assertEquals(customSunset, calcSunset)
    }
}
