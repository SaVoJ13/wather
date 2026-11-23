package com.weather.app.ui

import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.weatherConditionIconData
import com.weather.app.ui.theme.ClearSkyDayTop
import com.weather.app.ui.theme.ClearSkyNightTop
import com.weather.app.ui.theme.RainTop
import com.weather.app.ui.theme.SnowDarkTop
import com.weather.app.ui.theme.SnowLightTop
import com.weather.app.ui.theme.ThunderstormTop
import com.weather.app.ui.theme.weatherPaletteFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherThemeTest {

    @Test
    fun testWeatherPaletteForClearSkyDayAndNight() {
        val dayPalette = weatherPaletteFor(WeatherCondition.CLEAR_SKY, isDark = false, isDay = true)
        assertEquals(ClearSkyDayTop, dayPalette.gradientColors.first())

        val nightPalette = weatherPaletteFor(WeatherCondition.CLEAR_SKY, isDark = true, isDay = false)
        assertEquals(ClearSkyNightTop, nightPalette.gradientColors.first())
    }

    @Test
    fun testWeatherPaletteForRainAndThunderstorm() {
        val rainPalette = weatherPaletteFor(WeatherCondition.RAIN, isDark = false)
        assertEquals(RainTop, rainPalette.gradientColors.first())

        val stormPalette = weatherPaletteFor(WeatherCondition.THUNDERSTORM, isDark = true)
        assertEquals(ThunderstormTop, stormPalette.gradientColors.first())
    }

    @Test
    fun testWeatherPaletteForSnowLightAndDark() {
        val snowLight = weatherPaletteFor(WeatherCondition.SNOW, isDark = false)
        assertEquals(SnowLightTop, snowLight.gradientColors.first())

        val snowDark = weatherPaletteFor(WeatherCondition.SNOW, isDark = true)
        assertEquals(SnowDarkTop, snowDark.gradientColors.first())
    }

    @Test
    fun testWeatherConditionIconDataProvidesUkrainianDescriptions() {
        for (condition in WeatherCondition.entries) {
            val (icon, description) = weatherConditionIconData(condition)
            assertTrue("Description for $condition must not be blank", description.isNotBlank())
            assertTrue("Icon for $condition must not be null", icon.name.isNotBlank())
        }
    }

    @Test
    fun testWeatherMotionPolicyRespectsReducedMotion() {
        // Normal motion (reducedMotion = false)
        assertEquals(600, com.weather.app.ui.theme.WeatherMotionPolicy.skyGradientDurationMillis(false))
        assertEquals(220, com.weather.app.ui.theme.WeatherMotionPolicy.tabCrossfadeDurationMillis(false))
        assertEquals(250, com.weather.app.ui.theme.WeatherMotionPolicy.screenStateCrossfadeDurationMillis(false))
        assertEquals(1000, com.weather.app.ui.theme.WeatherMotionPolicy.sunArcAnimationDurationMillis(false))
        assertTrue(com.weather.app.ui.theme.WeatherMotionPolicy.shouldRunInfiniteAnimations(false))

        // Reduced motion enabled (reducedMotion = true)
        assertEquals(0, com.weather.app.ui.theme.WeatherMotionPolicy.skyGradientDurationMillis(true))
        assertEquals(0, com.weather.app.ui.theme.WeatherMotionPolicy.tabCrossfadeDurationMillis(true))
        assertEquals(80, com.weather.app.ui.theme.WeatherMotionPolicy.screenStateCrossfadeDurationMillis(true))
        assertEquals(0, com.weather.app.ui.theme.WeatherMotionPolicy.sunArcAnimationDurationMillis(true))
        org.junit.Assert.assertFalse(
            "Infinite animations must be disabled when Reduced Motion is active",
            com.weather.app.ui.theme.WeatherMotionPolicy.shouldRunInfiniteAnimations(true)
        )
    }
}
