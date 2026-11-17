package com.weather.app.data.remote.dto

/**
 * Типізовані структури відповіді Open-Meteo Geocoding API.
 * 
 * Першоджерело: https://open-meteo.com/en/docs/geocoding-api
 * Ліцензія: CC BY 4.0 (некомерційне використання для dev/test).
 */
data class GeocodingResponse(
    val results: List<GeocodingItemDto>? = null,
    val generationtime_ms: Double? = null
)

data class GeocodingItemDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val feature_code: String? = null,
    val country_code: String? = null,
    val country: String? = null,
    val admin1: String? = null,
    val admin2: String? = null,
    val admin3: String? = null,
    val admin4: String? = null,
    val timezone: String? = null,
    val population: Long? = null,
    val postcodes: List<String>? = null
)
