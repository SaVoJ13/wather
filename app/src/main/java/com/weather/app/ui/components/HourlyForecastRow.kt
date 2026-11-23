package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weather.app.R
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.RainAccent
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun HourlyForecastRow(
    hourlyItems: List<HourlyForecastItem>,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surfaceTint)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.hourly_forecast_title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.textColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(hourlyItems) { index, item ->
                val timeLabel = if (index == 0) stringResource(R.string.now) else item.time.format(timeFormatter)
                val tempInt = item.temperatureCelsius.roundToInt()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(52.dp)
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textColor.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    WeatherConditionIcon(
                        condition = item.condition,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val tempPrefix = if (tempInt > 0) "+" else ""
                    Text(
                        text = "$tempPrefix$tempInt°",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Показуємо відсоток опадів лише якщо він більше 10%
                    if (item.precipitationProbabilityPercent != null && item.precipitationProbabilityPercent > 10) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = stringResource(R.string.precipitation_probability_desc),
                                tint = RainAccent,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${item.precipitationProbabilityPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = RainAccent
                            )
                        }
                    } else {
                        // Порожнє місце однакової висоти для збереження вирівнювання
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
