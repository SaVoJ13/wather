package com.weather.app.domain.model

import androidx.annotation.StringRes
import com.weather.app.R

/**
 * Доменне представлення погодних умов з локалізованим описом.
 * Відповідає класифікації WMO (World Meteorological Organization).
 */
enum class WeatherCondition(
    val descriptionUk: String,
    @StringRes val stringRes: Int
) {
    CLEAR_SKY("Ясно", R.string.condition_clear_sky),
    MAINLY_CLEAR("Здебільшого ясно", R.string.condition_mainly_clear),
    PARTLY_CLOUDY("Мінлива хмарність", R.string.condition_partly_cloudy),
    OVERCAST("Хмарно", R.string.condition_overcast),
    FOG("Туман", R.string.condition_fog),
    DRIZZLE("Мряка", R.string.condition_drizzle),
    RAIN("Дощ", R.string.condition_rain),
    HEAVY_RAIN("Сильний дощ", R.string.condition_heavy_rain),
    SNOW("Сніг", R.string.condition_snow),
    THUNDERSTORM("Гроза", R.string.condition_thunderstorm),
    UNKNOWN("Невідомо", R.string.condition_unknown)
}
