package com.weather.app.data.mapper

import com.weather.app.data.remote.dto.GeocodingItemDto
import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.domain.model.LocationModel

/**
 * Мапер для перетворення даних геокодування в доменні моделі [LocationModel].
 */
object GeocodingMapper {

    fun toDomain(dto: GeocodingItemDto): LocationModel {
        return LocationModel(
            id = dto.id,
            name = dto.name,
            country = dto.country ?: dto.country_code ?: "",
            admin1 = dto.admin1,
            latitude = dto.latitude,
            longitude = dto.longitude,
            timezone = dto.timezone ?: "UTC",
            isCurrentLocation = false
        )
    }

    fun toDomainList(response: GeocodingResponse?): List<LocationModel> {
        val list = response?.results ?: return emptyList()
        return list.map { toDomain(it) }
    }
}
