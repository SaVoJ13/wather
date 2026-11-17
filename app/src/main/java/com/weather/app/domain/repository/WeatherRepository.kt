package com.weather.app.domain.repository

import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

/**
 * Контракт репозиторію погоди відповідно до docs/architecture/CONTRACTS.md.
 */
interface WeatherRepository {
    /**
     * Отримує повний прогноз (поточна, 24h погодинна, 14 днів) для заданих координат.
     * Працює за стратегією: кеш спочатку (швидкий показ) -> оновлення з мережі.
     */
    suspend fun getFullForecast(location: LocationModel, forceRefresh: Boolean = false): Result<FullWeatherForecast>

    /**
     * Потік останнього закешованого прогнозу.
     */
    fun observeCurrentForecast(): Flow<FullWeatherForecast?>
}
