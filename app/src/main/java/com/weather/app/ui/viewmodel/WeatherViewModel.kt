package com.weather.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.data.repository.LocationRepositoryImpl
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.LocationRepository
import com.weather.app.domain.repository.WeatherRepository
import com.weather.app.ui.state.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel головного екрана погоди з підтримкою реального API Open-Meteo,
 * пошуку міст через геокодування та визначення геопозиції користувача.
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl(),
    private val locationRepository: LocationRepository = LocationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val popularCities: List<LocationModel> = listOf(
        LocationModel(id = 703448L, name = "Київ", country = "Україна", admin1 = "Київ", latitude = 50.4501, longitude = 30.5234, timezone = "Europe/Kyiv"),
        LocationModel(id = 702550L, name = "Львів", country = "Україна", admin1 = "Львівська область", latitude = 49.8397, longitude = 24.0297, timezone = "Europe/Kyiv"),
        LocationModel(id = 698740L, name = "Одеса", country = "Україна", admin1 = "Одеська область", latitude = 46.4825, longitude = 30.7233, timezone = "Europe/Kyiv"),
        LocationModel(id = 706483L, name = "Харків", country = "Україна", admin1 = "Харківська область", latitude = 49.9935, longitude = 36.2304, timezone = "Europe/Kyiv"),
        LocationModel(id = 709930L, name = "Дніпро", country = "Україна", admin1 = "Дніпропетровська область", latitude = 48.4647, longitude = 35.0462, timezone = "Europe/Kyiv"),
        LocationModel(id = 756135L, name = "Варшава", country = "Польща", admin1 = "Мазовецьке воєводство", latitude = 52.2297, longitude = 21.0122, timezone = "Europe/Warsaw"),
        LocationModel(id = 2643743L, name = "Лондон", country = "Велика Британія", admin1 = "Англія", latitude = 51.5085, longitude = -0.1257, timezone = "Europe/London"),
        LocationModel(id = 2950159L, name = "Берлін", country = "Німеччина", admin1 = "Берлін", latitude = 52.5244, longitude = 13.4105, timezone = "Europe/Berlin")
    )

    private var currentLocation: LocationModel = popularCities.first()

    init {
        observeCachedForecast()
        loadForecast(currentLocation)
    }

    private fun observeCachedForecast() {
        viewModelScope.launch {
            weatherRepository.observeCurrentForecast()
                .catch { /* ігноруємо помилки спостереження */ }
                .collect { forecast ->
                    if (forecast != null && _uiState.value is WeatherUiState.Loading) {
                        _uiState.value = WeatherUiState.Success(
                            forecast = forecast,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun loadForecast(location: LocationModel = currentLocation, forceRefresh: Boolean = false) {
        currentLocation = location
        val currentState = _uiState.value

        if (forceRefresh && currentState is WeatherUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        } else if (!forceRefresh && currentState !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        viewModelScope.launch {
            val result = weatherRepository.getFullForecast(location, forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { forecast ->
                    _uiState.value = WeatherUiState.Success(
                        forecast = forecast,
                        isRefreshing = false,
                        isOffline = forecast.isStale
                    )
                },
                onFailure = { error ->
                    val lastForecast = (currentState as? WeatherUiState.Success)?.forecast
                    _uiState.value = WeatherUiState.Error(
                        message = error.message ?: "Помилка завантаження погодних даних",
                        canRetry = true,
                        lastCachedForecast = lastForecast
                    )
                }
            )
        }
    }

    fun refresh() {
        loadForecast(currentLocation, forceRefresh = true)
    }

    fun selectLocation(location: LocationModel) {
        loadForecast(location, forceRefresh = false)
    }

    suspend fun searchCities(query: String): List<LocationModel> {
        val result = locationRepository.searchCities(query)
        return result.getOrDefault(emptyList())
    }

    fun onGpsLocationAcquired(latitude: Double, longitude: Double) {
        val gpsLocation = LocationModel(
            id = 999999L,
            name = "Поточна локація",
            country = "Україна",
            admin1 = null,
            latitude = latitude,
            longitude = longitude,
            timezone = "auto",
            isCurrentLocation = true
        )
        selectLocation(gpsLocation)
    }
}
