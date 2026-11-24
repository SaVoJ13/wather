package com.weather.app.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.weather.app.R

/**
 * Три затверджені навігаційні розділи нижньої панелі відповідно до USER_FLOWS.md та Sapphire Atmospheric Glass v2 (ADR-019):
 * 1. Зараз (Now) — поточні умови, погодинний ряд, ключові метрики, сонце.
 * 2. Прогноз (Forecast) — коротка назва вкладки «Прогноз» (до 16 днів щоденного прогнозу, графік тренду, деталі дня).
 * 3. Місця (Locations) — коротка назва вкладки «Місця» (ілюстровані картки збережених міст, пошук, безпечний GPS).
 */
enum class WeatherNavTab(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val contentDescription: String
) {
    NOW(
        titleRes = R.string.now,
        icon = Icons.Default.WbSunny,
        contentDescription = "Вкладка Зараз: поточні погодні умови та погодинна стрічка"
    ),
    FORECAST(
        titleRes = R.string.nav_forecast,
        icon = Icons.Default.CalendarMonth,
        contentDescription = "Вкладка Прогноз: щоденний прогноз та графік тренду"
    ),
    LOCATIONS(
        titleRes = R.string.nav_locations,
        icon = Icons.Default.LocationOn,
        contentDescription = "Вкладка Місця: управління збереженими містами та пошук"
    )
}
