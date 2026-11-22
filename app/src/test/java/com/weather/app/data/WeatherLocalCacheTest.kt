package com.weather.app.data

import android.content.SharedPreferences
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.time.LocalDateTime

class WeatherLocalCacheTest {

    private val storage = mutableMapOf<String, Any?>()

    @Before
    fun setUp() {
        storage.clear()

        val editorHandler = object : InvocationHandler {
            override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                when (method.name) {
                    "putString" -> {
                        val key = args!![0] as String
                        val value = args[1] as String?
                        storage[key] = value
                        return proxy
                    }
                    "putLong" -> {
                        val key = args!![0] as String
                        val value = args[1] as Long
                        storage[key] = value
                        return proxy
                    }
                    "putFloat" -> {
                        val key = args!![0] as String
                        val value = args[1] as Float
                        storage[key] = value
                        return proxy
                    }
                    "putBoolean" -> {
                        val key = args!![0] as String
                        val value = args[1] as Boolean
                        storage[key] = value
                        return proxy
                    }
                    "remove" -> {
                        val key = args!![0] as String
                        storage.remove(key)
                        return proxy
                    }
                    "clear" -> {
                        storage.clear()
                        return proxy
                    }
                    "apply" -> {
                        return null
                    }
                    "commit" -> {
                        return true
                    }
                }
                return null
            }
        }

        val fakeEditor = Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java),
            editorHandler
        ) as SharedPreferences.Editor

        val prefsHandler = object : InvocationHandler {
            override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                when (method.name) {
                    "getString" -> {
                        val key = args!![0] as String
                        val def = args.getOrNull(1) as String?
                        return storage[key] as? String ?: def
                    }
                    "getLong" -> {
                        val key = args!![0] as String
                        val def = args[1] as Long
                        return storage[key] as? Long ?: def
                    }
                    "getFloat" -> {
                        val key = args!![0] as String
                        val def = args[1] as Float
                        return storage[key] as? Float ?: def
                    }
                    "getBoolean" -> {
                        val key = args!![0] as String
                        val def = args[1] as Boolean
                        return storage[key] as? Boolean ?: def
                    }
                    "edit" -> {
                        return fakeEditor
                    }
                }
                return null
            }
        }

        val fakePrefs = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java),
            prefsHandler
        ) as SharedPreferences

        WeatherLocalCache.customPrefs = fakePrefs
    }

    @After
    fun tearDown() {
        WeatherLocalCache.customPrefs = null
        storage.clear()
    }

    @Test
    fun saveAndLoadForecast_persistsFull24HoursAnd14Days() {
        val location = LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            admin1 = "Київ",
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv"
        )
        val mockForecast = MockWeatherRepository.createKyivForecast(location)

        val now = LocalDateTime.now()
        val full24Hours = (0 until 24).map { h ->
            HourlyForecastItem(
                time = now.plusHours(h.toLong()),
                temperatureCelsius = 15.0 + h,
                condition = WeatherCondition.CLEAR_SKY,
                precipitationProbabilityPercent = h * 2
            )
        }
        val fullForecast = mockForecast.copy(
            hourly24h = full24Hours,
            dataSource = WeatherDataSource.WEATHER_METRO
        )

        WeatherLocalCache.saveForecast(forecast = fullForecast)

        val loaded = WeatherLocalCache.loadCachedForecast()
        assertNotNull("Loaded forecast must not be null", loaded)
        assertEquals("Location name must match", "Київ", loaded!!.location.name)
        assertEquals("Hourly list must contain 24 items (not truncated to 6)", 24, loaded.hourly24h.size)
        assertEquals("Daily list must contain all 14 items (not truncated to 5)", 14, loaded.daily14Days.size)
        assertEquals("Data source must be preserved as WEATHER_METRO", WeatherDataSource.WEATHER_METRO, loaded.dataSource)
        assertFalse("Newly saved forecast must not be marked stale", loaded.isStale)
    }

    @Test
    fun multiCityCache_savesAndLoadsPerLocation() {
        val kyiv = LocationModel(703448L, "Київ", "Україна", null, 50.45, 30.52, "Europe/Kyiv")
        val warsaw = LocationModel(756135L, "Варшава", "Польща", null, 52.23, 21.01, "Europe/Warsaw")

        val kyivForecast = MockWeatherRepository.createKyivForecast(kyiv)
        val warsawForecast = MockWeatherRepository.createWarsawForecast(warsaw)

        WeatherLocalCache.saveForecastForLocation(location = kyiv, forecast = kyivForecast)
        WeatherLocalCache.saveForecastForLocation(location = warsaw, forecast = warsawForecast)

        val loadedKyiv = WeatherLocalCache.loadCachedForecastForLocation(location = kyiv)
        val loadedWarsaw = WeatherLocalCache.loadCachedForecastForLocation(location = warsaw)

        assertNotNull("Kyiv forecast must be loaded from multi-city cache", loadedKyiv)
        assertEquals("Київ", loadedKyiv!!.location.name)

        assertNotNull("Warsaw forecast must be loaded from multi-city cache", loadedWarsaw)
        assertEquals("Варшава", loadedWarsaw!!.location.name)
    }

    @Test
    fun corruptedCache_recoversGracefullyWithoutCrash() {
        // Записуємо пошкоджений невалідний JSON
        storage["forecast_json"] = "{ corrupted invalid json text ... "
        storage["loc_name"] = "Київ"
        storage["loc_lat"] = 50.45f
        storage["loc_lon"] = 30.52f

        // Метод не повинен викидати Exception або крашити застосунок
        val loaded = WeatherLocalCache.loadCachedForecast()
        assertNull("Corrupted cache must return null safely", loaded)

        // Пошкоджений ключ повинен бути автоматично видалений
        assertNull("Corrupted key must be cleaned up", storage["forecast_json"])
    }
}
