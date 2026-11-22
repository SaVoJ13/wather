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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weather.app.R
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.ui.theme.LocalWeatherPalette
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherMetricsGrid(
    current: CurrentWeather,
    modifier: Modifier = Modifier
) {
    val humidityComfort = when {
        current.relativeHumidityPercent < 35 -> stringResource(R.string.humidity_dry)
        current.relativeHumidityPercent in 35..65 -> stringResource(R.string.humidity_comfortable)
        else -> stringResource(R.string.humidity_humid)
    }

    val uvLevel = when (current.uvIndex.roundToInt()) {
        in 0..2 -> stringResource(R.string.uv_low)
        in 3..5 -> stringResource(R.string.uv_moderate)
        in 6..7 -> stringResource(R.string.uv_high)
        in 8..10 -> stringResource(R.string.uv_very_high)
        else -> stringResource(R.string.uv_extreme)
    }

    val pressureMmHg = (current.surfacePressureHpa * 0.750062).roundToInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.WaterDrop,
                title = stringResource(R.string.metric_humidity),
                value = "${current.relativeHumidityPercent}%",
                subtitle = humidityComfort,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Default.Air,
                title = stringResource(R.string.metric_wind),
                value = "${current.windSpeedKmh.roundToInt()} ${stringResource(R.string.wind_unit_kmh)}",
                subtitle = stringResource(R.string.wind_speed_label),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Compress,
                title = stringResource(R.string.metric_pressure),
                value = "${current.surfacePressureHpa.roundToInt()} ${stringResource(R.string.pressure_unit_hpa)}",
                subtitle = "$pressureMmHg ${stringResource(R.string.pressure_unit_mmhg)}",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Default.WbSunny,
                title = stringResource(R.string.metric_uv),
                value = String.format(Locale.getDefault(), "%.1f", current.uvIndex),
                subtitle = uvLevel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceTint)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textColor.copy(alpha = 0.75f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textColor.copy(alpha = 0.75f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = palette.textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textColor.copy(alpha = 0.8f)
        )
    }
}
