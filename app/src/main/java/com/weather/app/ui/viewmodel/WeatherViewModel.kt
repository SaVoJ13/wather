package com.weather.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.WeatherRepository
import com.weather.app.ui.state.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel головного екрана погоди з підтримкою станів Loading, Success, Error та Pull-to-refresh.
 */
class WeatherViewModel(
    private val repository: WeatherRepository = MockWeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentLocation: LocationModel = MockWeatherRepository.createKyivLocation()

    init {
        observeCachedForecast()
        loadForecast(currentLocation)
    }

    private fun observeCachedForecast() {
        viewModelScope.launch {
            repository.observeCurrentForecast()
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
            val result = repository.getFullForecast(location, forceRefresh = forceRefresh)
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
}
