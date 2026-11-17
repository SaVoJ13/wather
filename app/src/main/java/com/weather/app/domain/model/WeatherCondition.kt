package com.weather.app.domain.model

/**
 * Доменне представлення погодних умов з локалізованим описом українською мовою.
 * Відповідає класифікації WMO (World Meteorological Organization).
 */
enum class WeatherCondition(val descriptionUk: String) {
    CLEAR_SKY("Ясно"),
    MAINLY_CLEAR("Здебільшого ясно"),
    PARTLY_CLOUDY("Мінлива хмарність"),
    OVERCAST("Хмарно"),
    FOG("Туман"),
    DRIZZLE("Мряка"),
    RAIN("Дощ"),
    HEAVY_RAIN("Сильний дощ"),
    SNOW("Сніг"),
    THUNDERSTORM("Гроза"),
    UNKNOWN("Невідомо")
}
