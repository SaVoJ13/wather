package com.weather.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.ui.components.DailyForecastList
import com.weather.app.ui.theme.LocalWeatherPalette
import kotlin.math.roundToInt

/**
 * Вкладка 2: «Прогноз» (ForecastScreen) згідно з USER_FLOWS.md та UI_STATE_CONTRACT.md.
 * Єдиний вертикальний скрол:
 * - Загальний огляд діапазону температур на 14–16 днів;
 * - Повний список днів із кольоровими Range Bars;
 * - Відкриття деталей вибраного дня через Bottom Sheet.
 */
@Composable
fun ForecastScreen(
    forecast: FullWeatherForecast,
    onDayClick: (DailyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val scrollState = rememberScrollState()

    val days = forecast.daily14Days
    val minOverall = days.minOfOrNull { it.minTemperatureCelsius }?.roundToInt() ?: 0
    val maxOverall = days.maxOfOrNull { it.maxTemperatureCelsius }?.roundToInt() ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Заголовок та картка макро-тренду на 14-16 днів
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surfaceTint)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.daily_forecast_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Очікуваний діапазон періоду:",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "від ${if (minOverall > 0) "+$minOverall" else "$minOverall"}° до ${if (maxOverall > 0) "+$maxOverall" else "$maxOverall"}°",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                        color = palette.textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список 14-16 днів із графічними індикаторами Range Bars
        DailyForecastList(
            dailyItems = forecast.daily14Days,
            onDayClick = onDayClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(80.dp)) // Відступ під рекламний банер та NavigationBar
    }
}
