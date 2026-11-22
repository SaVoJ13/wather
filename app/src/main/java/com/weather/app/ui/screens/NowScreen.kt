package com.weather.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weather.app.R
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.WeatherDataSource
import com.weather.app.ui.components.CurrentWeatherHero
import com.weather.app.ui.components.HourlyForecastRow
import com.weather.app.ui.components.SunriseSunsetCard
import com.weather.app.ui.components.WeatherMetricsGrid
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Вкладка 1: «Зараз» (NowScreen) згідно з UI_STATE_CONTRACT.md та USER_FLOWS.md.
 * Містить Hero поточної погоди, 24-годинну стрічку від поточної години,
 * сітку метрик 2x2, схід і захід сонця на Canvas та правову плашку джерела даних.
 */
@Composable
fun NowScreen(
    forecast: FullWeatherForecast,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Головний блок поточної погоди (Hero)
        CurrentWeatherHero(
            current = forecast.current,
            todayForecast = forecast.daily14Days.firstOrNull(),
            updatedAt = forecast.updatedAt,
            isStale = forecast.isStale,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 24-годинна стрічка прогнозу від поточної години
        HourlyForecastRow(
            hourlyItems = forecast.hourly24h,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ключові метрики (вологість, вітер, тиск, УФ)
        WeatherMetricsGrid(
            current = forecast.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Схід і захід сонця на дузі Canvas
        SunriseSunsetCard(
            todayForecast = forecast.daily14Days.firstOrNull(),
            currentTime = forecast.current.time,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Динамічна правова атрибуція джерела погодних даних
        val attributionText = when (forecast.dataSource) {
            WeatherDataSource.OPEN_METEO -> stringResource(R.string.source_open_meteo)
            WeatherDataSource.WEATHER_METRO -> stringResource(R.string.source_weather_metro)
            WeatherDataSource.VISUAL_CROSSING -> stringResource(R.string.source_visual_crossing)
            WeatherDataSource.MOCK -> stringResource(R.string.source_mock)
        }
        Text(
            text = attributionText,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(80.dp)) // Відступ під рекламний банер та NavigationBar
    }
}
