package com.weather.app.domain.repository

import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

/**
 * Контракт репозиторію локації та геокодування відповідно до docs/architecture/CONTRACTS.md.
 */
interface LocationRepository {
    /**
     * Пошук міст через онлайн-геокодування. Працює автономно без GPS.
     */
    suspend fun searchCities(query: String): Result<List<LocationModel>>

    /**
     * Отримання поточної геолокації користувача (тільки foreground).
     * Якщо дозвіл не надано, повертає Result.failure(PermissionDeniedException).
     */
    suspend fun getCurrentLocation(): Result<LocationModel>

    /**
     * Отримання збережених обраних міст користувача.
     */
    fun getFavoriteLocations(): Flow<List<LocationModel>>

    /**
     * Додавання локації до обраних.
     */
    suspend fun saveFavoriteLocation(location: LocationModel)

    /**
     * Видалення локації з обраних.
     */
    suspend fun removeFavoriteLocation(location: LocationModel)
}
