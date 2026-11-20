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
}
