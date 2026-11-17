package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Елемент погодинного прогнозу (до 24 годин).
 */
data class HourlyForecastItem(
    val time: LocalDateTime,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int
)
