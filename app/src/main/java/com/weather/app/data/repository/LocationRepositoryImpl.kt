package com.weather.app.data.repository

import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.location.DefaultLocationClient
import com.weather.app.data.location.LocationClient
import com.weather.app.data.remote.GeocodingApiClient
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Реалізація [LocationRepository].
 * Забезпечує пошук міст без обов'язкового GPS, отримання поточної геолокації
 * та дисковий менеджмент списку обраних міст через [WeatherLocalCache].
 */
class LocationRepositoryImpl(
    private val geocodingApiClient: GeocodingApiClient = GeocodingApiClient(),
    private val locationClient: LocationClient = DefaultLocationClient()
) : LocationRepository {

    private val _favoritesFlow = MutableStateFlow<List<LocationModel>>(emptyList())

    init {
        try {
            val savedCities = WeatherLocalCache.loadSelectedCities(null)
            if (savedCities.isNotEmpty()) {
                _favoritesFlow.value = savedCities
            } else {
                _favoritesFlow.value = listOf(
                    LocationModel(
                        id = 703448L,
                        name = "Київ",
                        country = "Україна",
                        admin1 = "Київська область",
                        latitude = 50.4501,
                        longitude = 30.5234,
                        timezone = "Europe/Kyiv"
                    )
                )
            }
        } catch (ignored: Throwable) {
            _favoritesFlow.value = listOf(
                LocationModel(
                    id = 703448L,
                    name = "Київ",
                    country = "Україна",
                    admin1 = "Київська область",
                    latitude = 50.4501,
                    longitude = 30.5234,
                    timezone = "Europe/Kyiv"
                )
            )
        }
    }

    override suspend fun searchCities(query: String): Result<List<LocationModel>> {
        return geocodingApiClient.search(query)
    }

    override suspend fun getCurrentLocation(): Result<LocationModel> {
        return locationClient.getCurrentLocation()
    }

    override fun getFavoriteLocations(): Flow<List<LocationModel>> {
        return _favoritesFlow.asStateFlow()
    }

    override suspend fun saveFavoriteLocation(location: LocationModel) {
        _favoritesFlow.update { currentList ->
            val updated = if (currentList.any { it.name.equals(location.name, ignoreCase = true) && it.country == location.country }) {
                currentList
            } else {
                currentList + location.copy(isCurrentLocation = false)
            }
            try {
                WeatherLocalCache.addSelectedCity(null, location)
            } catch (ignored: Throwable) {}
            updated
        }
    }

    override suspend fun removeFavoriteLocation(location: LocationModel) {
        _favoritesFlow.update { currentList ->
            val updated = currentList.filterNot {
                (it.id != 0L && it.id == location.id) ||
                        (it.name.equals(location.name, ignoreCase = true) && it.country == location.country)
            }
            try {
                WeatherLocalCache.removeSelectedCity(null, location)
            } catch (ignored: Throwable) {}
            updated
        }
    }
}

