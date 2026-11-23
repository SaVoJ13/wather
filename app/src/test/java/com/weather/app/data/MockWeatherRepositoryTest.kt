package com.weather.app.data

import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.domain.model.WeatherCondition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockWeatherRepositoryTest {

    private lateinit var repository: MockWeatherRepository

    @Before
    fun setUp() {
        repository = MockWeatherRepository()
    }

    @Test
    fun getFullForecast_forKyiv_returns24HoursAnd14Days() = runBlocking {
        val kyiv = MockWeatherRepository.createKyivLocation()
        val result = repository.getFullForecast(kyiv)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()

        assertEquals("Київ", forecast.location.name)
        assertEquals("Україна", forecast.location.country)
        assertEquals(24, forecast.hourly24h.size)
        assertEquals(14, forecast.daily14Days.size)
        assertFalse(forecast.isStale)
        assertNotNull(forecast.current)
        assertEquals(WeatherCondition.CLEAR_SKY, forecast.current.condition)
    }

    @Test
    fun getFullForecast_forWarsaw_returns24HoursAnd14DaysRain() = runBlocking {
        val warsaw = MockWeatherRepository.createWarsawLocation()
        val result = repository.getFullForecast(warsaw)

        assertTrue(result.isSuccess)
        val forecast = result.getOrThrow()

        assertEquals("Варшава", forecast.location.name)
        assertEquals("Польща", forecast.location.country)
        assertEquals(24, forecast.hourly24h.size)
        assertEquals(14, forecast.daily14Days.size)
        assertEquals(WeatherCondition.RAIN, forecast.current.condition)
        assertTrue((forecast.current.relativeHumidityPercent ?: 0) > 80)
    }

    @Test
    fun observeCurrentForecast_emitsInitialKyivForecast() = runBlocking {
        val current = repository.observeCurrentForecast().first()

        assertNotNull(current)
        assertEquals("Київ", current?.location?.name)
    }
}
