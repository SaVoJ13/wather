package com.weather.app.widget

import com.weather.app.R
import com.weather.app.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherGlanceWidgetTest {

    @Test
    fun testWeatherConditionDrawableResMapsAllConditions() {
        for (condition in WeatherCondition.entries) {
            val resId = WeatherGlanceWidget.weatherConditionDrawableRes(condition)
            assertTrue("Drawable resource for $condition must be positive", resId > 0)
        }
    }

    @Test
    fun testSpecificWeatherConditionDrawables() {
        assertEquals(R.drawable.ic_widget_sunny, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.CLEAR_SKY))
        assertEquals(R.drawable.ic_widget_partly_cloudy, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.MAINLY_CLEAR))
        assertEquals(R.drawable.ic_widget_partly_cloudy, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.PARTLY_CLOUDY))
        assertEquals(R.drawable.ic_widget_cloudy, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.OVERCAST))
        assertEquals(R.drawable.ic_widget_rain, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.DRIZZLE))
        assertEquals(R.drawable.ic_widget_rain, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.RAIN))
        assertEquals(R.drawable.ic_widget_rain, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.HEAVY_RAIN))
        assertEquals(R.drawable.ic_widget_snow, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.SNOW))
        assertEquals(R.drawable.ic_widget_thunderstorm, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.THUNDERSTORM))
        assertEquals(R.drawable.ic_widget_fog, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.FOG))
    }

    @Test
    fun testFormatDayOfWeekUk() {
        assertEquals("пн", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.MONDAY))
        assertEquals("вт", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.TUESDAY))
        assertEquals("ср", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.WEDNESDAY))
        assertEquals("чт", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.THURSDAY))
        assertEquals("пт", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.FRIDAY))
        assertEquals("сб", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.SATURDAY))
        assertEquals("нд", WeatherGlanceWidget.formatDayOfWeekUk(java.time.DayOfWeek.SUNDAY))
    }

    @Test
    fun testWidgetReceiverProvidesGlanceAppWidget() {
        val receiver = WeatherWidgetReceiver()
        assertNotNull(receiver.glanceAppWidget)
        assertTrue(receiver.glanceAppWidget is WeatherGlanceWidget)
    }

    @Test
    fun testWidgetResponsiveSizesConfigured() {
        val widget = WeatherGlanceWidget()
        assertNotNull(widget.sizeMode)
    }

    @Test
    fun testWidgetSizes() {
        assertEquals(120.0f, WeatherGlanceWidget.COMPACT_SIZE.width.value, 0.01f)
        assertEquals(100.0f, WeatherGlanceWidget.COMPACT_SIZE.height.value, 0.01f)
        assertEquals(220.0f, WeatherGlanceWidget.EXPANDED_SIZE.width.value, 0.01f)
        assertEquals(100.0f, WeatherGlanceWidget.EXPANDED_SIZE.height.value, 0.01f)
    }

    @Test
    fun testIsExpandedModeFor2x2And4x2Sizes() {
        org.junit.Assert.assertFalse(
            "2x2 COMPACT_SIZE must use compact mode",
            WeatherGlanceWidget.isExpandedMode(WeatherGlanceWidget.COMPACT_SIZE)
        )
        assertTrue(
            "4x2 EXPANDED_SIZE must use expanded mode",
            WeatherGlanceWidget.isExpandedMode(WeatherGlanceWidget.EXPANDED_SIZE)
        )
    }

    @Test
    fun testResolveWidgetForecastNeverUsesMockDataWhenCacheIsEmpty() {
        val resolved = WeatherGlanceWidget.resolveWidgetForecast(
            cachedForecast = null,
            lastKnownForecast = null
        )
        org.junit.Assert.assertNull(
            "Widget must return null (Empty/No Data state) when no real cache exists, never fake Mock data",
            resolved
        )
    }

    @Test
    fun testFormatTodayRangeReturnsNullWhenDailyEmpty_neverFakesZero() {
        val now = java.time.LocalDateTime.of(2026, 11, 23, 14, 0)
        val location = com.weather.app.domain.model.LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv"
        )
        val forecastWithoutDaily = com.weather.app.domain.model.FullWeatherForecast(
            location = location,
            current = com.weather.app.domain.model.CurrentWeather(
                temperatureCelsius = 11.4,
                apparentTemperatureCelsius = null,
                condition = WeatherCondition.PARTLY_CLOUDY,
                relativeHumidityPercent = null,
                windSpeedKmh = null,
                surfacePressureHpa = null,
                uvIndex = null,
                time = now
            ),
            hourly24h = emptyList(),
            daily14Days = emptyList(),
            updatedAt = now,
            isStale = false,
            dataSource = com.weather.app.domain.model.WeatherDataSource.WEATHER_METRO
        )

        org.junit.Assert.assertNull(
            "formatTodayRange must return null when daily14Days is empty, never fake ↑0° / ↓0°",
            WeatherGlanceWidget.formatTodayRange(forecastWithoutDaily)
        )

        val forecastWithDaily = forecastWithoutDaily.copy(
            daily14Days = listOf(
                com.weather.app.domain.model.DailyForecastItem(
                    date = now,
                    minTemperatureCelsius = 4.2,
                    maxTemperatureCelsius = 13.8,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    precipitationProbabilityPercent = 20,
                    precipitationSumMm = null,
                    maxWindSpeedKmh = null
                )
            )
        )
        assertEquals("↑14° / ↓4°", WeatherGlanceWidget.formatTodayRange(forecastWithDaily))
    }

    @Test
    fun testIsForecastStaleForWidget() {
        val baseTime = java.time.LocalDateTime.of(2026, 11, 23, 12, 0)
        val location = com.weather.app.domain.model.LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            latitude = 50.45,
            longitude = 30.52,
            timezone = "Europe/Kyiv"
        )
        val freshForecast = com.weather.app.domain.model.FullWeatherForecast(
            location = location,
            current = com.weather.app.domain.model.CurrentWeather(
                temperatureCelsius = 10.0,
                apparentTemperatureCelsius = 9.0,
                condition = WeatherCondition.CLEAR_SKY,
                relativeHumidityPercent = 65,
                windSpeedKmh = 12.0,
                surfacePressureHpa = 1015.0,
                uvIndex = 2.0,
                time = baseTime
            ),
            hourly24h = emptyList(),
            daily14Days = emptyList(),
            updatedAt = baseTime,
            isStale = false,
            dataSource = com.weather.app.domain.model.WeatherDataSource.WEATHER_METRO
        )

        org.junit.Assert.assertFalse(
            "Forecast updated 15 minutes ago must not be stale",
            WeatherGlanceWidget.isForecastStaleForWidget(freshForecast, now = baseTime.plusMinutes(15))
        )
        assertTrue(
            "Forecast updated 75 minutes ago (> 60m) must be marked stale for widget",
            WeatherGlanceWidget.isForecastStaleForWidget(freshForecast, now = baseTime.plusMinutes(75))
        )
        assertTrue(
            "Forecast with isStale = true flag must always be marked stale for widget",
            WeatherGlanceWidget.isForecastStaleForWidget(freshForecast.copy(isStale = true), now = baseTime.plusMinutes(5))
        )
    }
}
