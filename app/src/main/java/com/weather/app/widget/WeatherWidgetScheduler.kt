package com.weather.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.glance.appwidget.updateAll
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Планувальник оновлень погодного віджета кожні 10 хвилин згідно з побажанням Власника.
 * Використовує AlarmManager із PendingIntent для пробудження та оновлення даних віджета
 * незалежно від того, чи відкритий головний екран застосунку.
 */
object WeatherWidgetScheduler {

    const val UPDATE_INTERVAL_MS = 10 * 60 * 1000L // 10 хвилин
    private const val REQUEST_CODE_10_MIN = 1010
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Планує наступне оновлення віджета через 10 хвилин.
     */
    fun scheduleNextUpdate(context: Context, delayMs: Long = UPDATE_INTERVAL_MS) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = WeatherWidgetReceiver.ACTION_UPDATE_WIDGET_10_MIN
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_10_MIN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = SystemClock.elapsedRealtime() + delayMs

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    /**
     * Скасовує заплановані оновлення (наприклад, при видаленні останнього віджета).
     */
    fun cancelUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = WeatherWidgetReceiver.ACTION_UPDATE_WIDGET_10_MIN
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_10_MIN,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Оновлює віджет із локального дискового кешу без виконання прямих мережевих HTTP-запитів.
     * Відповідно до WIDGET_AD_CONTRACTS.md, віджет Glance ніколи не робить запитів до API самостійно.
     */
    fun performBackgroundRefresh(context: Context) {
        scope.launch {
            try {
                val cachedForecast = WeatherLocalCache.loadCachedForecast(context)
                if (cachedForecast != null) {
                    WeatherRepositoryImpl.lastKnownForecast = cachedForecast
                    WeatherGlanceWidget().updateAll(context)
                }
            } catch (e: Exception) {
                // безпечне ігнорування
            } finally {
                // У будь-якому випадку плануємо наступне оновлення через 10 хвилин
                scheduleNextUpdate(context, UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Миттєво оновлює віджет новим прогнозом, отриманим у застосунку, і перезапускає 10-хвилинний таймер.
     */
    fun updateWidgetNow(context: Context, forecast: FullWeatherForecast) {
        scope.launch {
            try {
                WeatherLocalCache.saveForecast(context, forecast)
                WeatherRepositoryImpl.lastKnownForecast = forecast
                WeatherGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                // безпечне ігнорування
            } finally {
                scheduleNextUpdate(context, UPDATE_INTERVAL_MS)
            }
        }
    }
}
