package com.weather.app.domain.model

import java.time.LocalDateTime

/**
 * Поточні погодні умови на локації.
 */
data class CurrentWeather(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val relativeHumidityPercent: Int,
    val windSpeedKmh: Double,
    val surfacePressureHpa: Double,
    val uvIndex: Double,
    val time: LocalDateTime
)
