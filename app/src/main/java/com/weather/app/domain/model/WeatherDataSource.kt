package com.weather.app.domain.model

/**
 * Ідентифікатор фактичного джерела погодних даних для динамічної атрибуції та дотримання ліцензій.
 */
enum class WeatherDataSource(val defaultName: String) {
    OPEN_METEO("Open-Meteo"),
    WEATHER_METRO("WeatherMetro"),
    VISUAL_CROSSING("Visual Crossing"),
    MOCK("Mock / Test Data")
}
