package com.weather.app.data.mapper

import com.weather.app.domain.model.WeatherCondition

/**
 * Мапер погодних інтерпретаційних кодів WMO (0-99) у строгий [WeatherCondition].
 * 
 * Першоджерело WMO Code Table 4677 / Open-Meteo Documentation:
 * 0: Clear sky
 * 1, 2, 3: Mainly clear, partly cloudy, and overcast
 * 45, 48: Fog and depositing rime fog
 * 51, 53, 55: Drizzle: Light, moderate, and dense intensity
 * 56, 57: Freezing Drizzle: Light and dense intensity
 * 61, 63, 65: Rain: Slight, moderate and heavy intensity
 * 66, 67: Freezing Rain: Light and heavy intensity
 * 71, 73, 75: Snow fall: Slight, moderate, and heavy intensity
 * 77: Snow grains
 * 80, 81, 82: Rain showers: Slight, moderate, and violent
 * 85, 86: Snow showers slight and heavy
 * 95: Thunderstorm: Slight or moderate
 * 96, 99: Thunderstorm with slight and heavy hail
 */
object WeatherCodeMapper {

    fun mapWmoCode(wmoCode: Int?): WeatherCondition {
        if (wmoCode == null || wmoCode !in 0..99) {
            return WeatherCondition.UNKNOWN
        }
        return when (wmoCode) {
            0 -> WeatherCondition.CLEAR_SKY
            1 -> WeatherCondition.MAINLY_CLEAR
            2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.OVERCAST
            45, 48 -> WeatherCondition.FOG
            51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
            61, 63, 80, 81 -> WeatherCondition.RAIN
            65, 82, 66, 67 -> WeatherCondition.HEAVY_RAIN
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.UNKNOWN
        }
    }

    /**
     * Повертає локалізований опис українською мовою для заданого коду WMO.
     */
    fun getDescriptionUk(wmoCode: Int?): String {
        return mapWmoCode(wmoCode).descriptionUk
    }
}
