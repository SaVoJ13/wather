package com.weather.app.ui.state

import com.weather.app.domain.model.FullWeatherForecast

/**
 * Стан інтерфейсу погодного екрана згідно з docs/architecture/CONTRACTS.md.
 */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val forecast: FullWeatherForecast,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val message: String? = null
    ) : WeatherUiState

    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val lastCachedForecast: FullWeatherForecast? = null
    ) : WeatherUiState
}
