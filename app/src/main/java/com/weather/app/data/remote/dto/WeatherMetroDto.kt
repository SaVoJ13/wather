package com.weather.app.data.remote.dto

/**
 * Типізовані DTO структури відповіді WeatherMetro Forecast API v2.
 * 
 * Першоджерело: https://api.weathermetro.com/v2/forecast
 * Дозволено для комерційного використання з обов'язковою атрибуцією: "Weather data by WeatherMetro".
 * 16 днів щоденного та 384 години погодинного прогнозу, до 100 000 запитів/міс за $0.
 */

data class WeatherMetroResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtimeMs: Double? = null,
    val utcOffsetSeconds: Int = 0,
    val timezone: String? = null,
    val timezoneAbbreviation: String? = null,
    val hourly: WeatherMetroHourlyDto? = null,
    val daily: WeatherMetroDailyDto? = null
)

data class WeatherMetroHourlyDto(
    val time: List<String> = emptyList(),
    val temperature2m: List<Double> = emptyList(),
    val apparentTemperature: List<Double?> = emptyList(),
    val precipitationProbability: List<Int?> = emptyList(),
    val weathercode: List<Int> = emptyList(),
    val relativeHumidity2m: List<Int?> = emptyList(),
    val windSpeed10m: List<Double?> = emptyList(),
    val surfacePressure: List<Double?> = emptyList(),
    val uvIndex: List<Double?> = emptyList()
)

data class WeatherMetroDailyDto(
    val time: List<String> = emptyList(),
    val temperature2mMax: List<Double> = emptyList(),
    val temperature2mMin: List<Double> = emptyList(),
    val precipitationSum: List<Double?> = emptyList(),
    val precipitationProbabilityMax: List<Int?> = emptyList(),
    val weathercode: List<Int> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    val windSpeed10mMax: List<Double?> = emptyList()
)
