package com.weather.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.glance.appwidget.updateAll
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.FullWeatherForecast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Енергоощадний планувальник оновлень погодного віджета Jetpack Glance (Phase 5 — ADR-016).
 *
 * Відповідно до обмежень ОС Android (Doze Mode, App Standby Buckets):
 * 1. Використовує тип [AlarmManager.ELAPSED_REALTIME] (non-wakeup), який НЕ примушує процесор
 *    прокидатися з глибокого сну, зберігаючи заряд акумулятора.
 * 2. Базовий цільовий інтервал ([UPDATE_INTERVAL_MS]) є рекомендованим орієнтиром для активного стану;
 *    операційна система Android керує групуванням фонових подій за власним енергоощадним графіком,
 *    тому точний 10-хвилинний інтервал у фоні не гарантується і не обіцяється.
 * 3. Миттєве оновлення віджета гарантується при кожному відкритті застосунку, оновленні прогнозу
 *    або зміні активного міста ([updateWidgetNow]).
 * 4. Віджет ніколи не робить фонових GPS-запитів та прямих мережевих викликів.
 */
object WeatherWidgetScheduler {

    const val UPDATE_INTERVAL_MS = 10 * 60 * 1000L // Базовий рекомендований інтервал (10 хв) за дозволом ОС
    const val ALARM_TYPE = AlarmManager.ELAPSED_REALTIME // Non-wakeup для збереження батареї
    const val IS_EXACT_INTERVAL_GUARANTEED = false // ОС Android може відкладати оновлення в режимі Doze
    private const val REQUEST_CODE_10_MIN = 1010
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Планує наступне енергоощадне оновлення віджета за дозволеним системою графіком (non-wakeup).
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

        val safeDelayMs = delayMs.coerceAtLeast(UPDATE_INTERVAL_MS)
        val triggerAt = SystemClock.elapsedRealtime() + safeDelayMs

        alarmManager.setAndAllowWhileIdle(
            ALARM_TYPE,
            triggerAt,
            pendingIntent
        )
    }

    /**
     * Скасовує заплановані оновлення (наприклад, при видаленні останнього екземпляра віджета).
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
     * Оновлює віджет із локального дискового кешу без виконання прямих мережевих HTTP-запитів і без GPS.
     */
    fun performBackgroundRefresh(context: Context) {
        scope.launch {
            try {
                val cachedForecast = WeatherLocalCache.loadCachedForecast(context)
                if (cachedForecast != null) {
                    WeatherRepositoryImpl.lastKnownForecast = cachedForecast
                }
                WeatherGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                // безпечне ігнорування
            } finally {
                // Плануємо наступне енергоощадне оновлення за графіком ОС
                scheduleNextUpdate(context, UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Миттєво оновлює віджет новим реальним прогнозом, отриманим у застосунку, і перезапускає планувальник.
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
