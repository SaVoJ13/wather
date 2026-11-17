package com.weather.app.data.remote.dto

/**
 * Типізовані структури відповіді Open-Meteo Weather Forecast API.
 * 
 * Першоджерело: https://open-meteo.com/en/docs
 * Ліцензія джерела: CC BY 4.0 (лише некомерційне використання для прототипу/тестів;
 * для комерційного релізу з рекламою згідно з ADR-005 потрібна комерційна підписка або інший провайдер).
 */

data class OpenMeteoForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double? = null,
    val utc_offset_seconds: Int = 0,
    val timezone: String? = null,
    val timezone_abbreviation: String? = null,
    val elevation: Double? = null,
    val current: OpenMeteoCurrentDto? = null,
    val hourly: OpenMeteoHourlyDto? = null,
    val daily: OpenMeteoDailyDto? = null
)

data class OpenMeteoCurrentDto(
    val time: String,
    val interval: Int? = null,
    val temperature_2m: Double,
    val apparent_temperature: Double? = null,
    val relative_humidity_2m: Int? = null,
    val weather_code: Int,
    val surface_pressure: Double? = null,
    val wind_speed_10m: Double? = null,
    val uv_index: Double? = null
)

data class OpenMeteoHourlyDto(
    val time: List<String> = emptyList(),
    val temperature_2m: List<Double> = emptyList(),
    val relative_humidity_2m: List<Int>? = null,
    val apparent_temperature: List<Double>? = null,
    val precipitation_probability: List<Int>? = null,
    val weather_code: List<Int> = emptyList(),
    val surface_pressure: List<Double>? = null,
    val wind_speed_10m: List<Double>? = null,
    val uv_index: List<Double>? = null
)

data class OpenMeteoDailyDto(
    val time: List<String> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList(),
    val apparent_temperature_max: List<Double>? = null,
    val apparent_temperature_min: List<Double>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    val uv_index_max: List<Double>? = null,
    val precipitation_sum: List<Double>? = null,
    val precipitation_probability_max: List<Int>? = null,
    val wind_speed_10m_max: List<Double>? = null
)
