package com.weather.app.widget

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тести для [WeatherWidgetScheduler]:
 * Перевіряє інтервали оновлення та налаштування віджета.
 */
class WeatherWidgetSchedulerTest {

    @Test
    fun testUpdateIntervalIsExactlyTenMinutes() {
        val tenMinutesMs = 10 * 60 * 1000L
        assertEquals("Update interval must be exactly 10 minutes (600 000 ms)", tenMinutesMs, WeatherWidgetScheduler.UPDATE_INTERVAL_MS)
    }

    @Test
    fun testWidgetReceiverActionConstant() {
        assertEquals(
            "com.weather.app.action.UPDATE_WIDGET_10_MIN",
            WeatherWidgetReceiver.ACTION_UPDATE_WIDGET_10_MIN
        )
    }
}
