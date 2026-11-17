package com.weather.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weather.app.R
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.MainActivity
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Домашній погодний віджет на базі Jetpack Glance згідно з TASK-007, docs/ux/UX_SPEC.md та ADR-005.
 *
 * Особливості:
 * 1. Адаптивність: підтримує компактний (2x2) та розширений (4x2) режими відображення.
 * 2. Енергоефективність: жодних безперервних анімацій або тикерів (строге дотримання лімітів RemoteViews).
 * 3. Безпека та бізнес-модель: сувора відсутність реклами у віджеті (заборонено політиками Android).
 * 4. Офлайн-стійкість: показує останній закешований прогноз із точною позначкою часу оновлення.
 * 5. Інтерактивність: клік на будь-яку ділянку віджета відкриває MainActivity.
 */
class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        val COMPACT_SIZE = DpSize(120.dp, 100.dp)
        val EXPANDED_SIZE = DpSize(220.dp, 100.dp)

        fun weatherConditionDrawableRes(condition: WeatherCondition): Int {
            return when (condition) {
                WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> R.drawable.ic_widget_sunny
                WeatherCondition.PARTLY_CLOUDY, WeatherCondition.OVERCAST -> R.drawable.ic_widget_cloudy
                WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> R.drawable.ic_widget_rain
                WeatherCondition.SNOW -> R.drawable.ic_widget_snow
                WeatherCondition.THUNDERSTORM -> R.drawable.ic_widget_thunderstorm
                WeatherCondition.FOG -> R.drawable.ic_widget_fog
                WeatherCondition.UNKNOWN -> R.drawable.ic_widget_cloudy
            }
        }
    }

    override val sizeMode = SizeMode.Responsive(setOf(COMPACT_SIZE, EXPANDED_SIZE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Отримуємо останній закешований прогноз локації користувача з дискового кешу, пам'яті або фолбек
        val forecast = com.weather.app.data.local.WeatherLocalCache.loadCachedForecast(context)
            ?: com.weather.app.data.repository.WeatherRepositoryImpl.lastKnownForecast
            ?: MockWeatherRepository.createKyivForecast()

        // Переконуємося, що 10-хвилинне оновлення заплановане
        WeatherWidgetScheduler.scheduleNextUpdate(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            val size = LocalSize.current
            val isExpanded = size.width >= EXPANDED_SIZE.width

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_background))
                    .clickable(actionStartActivity(openAppIntent))
                    .padding(12.dp)
            ) {
                if (isExpanded) {
                    ExpandedWidgetContent(forecast)
                } else {
                    CompactWidgetContent(forecast)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CompactWidgetContent(forecast: FullWeatherForecast) {
        val tempInt = forecast.current.temperatureCelsius.roundToInt()
        val tempString = (if (tempInt > 0) "+$tempInt" else "$tempInt") + "°"
        val today = forecast.daily14Days.firstOrNull()
        val minToday = today?.minTemperatureCelsius?.roundToInt() ?: 0
        val maxToday = today?.maxTemperatureCelsius?.roundToInt() ?: 0
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = forecast.location.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Image(
                    provider = ImageProvider(weatherConditionDrawableRes(forecast.current.condition)),
                    contentDescription = forecast.current.condition.descriptionUk,
                    modifier = GlanceModifier.size(24.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = tempString,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "↑ ${if (maxToday > 0) "+$maxToday" else "$maxToday"}°  ↓ ${if (minToday > 0) "+$minToday" else "$minToday"}°",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFE0E0E0)),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Оновл. $updateTime",
                style = TextStyle(
                    color = ColorProvider(Color(0x99FFFFFF)),
                    fontSize = 9.sp
                )
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun ExpandedWidgetContent(forecast: FullWeatherForecast) {
        val tempInt = forecast.current.temperatureCelsius.roundToInt()
        val tempString = (if (tempInt > 0) "+$tempInt" else "$tempInt") + "°"
        val feelsLikeInt = forecast.current.apparentTemperatureCelsius.roundToInt()
        val feelsLikeString = "Відчувається як " + (if (feelsLikeInt > 0) "+$feelsLikeInt" else "$feelsLikeInt") + "°"
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)
        val hourlyUpcoming = forecast.hourly24h.take(3)

        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Ліва колонка: поточна температура та стан
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = forecast.location.name,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = tempString,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(weatherConditionDrawableRes(forecast.current.condition)),
                        contentDescription = forecast.current.condition.descriptionUk,
                        modifier = GlanceModifier.size(28.dp)
                    )
                }

                Text(
                    text = feelsLikeString,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFD0D0D0)),
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = "Оновлено о $updateTime",
                    style = TextStyle(
                        color = ColorProvider(Color(0x99FFFFFF)),
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Права колонка: найближчі 3 години прогнозу
            Row(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                hourlyUpcoming.forEach { hourItem ->
                    val hourTempInt = hourItem.temperatureCelsius.roundToInt()
                    val hourTempString = (if (hourTempInt > 0) "+$hourTempInt" else "$hourTempInt") + "°"
                    val hourLabel = hourItem.time.format(timeFormatter)

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        Text(
                            text = hourLabel,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFB0BEC5)),
                                fontSize = 10.sp
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Image(
                            provider = ImageProvider(weatherConditionDrawableRes(hourItem.condition)),
                            contentDescription = null,
                            modifier = GlanceModifier.size(20.dp)
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Text(
                            text = hourTempString,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        if (hourItem.precipitationProbabilityPercent > 10) {
                            Text(
                                text = "${hourItem.precipitationProbabilityPercent}%",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF4FC3F7)),
                                    fontSize = 9.sp
                                )
                            )
                        } else {
                            Spacer(modifier = GlanceModifier.height(11.dp))
                        }
                    }
                }
            }
        }
    }
}
