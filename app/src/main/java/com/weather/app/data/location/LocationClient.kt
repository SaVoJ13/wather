package com.weather.app.data.location

import com.weather.app.domain.model.LocationModel

/**
 * Виняток при відхиленні користувачем дозволу на отримання геолокації.
 */
class PermissionDeniedException(message: String = "Доступ до геолокації не надано користувачем") : SecurityException(message)

/**
 * Виняток при вимкненому GPS/провайдері геолокації в системі.
 */
class LocationDisabledException(message: String = "Службу геолокації вимкнено на пристрої") : Exception(message)

/**
 * Безпечний контракт для отримання геопозиції пристрою виключно у передньому плані (foreground).
 * 
 * Згідно з ADR-004 та Конституцією:
 * 1. Жодної фонової геолокації (заборонено ACCESS_BACKGROUND_LOCATION).
 * 2. Запит лише при явній дії користувача.
 * 3. Жодних точних координат у системні логи чи аналітику.
 */
interface LocationClient {
    /**
     * Отримує поточну локацію користувача (coarse або fine).
     */
    suspend fun getCurrentLocation(): Result<LocationModel>
}

/**
 * Мок-імплементація або тестова реалізація LocationClient.
 */
class DefaultLocationClient(
    private val locationProvider: (suspend () -> Result<LocationModel>)? = null
) : LocationClient {

    override suspend fun getCurrentLocation(): Result<LocationModel> {
        if (locationProvider != null) {
            return locationProvider.invoke()
        }

        // За замовчуванням повертаємо безпечну локацію Києва як поточну
        return Result.success(
            LocationModel(
                id = 1L,
                name = "Київ (Поточна локація)",
                country = "Україна",
                admin1 = "Київ",
                latitude = 50.4501,
                longitude = 30.5234,
                timezone = "Europe/Kyiv",
                isCurrentLocation = true
            )
        )
    }
}
