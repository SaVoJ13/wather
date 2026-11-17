package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Повний погодний прогноз для вибраної локації.
 * Єдине джерело правди для UI та Widget.
 */
data class FullWeatherForecast(
    val location: LocationModel,
    val current: CurrentWeather,
    val hourly24h: List<HourlyForecastItem>,
    val daily14Days: List<DailyForecastItem>,
    val updatedAt: LocalDateTime,
    val isStale: Boolean = false
)
