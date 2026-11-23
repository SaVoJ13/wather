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

    @Test
    fun testAlarmTypeIsEnergyEfficientNonWakeup() {
        assertEquals(
            "Widget scheduler must use non-wakeup AlarmManager.ELAPSED_REALTIME to preserve battery in Doze mode",
            android.app.AlarmManager.ELAPSED_REALTIME,
            WeatherWidgetScheduler.ALARM_TYPE
        )
        org.junit.Assert.assertNotEquals(
            "Widget scheduler must NOT use ELAPSED_REALTIME_WAKEUP",
            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
            WeatherWidgetScheduler.ALARM_TYPE
        )
    }

    @Test
    fun testExactIntervalIsNotGuaranteedByOs() {
        org.junit.Assert.assertFalse(
            "Scheduler must honestly report that exact 10-minute intervals are not guaranteed by Android OS power limits",
            WeatherWidgetScheduler.IS_EXACT_INTERVAL_GUARANTEED
        )
    }
}
