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
import androidx.glance.LocalContext
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
import java.util.Locale
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
                WeatherCondition.CLEAR_SKY -> R.drawable.ic_widget_sunny
                WeatherCondition.MAINLY_CLEAR, WeatherCondition.PARTLY_CLOUDY -> R.drawable.ic_widget_partly_cloudy
                WeatherCondition.OVERCAST -> R.drawable.ic_widget_cloudy
                WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> R.drawable.ic_widget_rain
                WeatherCondition.SNOW -> R.drawable.ic_widget_snow
                WeatherCondition.THUNDERSTORM -> R.drawable.ic_widget_thunderstorm
                WeatherCondition.FOG -> R.drawable.ic_widget_fog
                WeatherCondition.UNKNOWN -> R.drawable.ic_widget_cloudy
            }
        }

        fun formatDayOfWeekUk(dayOfWeek: java.time.DayOfWeek): String {
            return when (dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "пн"
                java.time.DayOfWeek.TUESDAY -> "вт"
                java.time.DayOfWeek.WEDNESDAY -> "ср"
                java.time.DayOfWeek.THURSDAY -> "чт"
                java.time.DayOfWeek.FRIDAY -> "пт"
                java.time.DayOfWeek.SATURDAY -> "сб"
                java.time.DayOfWeek.SUNDAY -> "нд"
            }
        }

        fun formatDayOfWeekPl(dayOfWeek: java.time.DayOfWeek): String {
            return when (dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "pn"
                java.time.DayOfWeek.TUESDAY -> "wt"
                java.time.DayOfWeek.WEDNESDAY -> "śr"
                java.time.DayOfWeek.THURSDAY -> "cz"
                java.time.DayOfWeek.FRIDAY -> "pt"
                java.time.DayOfWeek.SATURDAY -> "sb"
                java.time.DayOfWeek.SUNDAY -> "nd"
            }
        }

        fun formatDayOfWeek(dayOfWeek: java.time.DayOfWeek, locale: Locale = Locale.getDefault()): String {
            return when (locale.language) {
                "uk" -> formatDayOfWeekUk(dayOfWeek)
                "pl" -> formatDayOfWeekPl(dayOfWeek)
                else -> dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, locale)
                    .lowercase(locale)
                    .take(2)
            }
        }

        fun getLocalizedContext(context: Context): Context {
            val appLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            if (!appLocales.isEmpty) {
                val locale = appLocales[0]
                if (locale != null) {
                    val config = android.content.res.Configuration(context.resources.configuration)
                    config.setLocale(locale)
                    return context.createConfigurationContext(config)
                }
            }
            return context
        }

        fun getCurrentLocale(): Locale {
            val appLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            return if (!appLocales.isEmpty) {
                appLocales[0] ?: Locale.getDefault()
            } else {
                Locale.getDefault()
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
        val rawContext = LocalContext.current
        val context = getLocalizedContext(rawContext)
        val tempInt = forecast.current.temperatureCelsius.roundToInt()
        val tempString = "$tempInt°"
        val today = forecast.daily14Days.firstOrNull()
        val minToday = today?.minTemperatureCelsius?.roundToInt() ?: 0
        val maxToday = today?.maxTemperatureCelsius?.roundToInt() ?: 0
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)
        val conditionDesc = context.getString(forecast.current.condition.stringRes)

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_location),
                    contentDescription = null,
                    modifier = GlanceModifier.size(13.dp)
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                Text(
                    text = forecast.location.name,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCBD5E1)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                Image(
                    provider = ImageProvider(weatherConditionDrawableRes(forecast.current.condition)),
                    contentDescription = conditionDesc,
                    modifier = GlanceModifier.size(24.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = tempString,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = conditionDesc,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFF1F5F9)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "↑${maxToday}° / ↓${minToday}°",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF94A3B8)),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = context.getString(R.string.widget_updated_short, updateTime),
                style = TextStyle(
                    color = ColorProvider(Color(0x9994A3B8)),
                    fontSize = 9.sp
                )
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun ExpandedWidgetContent(forecast: FullWeatherForecast) {
        val rawContext = LocalContext.current
        val context = getLocalizedContext(rawContext)
        val currentLocale = getCurrentLocale()
        val tempInt = forecast.current.temperatureCelsius.roundToInt()
        val tempString = "$tempInt°"
        val feelsLikeInt = forecast.current.apparentTemperatureCelsius.roundToInt()
        val feelsLikeString = "$feelsLikeInt°"
        val today = forecast.daily14Days.firstOrNull()
        val minToday = today?.minTemperatureCelsius?.roundToInt() ?: 0
        val maxToday = today?.maxTemperatureCelsius?.roundToInt() ?: 0
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)
        val dailyItems = forecast.daily14Days.take(4)
        val conditionDesc = context.getString(forecast.current.condition.stringRes)
        val feelsLikeLabel = context.getString(R.string.feels_like, feelsLikeString)
        val windUnit = context.getString(R.string.wind_unit_kmh)

        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Ліва колонка (~48% ширини): велика іконка, температура, опис, діапазон/відчуття, метрики, локація
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(weatherConditionDrawableRes(forecast.current.condition)),
                    contentDescription = conditionDesc,
                    modifier = GlanceModifier.size(48.dp)
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = tempString,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = conditionDesc,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF1F5F9)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = "$feelsLikeLabel • ↑${maxToday}° / ↓${minToday}°",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = "💧 ${forecast.current.relativeHumidityPercent}%  💨 ${forecast.current.windSpeedKmh.roundToInt()} $windUnit",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCBD5E1)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_location),
                        contentDescription = null,
                        modifier = GlanceModifier.size(12.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(3.dp))
                    Text(
                        text = "${forecast.location.name} • $updateTime",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF94A3B8)),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Права колонка (~52% ширини): щоденний прогноз на 4 дні
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                dailyItems.forEachIndexed { index, item ->
                    val max = item.maxTemperatureCelsius.roundToInt()
                    val min = item.minTemperatureCelsius.roundToInt()
                    val dayLabel = formatDayOfWeek(item.date.dayOfWeek, currentLocale)
                    val pop = item.precipitationProbabilityPercent
                    val itemConditionDesc = context.getString(item.condition.stringRes)

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = dayLabel,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFCBD5E1)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier.width(26.dp)
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Row(
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Image(
                                provider = ImageProvider(weatherConditionDrawableRes(item.condition)),
                                contentDescription = itemConditionDesc,
                                modifier = GlanceModifier.size(22.dp)
                            )
                            if (pop > 15) {
                                Spacer(modifier = GlanceModifier.width(3.dp))
                                Text(
                                    text = "💧 $pop%",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF38BDF8)),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        Row(
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "${max}°",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = "${min}°",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF94A3B8)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }

                    if (index < dailyItems.size - 1) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ColorProvider(Color(0x1FFFFFFF)))
                        ) {}
                    }
                }
            }
        }
    }
}
