package com.weather.app.data

import com.weather.app.data.location.DefaultLocationClient
import com.weather.app.data.location.PermissionDeniedException
import com.weather.app.data.remote.GeocodingApiClient
import com.weather.app.data.repository.LocationRepositoryImpl
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationRepositoryTest {

    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setUp() {
        repository = LocationRepositoryImpl(
            geocodingApiClient = GeocodingApiClient(),
            locationClient = DefaultLocationClient()
        )
    }

    @Test
    fun searchCities_returnsMatchingMockCities_withoutGps() = runBlocking {
        val result = repository.searchCities("Київ")
        assertTrue(result.isSuccess)
        val cities = result.getOrThrow()
        assertTrue(cities.isNotEmpty())
        assertEquals("Київ", cities.first().name)
        assertEquals("Україна", cities.first().country)
    }

    @Test
    fun searchCities_emptyQuery_returnsEmptyList() = runBlocking {
        val result = repository.searchCities("   ")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun favoriteLocations_addAndRemove_worksCorrectly() = runBlocking {
        val initialFavorites = repository.getFavoriteLocations().first()
        assertEquals(1, initialFavorites.size)
        assertEquals("Київ", initialFavorites.first().name)

        val lviv = LocationModel(
            id = 702550L,
            name = "Львів",
            country = "Україна",
            admin1 = "Львівська область",
            latitude = 49.8397,
            longitude = 24.0297,
            timezone = "Europe/Kyiv"
        )

        repository.saveFavoriteLocation(lviv)
        val afterAdd = repository.getFavoriteLocations().first()
        assertEquals(2, afterAdd.size)
        assertTrue(afterAdd.any { it.name == "Львів" })

        // Повторне додавання не дублює
        repository.saveFavoriteLocation(lviv)
        val afterDuplicate = repository.getFavoriteLocations().first()
        assertEquals(2, afterDuplicate.size)

        // Видалення
        repository.removeFavoriteLocation(lviv)
        val afterRemove = repository.getFavoriteLocations().first()
        assertEquals(1, afterRemove.size)
        assertFalse(afterRemove.any { it.name == "Львів" })
    }

    @Test
    fun getCurrentLocation_permissionDenied_returnsFailure() = runBlocking {
        val repoWithDeniedLocation = LocationRepositoryImpl(
            locationClient = DefaultLocationClient(
                locationProvider = { Result.failure(PermissionDeniedException()) }
            )
        )

        val result = repoWithDeniedLocation.getCurrentLocation()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PermissionDeniedException)
    }
}
