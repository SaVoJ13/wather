package com.weather.app.data

import com.weather.app.data.mapper.GeocodingMapper
import com.weather.app.data.remote.dto.GeocodingItemDto
import com.weather.app.data.remote.dto.GeocodingResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeocodingMapperTest {

    @Test
    fun toDomain_mapsAllFieldsCorrectly() {
        val dto = GeocodingItemDto(
            id = 703448L,
            name = "Київ",
            latitude = 50.4501,
            longitude = 30.5234,
            country_code = "UA",
            country = "Україна",
            admin1 = "Київська область",
            timezone = "Europe/Kyiv"
        )

        val domain = GeocodingMapper.toDomain(dto)

        assertEquals(703448L, domain.id)
        assertEquals("Київ", domain.name)
        assertEquals(50.4501, domain.latitude, 0.0001)
        assertEquals(30.5234, domain.longitude, 0.0001)
        assertEquals("Україна", domain.country)
        assertEquals("Київська область", domain.admin1)
        assertEquals("Europe/Kyiv", domain.timezone)
        assertFalse(domain.isCurrentLocation)
    }

    @Test
    fun toDomain_nullCountryFallsBackToCountryCode() {
        val dto = GeocodingItemDto(
            id = 123L,
            name = "TestCity",
            latitude = 10.0,
            longitude = 20.0,
            country_code = "PL",
            country = null,
            admin1 = null,
            timezone = null
        )

        val domain = GeocodingMapper.toDomain(dto)
        assertEquals("PL", domain.country)
        assertEquals("UTC", domain.timezone)
    }

    @Test
    fun toDomainList_nullOrEmptyResponseReturnsEmptyList() {
        assertTrue(GeocodingMapper.toDomainList(null).isEmpty())
        assertTrue(GeocodingMapper.toDomainList(GeocodingResponse(results = null)).isEmpty())
        assertTrue(GeocodingMapper.toDomainList(GeocodingResponse(results = emptyList())).isEmpty())
    }

    @Test
    fun toDomainList_validResponseReturnsMappedList() {
        val response = GeocodingResponse(
            results = listOf(
                GeocodingItemDto(id = 1L, name = "Kyiv", latitude = 50.4, longitude = 30.5, country = "Ukraine"),
                GeocodingItemDto(id = 2L, name = "Lviv", latitude = 49.8, longitude = 24.0, country = "Ukraine")
            )
        )

        val list = GeocodingMapper.toDomainList(response)
        assertEquals(2, list.size)
        assertEquals("Kyiv", list[0].name)
        assertEquals("Lviv", list[1].name)
    }
}
