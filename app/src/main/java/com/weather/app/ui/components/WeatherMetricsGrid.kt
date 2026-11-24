package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.ui.theme.LocalWeatherUnits
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Сітка погодних показників 2×2 зі скляними картками та візуальними міні-шкалами
 * (Sapphire Atmospheric Glass v2 — Макет 01 & 02).
 */
@Composable
fun WeatherMetricsGrid(
    current: CurrentWeather,
    modifier: Modifier = Modifier
) {
    val units = LocalWeatherUnits.current
    val noDataStr = stringResource(R.string.no_data)

    val humidityComfort = when {
        current.relativeHumidityPercent == null -> noDataStr
        current.relativeHumidityPercent < 35 -> stringResource(R.string.humidity_dry)
        current.relativeHumidityPercent in 35..65 -> stringResource(R.string.humidity_comfortable)
        else -> stringResource(R.string.humidity_humid)
    }

    val uvLevel = when (current.uvIndex?.roundToInt()) {
        null -> noDataStr
        in 0..2 -> stringResource(R.string.uv_low)
        in 3..5 -> stringResource(R.string.uv_moderate)
        in 6..7 -> stringResource(R.string.uv_high)
        in 8..10 -> stringResource(R.string.uv_very_high)
        else -> stringResource(R.string.uv_extreme)
    }

    val humidityValue = current.relativeHumidityPercent?.let { "$it%" } ?: noDataStr
    val humidityProgress = current.relativeHumidityPercent?.let { (it / 100f).coerceIn(0.05f, 1f) } ?: 0f

    val windValue = current.windSpeedKmh?.let {
        units.formatWindSpeed(
            kmh = it,
            unitLabelKmh = stringResource(R.string.wind_unit_kmh),
            unitLabelMs = stringResource(R.string.wind_unit_ms)
        )
    } ?: noDataStr
    val windProgress = current.windSpeedKmh?.let { (it.toFloat() / 50f).coerceIn(0.08f, 1f) } ?: 0f

    val hpaLabel = stringResource(R.string.pressure_unit_hpa)
    val mmhgLabel = stringResource(R.string.pressure_unit_mmhg)
    val pressureValue = current.surfacePressureHpa?.let {
        units.formatPressurePrimary(it, unitLabelHpa = hpaLabel, unitLabelMmHg = mmhgLabel)
    } ?: noDataStr
    val pressureSubtitle = current.surfacePressureHpa?.let {
        units.formatPressureSecondary(it, unitLabelHpa = hpaLabel, unitLabelMmHg = mmhgLabel)
    } ?: noDataStr
    val pressureProgress = current.surfacePressureHpa?.let {
        (((it - 980.0) / 60.0).toFloat()).coerceIn(0.1f, 1f)
    } ?: 0f

    val uvValue = current.uvIndex?.let {
        "${String.format(Locale.getDefault(), "%.0f", it)} $uvLevel"
    } ?: noDataStr
    val uvProgress = current.uvIndex?.let { (it.toFloat() / 11f).coerceIn(0.06f, 1f) } ?: 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VolumetricMetricCard(
                title = stringResource(R.string.metric_humidity).uppercase(),
                value = humidityValue,
                subtitle = humidityComfort,
                progress = humidityProgress,
                barColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            VolumetricMetricCard(
                title = stringResource(R.string.metric_wind).uppercase(),
                value = windValue,
                subtitle = stringResource(R.string.wind_speed_label),
                progress = windProgress,
                barColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VolumetricMetricCard(
                title = stringResource(R.string.metric_pressure).uppercase(),
                value = pressureValue,
                subtitle = pressureSubtitle,
                progress = pressureProgress,
                barColor = Color(0xFF4ADE80),
                modifier = Modifier.weight(1f)
            )
            VolumetricMetricCard(
                title = stringResource(R.string.metric_uv).uppercase(),
                value = uvValue,
                subtitle = uvLevel,
                progress = uvProgress,
                barColor = Color(0xFFFACC15),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VolumetricMetricCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        cornerRadius = 24.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF93C5FD),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Візуальна шкала-індикатор (Progress Gauge Bar)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
            ) {
                val r = size.height / 2f
                drawRoundRect(
                    color = Color(0xDC0A142C),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
                if (progress > 0f) {
                    val fillW = (size.width * progress).coerceAtLeast(size.height)
                    drawRoundRect(
                        color = barColor,
                        size = Size(fillW, size.height),
                        cornerRadius = CornerRadius(r, r)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
