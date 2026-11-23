package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Елемент погодинного прогнозу (до 24 годин).
 * precipitationProbabilityPercent є nullable: null означає відсутність даних від API,
 * тоді як 0 означає фактичну нульову ймовірність опадів.
 */
data class HourlyForecastItem(
    val time: LocalDateTime,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int? = null
)
