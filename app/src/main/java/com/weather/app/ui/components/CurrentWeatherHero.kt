package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.StaleBadgeBackground
import com.weather.app.ui.theme.StaleBadgeText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherHero(
    current: CurrentWeather,
    todayForecast: DailyForecastItem?,
    updatedAt: LocalDateTime,
    isStale: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val tempInt = current.temperatureCelsius.roundToInt()
    val feelsLikeInt = current.apparentTemperatureCelsius.roundToInt()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val updateTimeFormatted = updatedAt.format(timeFormatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surfaceTint)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Погодна іконка та назва стану
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherConditionIcon(
                condition = current.condition,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = current.condition.descriptionUk,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Головна температура (DisplayLarge, 68sp)
        val tempPrefix = if (tempInt > 0) "+" else ""
        Text(
            text = "$tempPrefix$tempInt°",
            style = MaterialTheme.typography.displayLarge,
            color = palette.textColor
        )

        // Відчувається як
        val feelsPrefix = if (feelsLikeInt > 0) "+" else ""
        Text(
            text = "Відчувається як $feelsPrefix$feelsLikeInt°",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textColor.copy(alpha = 0.9f)
        )

        // Діапазон сьогодні (мін / макс)
        if (todayForecast != null) {
            val minToday = todayForecast.minTemperatureCelsius.roundToInt()
            val maxToday = todayForecast.maxTemperatureCelsius.roundToInt()
            val maxPrefix = if (maxToday > 0) "+" else ""
            val minPrefix = if (minToday > 0) "+" else ""
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "↑ $maxPrefix$maxToday°   ↓ $minPrefix$minToday°",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textColor.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Індикатор застарілості даних (Stale Data Badge) або час оновлення
        if (isStale) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StaleBadgeBackground)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Увага, дані застарілі",
                    tint = StaleBadgeText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Дані від $updateTimeFormatted (можуть бути застарілі)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = StaleBadgeText
                )
            }
        } else {
            Text(
                text = "Оновлено о $updateTimeFormatted",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = palette.textColor.copy(alpha = 0.7f)
            )
        }
    }
}
