package com.weather.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.ui.components.DailyForecastList
import com.weather.app.ui.components.DailyTemperatureTrendChart

/**
 * Вкладка 2: «Прогноз» (ForecastScreen — Sapphire Atmospheric Glass v2, Макет 03).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 2, 5, 7, 9):
 * - Верхня скляна картка з інтерактивним дволінійним графіком тренду Макс/Мін [DailyTemperatureTrendChart];
 * - Повний список днів [DailyForecastList] для фактичного горизонту прогнозу до 16 днів (без обрізання до 14);
 * - Відкриття деталей вибраного дня у [DayDetailsBottomSheet].
 */
@Composable
fun ForecastScreen(
    forecast: FullWeatherForecast,
    onDayClick: (DailyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val days = forecast.daily14Days

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Інтерактивний дволінійний графік температурного тренду Макс/Мін (до 16 днів)
        DailyTemperatureTrendChart(
            dailyItems = days,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Повний список щоденного прогнозу (до 16 днів) з 3D-ілюстраціями та Range Bars
        DailyForecastList(
            dailyItems = days,
            onDayClick = onDayClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(80.dp)) // Відступ під рекламний банер та NavigationBar
    }
}
