package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Елемент щоденного прогнозу (до 14 днів).
 */
data class DailyForecastItem(
    val date: LocalDateTime,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int,
    val precipitationSumMm: Double,
    val maxWindSpeedKmh: Double,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null
)
