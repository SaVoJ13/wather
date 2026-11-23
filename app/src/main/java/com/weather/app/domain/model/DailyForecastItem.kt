package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Елемент щоденного прогнозу (до 14-16 днів).
 * Відповідно до вимог достовірності метеоданих (TASK 1):
 * Показники, які можуть бути відсутні у відповіді API, є nullable.
 * Реальні нульові показники (наприклад, 0.0 мм опадів) зберігаються як 0.0,
 * а відсутні показники позначаються як null.
 */
data class DailyForecastItem(
    val date: LocalDateTime,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int? = null,
    val precipitationSumMm: Double? = null,
    val maxWindSpeedKmh: Double? = null,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null
)
