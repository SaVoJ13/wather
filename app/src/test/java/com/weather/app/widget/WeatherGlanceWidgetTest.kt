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
        assertEquals(R.drawable.ic_widget_cloudy, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.OVERCAST))
        assertEquals(R.drawable.ic_widget_rain, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.RAIN))
        assertEquals(R.drawable.ic_widget_snow, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.SNOW))
        assertEquals(R.drawable.ic_widget_thunderstorm, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.THUNDERSTORM))
        assertEquals(R.drawable.ic_widget_fog, WeatherGlanceWidget.weatherConditionDrawableRes(WeatherCondition.FOG))
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
}
