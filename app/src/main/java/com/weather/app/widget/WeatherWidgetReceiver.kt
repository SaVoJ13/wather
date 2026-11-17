package com.weather.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Реєстратор віджета Jetpack Glance у системі Android з підтримкою автоматичного
 * оновлення кожні 10 хвилин через WeatherWidgetScheduler.
 */
class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeatherGlanceWidget()

    companion object {
        const val ACTION_UPDATE_WIDGET_10_MIN = "com.weather.app.action.UPDATE_WIDGET_10_MIN"
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWidgetScheduler.performBackgroundRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherWidgetScheduler.cancelUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_UPDATE_WIDGET_10_MIN, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                WeatherWidgetScheduler.performBackgroundRefresh(context)
            }
        }
    }
}
