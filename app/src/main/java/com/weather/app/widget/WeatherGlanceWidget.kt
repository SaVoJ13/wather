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
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.MainActivity
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Домашній погодний віджет на базі Jetpack Glance (Phase 5 — Widgets & Animation).
 *
 * Особливості:
 * 1. Адаптивність: підтримує компактний (2x2) та розширений (4x2) режими відображення.
 * 2. Чесність даних (Data Integrity): працює виключно з реальними погодними даними та офлайн-кешем;
 *    ніколи не використовує MockWeatherRepository та не підставляє вигадані значення (0° тощо).
 * 3. Енергоефективність: жодних безперервних анімацій, фонового GPS або примусових пробуджень CPU.
 * 4. Безпека та бізнес-модель: сувора відсутність реклами у віджеті (0% реклами).
 * 5. Інтерактивність: клік на будь-яку ділянку віджета відкриває MainActivity.
 */
class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        val COMPACT_SIZE = DpSize(120.dp, 100.dp)
        val EXPANDED_SIZE = DpSize(220.dp, 100.dp)
        const val STALE_THRESHOLD_MINUTES = 60L

        fun isExpandedMode(size: DpSize): Boolean {
            return size.width >= EXPANDED_SIZE.width
        }

        /**
         * Повертає реальний збережений прогноз із дискового кешу або оперативної пам'яті.
         * Ніколи не повертає тестові/вигадані дані MockWeatherRepository.
         */
        fun resolveWidgetForecast(
            cachedForecast: FullWeatherForecast?,
            lastKnownForecast: FullWeatherForecast?
        ): FullWeatherForecast? {
            return cachedForecast ?: lastKnownForecast
        }

        /**
         * Перевіряє, чи є прогноз у віджеті застарілим (прапорець isStale або вік > 60 хвилин).
         */
        fun isForecastStaleForWidget(
            forecast: FullWeatherForecast,
            now: LocalDateTime = LocalDateTime.now()
        ): Boolean {
            if (forecast.isStale) return true
            val ageMinutes = Duration.between(forecast.updatedAt, now).toMinutes()
            return ageMinutes > STALE_THRESHOLD_MINUTES
        }

        /**
         * Форматує денний діапазон температур (↑max° / ↓min°) або повертає null,
         * якщо щоденний прогноз відсутній (без підстановки фіктивних 0°).
         */
        fun formatTodayRange(forecast: FullWeatherForecast): String? {
            val today = forecast.daily14Days.firstOrNull() ?: return null
            val minToday = today.minTemperatureCelsius.roundToInt()
            val maxToday = today.maxTemperatureCelsius.roundToInt()
            return "↑${maxToday}° / ↓${minToday}°"
        }

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
        // Отримуємо реальний закешований прогноз локації користувача з дискового кешу або пам'яті (без Mock-фолбеку)
        val forecast = resolveWidgetForecast(
            cachedForecast = com.weather.app.data.local.WeatherLocalCache.loadCachedForecast(context),
            lastKnownForecast = com.weather.app.data.repository.WeatherRepositoryImpl.lastKnownForecast
        )

        // Плануємо наступне енергоощадне оновлення згідно з обмеженнями ОС Android
        WeatherWidgetScheduler.scheduleNextUpdate(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            val size = LocalSize.current
            val isExpanded = isExpandedMode(size)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_background))
                    .clickable(actionStartActivity(openAppIntent))
                    .padding(12.dp)
            ) {
                if (forecast == null) {
                    EmptyWidgetContent(isExpanded = isExpanded)
                } else if (isExpanded) {
                    ExpandedWidgetContent(forecast)
                } else {
                    CompactWidgetContent(forecast)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun EmptyWidgetContent(isExpanded: Boolean) {
        val rawContext = LocalContext.current
        val context = getLocalizedContext(rawContext)
        val appName = context.getString(R.string.app_name)
        val noDataText = context.getString(R.string.no_data)
        val selectCityText = context.getString(R.string.city_selection)

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_partly_cloudy),
                contentDescription = noDataText,
                modifier = GlanceModifier.size(if (isExpanded) 36.dp else 28.dp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = appName,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = if (isExpanded) 15.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = noDataText,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFCBD5E1)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            if (isExpanded) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = selectCityText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CompactWidgetContent(forecast: FullWeatherForecast) {
        val rawContext = LocalContext.current
        val context = getLocalizedContext(rawContext)
        val tempInt = forecast.current.temperatureCelsius.roundToInt()
        val tempString = "$tempInt°"
        val rangeText = formatTodayRange(forecast)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)
        val isStale = isForecastStaleForWidget(forecast)
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

            if (rangeText != null) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = rangeText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            val updatedLabel = context.getString(R.string.widget_updated_short, updateTime)
            Text(
                text = if (isStale) "$updatedLabel • ⏳" else updatedLabel,
                style = TextStyle(
                    color = ColorProvider(if (isStale) Color(0xFFFBBF24) else Color(0x9994A3B8)),
                    fontSize = 9.sp
                ),
                maxLines = 1
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
        val feelsLikeLabel = if (forecast.current.apparentTemperatureCelsius != null) {
            val feelsLikeInt = forecast.current.apparentTemperatureCelsius.roundToInt()
            context.getString(R.string.feels_like, "$feelsLikeInt°")
        } else null
        val rangeText = formatTodayRange(forecast)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val updateTime = forecast.updatedAt.format(timeFormatter)
        val isStale = isForecastStaleForWidget(forecast)
        val dailyItems = forecast.daily14Days.take(4)
        val conditionDesc = context.getString(forecast.current.condition.stringRes)
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

                val subtitleText = listOfNotNull(feelsLikeLabel, rangeText).joinToString(" • ")
                if (subtitleText.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF94A3B8)),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                val humidityPart = forecast.current.relativeHumidityPercent?.let { "💧 $it%" }
                val windPart = forecast.current.windSpeedKmh?.let { "💨 ${it.roundToInt()} $windUnit" }
                val metricsText = listOfNotNull(humidityPart, windPart).joinToString("  ")
                if (metricsText.isNotEmpty()) {
                    Text(
                        text = metricsText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFCBD5E1)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }

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
                    val locationAndTime = if (isStale) {
                        "${forecast.location.name} • $updateTime • ⏳"
                    } else {
                        "${forecast.location.name} • $updateTime"
                    }
                    Text(
                        text = locationAndTime,
                        style = TextStyle(
                            color = ColorProvider(if (isStale) Color(0xFFFBBF24) else Color(0xFF94A3B8)),
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
                            if (pop != null && pop > 15) {
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
