package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Поточні погодні умови на локації.
 * Відповідно до вимог достовірності метеоданих (TASK 1):
 * Показники, які можуть бути відсутні у відповіді метеосервісу, є nullable (Double?, Int?).
 * Заборонено підставляти вигадані значення (наприклад, 1013.25 гПа, UV 0.0 або 50%).
 */
data class CurrentWeather(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double? = null,
    val condition: WeatherCondition,
    val relativeHumidityPercent: Int? = null,
    val windSpeedKmh: Double? = null,
    val surfacePressureHpa: Double? = null,
    val uvIndex: Double? = null,
    val time: LocalDateTime
)
