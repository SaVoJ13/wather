package com.weather.app.domain.model

/**
 * Модель населеного пункту / локації.
 */
data class LocationModel(
    val id: Long = 0,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val isCurrentLocation: Boolean = false
)
