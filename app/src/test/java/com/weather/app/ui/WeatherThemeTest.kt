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

    @Test
    fun testBottomNavigationBarContrastMeetsWcagAaAcrossAllPalettes() {
        for (condition in WeatherCondition.entries) {
            for (isDark in listOf(false, true)) {
                for (isDay in listOf(false, true)) {
                    val palette = weatherPaletteFor(condition, isDark = isDark, isDay = isDay)
                    val bg = palette.navBarContainerColor

                    val selectedTextContrast = com.weather.app.ui.theme.WeatherMotionPolicy.calculateContrastRatio(
                        palette.navBarSelectedTextColor,
                        bg
                    )
                    val unselectedTextContrast = com.weather.app.ui.theme.WeatherMotionPolicy.calculateContrastRatio(
                        palette.navBarUnselectedTextColor,
                        bg
                    )
                    val selectedIconContrast = com.weather.app.ui.theme.WeatherMotionPolicy.calculateContrastRatio(
                        palette.navBarSelectedIconColor,
                        bg
                    )
                    val selectedIconOnIndicatorContrast = com.weather.app.ui.theme.WeatherMotionPolicy.calculateContrastRatio(
                        palette.navBarSelectedIconColor,
                        palette.navBarIndicatorColor
                    )
                    val unselectedIconContrast = com.weather.app.ui.theme.WeatherMotionPolicy.calculateContrastRatio(
                        palette.navBarUnselectedIconColor,
                        bg
                    )

                    assertTrue(
                        "Selected tab text contrast ($selectedTextContrast) must exceed WCAG AAA (>= 7.0) for $condition (isDark=$isDark)",
                        selectedTextContrast >= 7.0
                    )
                    assertTrue(
                        "Unselected tab text contrast ($unselectedTextContrast) must exceed WCAG AA (>= 4.5) for $condition (isDark=$isDark)",
                        unselectedTextContrast >= 4.5
                    )
                    assertTrue(
                        "Selected tab icon contrast ($selectedIconContrast) must exceed WCAG AA (>= 4.5) for $condition (isDark=$isDark)",
                        selectedIconContrast >= 4.5
                    )
                    assertTrue(
                        "Selected tab icon contrast on indicator ($selectedIconOnIndicatorContrast) must exceed WCAG AA (>= 3.0) for $condition (isDark=$isDark)",
                        selectedIconOnIndicatorContrast >= 3.0
                    )
                    assertTrue(
                        "Unselected tab icon contrast ($unselectedIconContrast) must exceed WCAG AA (>= 4.5) for $condition (isDark=$isDark)",
                        unselectedIconContrast >= 4.5
                    )
                }
            }
        }
    }
}
