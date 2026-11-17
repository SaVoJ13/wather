package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.ui.theme.RainAccent
import com.weather.app.ui.theme.SunAccent
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsBottomSheet(
    item: DailyForecastItem?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ukLocale = Locale("uk", "UA")
    val titleFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", ukLocale)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val formattedDate = item.date.format(titleFormatter).replaceFirstChar { it.uppercase(ukLocale) }
    val minTempInt = item.minTemperatureCelsius.roundToInt()
    val maxTempInt = item.maxTemperatureCelsius.roundToInt()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            // Заголовок з кнопкою закриття
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрити деталі дня"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Головний блок стану
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeatherConditionIcon(
                    condition = item.condition,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = item.condition.descriptionUk,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Вдень до " + (if (maxTempInt > 0) "+" else "") +
                                "°, вночі від " + (if (minTempInt > 0) "+" else "") + "°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Метрики опадів та вітру
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = RainAccent,
                    label = "Опади",
                    value = " мм",
                    extra = "Ймовірність %",
                    modifier = Modifier.weight(1f)
                )

                DetailMetricCard(
                    icon = Icons.Default.Air,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "Вітер",
                    value = "до  км/год",
                    extra = "Макс. пориви",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Схід і захід сонця
            if (item.sunrise != null && item.sunset != null) {
                val sunriseFormatted = item.sunrise.format(timeFormatter)
                val sunsetFormatted = item.sunset.format(timeFormatter)
                val dayDuration = Duration.between(item.sunrise, item.sunset)
                val hours = dayDuration.toHours()
                val minutes = dayDuration.minusHours(hours).toMinutes()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = SunAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Схід сонця",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = sunriseFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Світловий день",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "г хв",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbTwilight,
                            contentDescription = null,
                            tint = SunAccent.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Захід сонця",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = sunsetFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailMetricCard(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    extra: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = extra,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}
