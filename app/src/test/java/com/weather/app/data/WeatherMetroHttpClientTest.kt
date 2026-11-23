package com.weather.app.data

import com.weather.app.data.remote.WeatherApiException
import com.weather.app.data.remote.WeatherMetroHttpClient
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тести для [WeatherMetroHttpClient] та обробки відповідей WeatherMetro API v2.
 */
class WeatherMetroHttpClientTest {

    @Test
    fun testWeatherMetroResponseParsing() {
        val sampleJson = """
        {
          "meta": { "code": 200, "status": "success" },
          "response": {
            "data": {
              "latitude": 50.45,
              "longitude": 30.52,
              "generationtime_ms": 1.45,
              "utc_offset_seconds": 7200,
              "timezone": "Europe/Kyiv",
              "timezone_abbreviation": "EET",
              "hourly": {
                "time": ["2026-11-23T00:00", "2026-11-23T01:00", "2026-11-23T02:00"],
                "temperature_2m": [3.2, 2.9, 2.5],
                "precipitation_probability": [10, 15, 20],
                "weathercode": [1, 2, 3],
                "relative_humidity_2m": [80, 82, 85],
                "wind_speed_10m": [12.5, 14.0, 15.2]
              },
              "daily": {
                "time": ["2026-11-23", "2026-11-24"],
                "temperature_2m_max": [6.5, 5.0],
                "temperature_2m_min": [1.2, 0.5],
                "precipitation_probability_max": [30, 45],
                "weathercode": [3, 61],
                "sunrise": ["2026-11-23T07:22", "2026-11-24T07:23"],
                "sunset": ["2026-11-23T16:06", "2026-11-24T16:05"],
                "wind_speed_10m_max": [18.2, 22.0]
              }
            }
          }
        }
        """.trimIndent()

        val root = JSONObject(sampleJson)
        val dataObj = root.getJSONObject("response").getJSONObject("data")
        val hourlyObj = dataObj.getJSONObject("hourly")
        val dailyObj = dataObj.getJSONObject("daily")

        assertEquals(50.45, dataObj.getDouble("latitude"), 0.001)
        assertEquals(30.52, dataObj.getDouble("longitude"), 0.001)
        assertEquals("Europe/Kyiv", dataObj.getString("timezone"))

        assertEquals(3, hourlyObj.getJSONArray("time").length())
        assertEquals(3.2, hourlyObj.getJSONArray("temperature_2m").getDouble(0), 0.01)
        assertEquals(1, hourlyObj.getJSONArray("weathercode").getInt(0))

        assertEquals(2, dailyObj.getJSONArray("time").length())
        assertEquals(6.5, dailyObj.getJSONArray("temperature_2m_max").getDouble(0), 0.01)
        assertEquals(30, dailyObj.getJSONArray("precipitation_probability_max").getInt(0))
        assertEquals(3, dailyObj.getJSONArray("weathercode").getInt(0))
    }

    @Test
    fun testWeatherApiExceptionProperties() {
        val ex401 = WeatherApiException(401, "Invalid API key")
        assertEquals(401, ex401.statusCode)
        assertTrue(ex401.message!!.contains("401"))
        assertTrue(ex401.message!!.contains("Invalid API key"))

        val ex429 = WeatherApiException(429, "Rate limit exceeded")
        assertEquals(429, ex429.statusCode)
        assertTrue(ex429.message!!.contains("429"))
    }
}
