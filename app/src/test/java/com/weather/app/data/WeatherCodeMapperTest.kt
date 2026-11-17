package com.weather.app.data

import com.weather.app.data.mapper.WeatherCodeMapper
import com.weather.app.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {

    @Test
    fun mapWmoCode_clearSky_returnsCorrectCondition() {
        assertEquals(WeatherCondition.CLEAR_SKY, WeatherCodeMapper.mapWmoCode(0))
        assertEquals("Ясно", WeatherCodeMapper.getDescriptionUk(0))
    }

    @Test
    fun mapWmoCode_cloudyVariations_returnsCorrectCondition() {
        assertEquals(WeatherCondition.MAINLY_CLEAR, WeatherCodeMapper.mapWmoCode(1))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCodeMapper.mapWmoCode(2))
        assertEquals(WeatherCondition.OVERCAST, WeatherCodeMapper.mapWmoCode(3))
    }

    @Test
    fun mapWmoCode_fog_returnsCorrectCondition() {
        assertEquals(WeatherCondition.FOG, WeatherCodeMapper.mapWmoCode(45))
        assertEquals(WeatherCondition.FOG, WeatherCodeMapper.mapWmoCode(48))
    }

    @Test
    fun mapWmoCode_drizzle_returnsCorrectCondition() {
        assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapWmoCode(51))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapWmoCode(53))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapWmoCode(55))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapWmoCode(56))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCodeMapper.mapWmoCode(57))
    }

    @Test
    fun mapWmoCode_rain_returnsCorrectCondition() {
        assertEquals(WeatherCondition.RAIN, WeatherCodeMapper.mapWmoCode(61))
        assertEquals(WeatherCondition.RAIN, WeatherCodeMapper.mapWmoCode(63))
        assertEquals(WeatherCondition.RAIN, WeatherCodeMapper.mapWmoCode(80))
        assertEquals(WeatherCondition.RAIN, WeatherCodeMapper.mapWmoCode(81))
        assertEquals("Дощ", WeatherCodeMapper.getDescriptionUk(61))
    }

    @Test
    fun mapWmoCode_heavyRain_returnsCorrectCondition() {
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherCodeMapper.mapWmoCode(65))
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherCodeMapper.mapWmoCode(82))
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherCodeMapper.mapWmoCode(66))
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherCodeMapper.mapWmoCode(67))
        assertEquals("Сильний дощ", WeatherCodeMapper.getDescriptionUk(65))
    }

    @Test
    fun mapWmoCode_snow_returnsCorrectCondition() {
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(71))
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(73))
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(75))
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(77))
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(85))
        assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.mapWmoCode(86))
        assertEquals("Сніг", WeatherCodeMapper.getDescriptionUk(71))
    }

    @Test
    fun mapWmoCode_thunderstorm_returnsCorrectCondition() {
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCodeMapper.mapWmoCode(95))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCodeMapper.mapWmoCode(96))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCodeMapper.mapWmoCode(99))
        assertEquals("Гроза", WeatherCodeMapper.getDescriptionUk(95))
    }

    @Test
    fun mapWmoCode_unknownOrNull_returnsUnknown() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapWmoCode(null))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapWmoCode(-1))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.mapWmoCode(999))
        assertEquals("Невідомо", WeatherCodeMapper.getDescriptionUk(null))
    }
}
