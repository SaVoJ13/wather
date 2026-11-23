package com.weather.app.data

import com.weather.app.data.remote.dto.GeocodingItemDto
import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.data.location.DefaultLocationClient
import com.weather.app.data.location.LocationDisabledException
import com.weather.app.data.location.PermissionDeniedException
import com.weather.app.data.remote.GeocodingApiClient
import com.weather.app.data.repository.LocationRepositoryImpl
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тести Phase 3 для [LocationRepositoryImpl]:
 * - Пошук міст без обов'язкового GPS;
 * - Менеджмент обраних міст;
 * - Обробка відмови користувача та вимкненого сервісу локації.
 */
class LocationRepositoryPhase3Test {

    private val warsaw = LocationModel(
        id = 2L,
        name = "Варшава",
        country = "Польща",
        admin1 = "Мазовецьке воєводство",
        latitude = 52.2297,
        longitude = 21.0122,
        timezone = "Europe/Warsaw"
    )

    private val kyiv = LocationModel(
        id = 703448L,
        name = "Київ",
        country = "Україна",
        admin1 = "Київська область",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv"
    )

    @Test
    fun testFavoriteLocationsManagement() = runBlocking {
        val repository = LocationRepositoryImpl()

        // 1. Початковий список містить Київ
        val initial = repository.getFavoriteLocations().first()
        assertTrue(initial.any { it.name.contains("Київ") })

        // 2. Додаємо Варшаву
        repository.saveFavoriteLocation(warsaw)
        val afterAdd = repository.getFavoriteLocations().first()
        assertEquals(2, afterAdd.size)
        assertTrue(afterAdd.any { it.name == "Варшава" })

        // 3. Видаляємо Варшаву
        repository.removeFavoriteLocation(warsaw)
        val afterRemove = repository.getFavoriteLocations().first()
        assertEquals(1, afterRemove.size)
        assertFalse(afterRemove.any { it.name == "Варшава" })
    }

    @Test
    fun testLocationPermissionDeniedHandledGracefully() = runBlocking {
        val deniedClient = DefaultLocationClient(
            locationProvider = {
                Result.failure(PermissionDeniedException("Дозвіл на доступ до геолокації відхилено"))
            }
        )

        val repository = LocationRepositoryImpl(locationClient = deniedClient)
        val result = repository.getCurrentLocation()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PermissionDeniedException)
    }

    @Test
    fun testLocationDisabledHandledGracefully() = runBlocking {
        val disabledClient = DefaultLocationClient(
            locationProvider = {
                Result.failure(LocationDisabledException("GPS вимкнено"))
            }
        )

        val repository = LocationRepositoryImpl(locationClient = disabledClient)
        val result = repository.getCurrentLocation()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is LocationDisabledException)
    }

    @Test
    fun testSearchCitiesDoesNotRequireGps() = runBlocking {
        val fakeGeocodingClient = GeocodingApiClient(
            networkSearch = { query, _ ->
                Result.success(
                    GeocodingResponse(
                        results = listOf(
                            GeocodingItemDto(
                                id = 3088006L,
                                name = "Przecław",
                                country = "Poland",
                                admin1 = "West Pomeranian Voivodeship",
                                latitude = 53.3745,
                                longitude = 14.4725,
                                timezone = "Europe/Warsaw"
                            )
                        )
                    )
                )
            }
        )

        val repository = LocationRepositoryImpl(geocodingApiClient = fakeGeocodingClient)
        val searchResult = repository.searchCities("Przecław")

        assertTrue(searchResult.isSuccess)
        val cities = searchResult.getOrThrow()
        assertEquals(1, cities.size)
        assertEquals("Przecław", cities.first().name)
    }
}
