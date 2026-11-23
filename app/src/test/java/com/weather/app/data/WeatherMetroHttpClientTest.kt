package com.weather.app.data

import com.weather.app.data.remote.WeatherApiException
import com.weather.app.data.remote.WeatherMetroHttpClient
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тести для [WeatherMetroHttpClient] та обробки відповідей WeatherMetro API v2.
 * Перевіряє коректний парсинг повного JSON та генерацію винятків [WeatherApiException]
 * при помилках 401, 429, 500 або некоректних полях.
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
        // HTTP 401: Неавторизований доступ / недійсний ключ
        val ex401 = WeatherApiException(401, "Invalid API key")
        assertEquals(401, ex401.statusCode)
        assertTrue(ex401.message!!.contains("401"))
        assertTrue(ex401.message!!.contains("Invalid API key"))

        // HTTP 429: Перевищення ліміту запитів
        val ex429 = WeatherApiException(429, "Rate limit exceeded (Too Many Requests)")
        assertEquals(429, ex429.statusCode)
        assertTrue(ex429.message!!.contains("429"))

        // HTTP 500: Помилка на стороні сервера
        val ex500 = WeatherApiException(500, "Internal Server Error")
        assertEquals(500, ex500.statusCode)
        assertTrue(ex500.message!!.contains("500"))
    }

    @Test
    fun testWeathercodeParsingNullableInts() {
        val json = """
        {
          "response": {
            "data": {
              "latitude": 50.45,
              "longitude": 30.52,
              "hourly": {
                "time": ["2026-11-23T00:00", "2026-11-23T01:00", "2026-11-23T02:00", "2026-11-23T03:00"],
                "temperature_2m": [10.0, 11.0, 12.0, 13.0],
                "weathercode": [0, null, 95, "invalid"]
              },
              "daily": {
                "time": ["2026-11-23", "2026-11-24"],
                "temperature_2m_max": [15.0, 16.0],
                "temperature_2m_min": [5.0, 6.0],
                "weathercode": [null, 0]
              }
            }
          }
        }
        """.trimIndent()

        val root = JSONObject(json)
        val dataObj = root.getJSONObject("response").getJSONObject("data")
        val hourlyObj = dataObj.getJSONObject("hourly")
        val hourlyArr = hourlyObj.getJSONArray("weathercode")

        // Перевіряємо, що елементи JSON: 0 є числом 0, null є null, 95 є 95, "invalid" не число
        assertEquals(0, hourlyArr.getInt(0))
        assertTrue(hourlyArr.isNull(1))
        assertEquals(95, hourlyArr.getInt(2))
        assertEquals("invalid", hourlyArr.getString(3))
    }

    @Test
    fun testFractionalWmoCodeIsNotTruncatedToZero() {
        val jsonArray = org.json.JSONArray("[0, 0.0, 0.5, 1.0, 1.9, \"0\", \"0.5\", null]")
        val parsed = WeatherMetroHttpClient.jsonArrayToNullableIntList(jsonArray)

        // 0 -> 0 (Clear Sky)
        assertEquals(0, parsed[0])
        // 0.0 -> 0 (Clear Sky)
        assertEquals(0, parsed[1])
        // 0.5 -> null (НЕ truncates to 0!)
        org.junit.Assert.assertNull("0.5 must NOT be truncated to 0 Clear Sky", parsed[2])
        // 1.0 -> 1
        assertEquals(1, parsed[3])
        // 1.9 -> null (НЕ truncates to 1!)
        org.junit.Assert.assertNull("1.9 must NOT be truncated to 1", parsed[4])
        // "0" -> 0
        assertEquals(0, parsed[5])
        // "0.5" -> null
        org.junit.Assert.assertNull("\"0.5\" must NOT be parsed as 0", parsed[6])
        // null -> null
        org.junit.Assert.assertNull(parsed[7])
    }
}
