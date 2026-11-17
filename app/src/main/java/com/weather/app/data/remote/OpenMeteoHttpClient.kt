package com.weather.app.data.remote

import com.weather.app.data.remote.dto.GeocodingItemDto
import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.data.remote.dto.OpenMeteoCurrentDto
import com.weather.app.data.remote.dto.OpenMeteoDailyDto
import com.weather.app.data.remote.dto.OpenMeteoForecastResponse
import com.weather.app.data.remote.dto.OpenMeteoHourlyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Легкий мережевий клієнт для взаємодії з Open-Meteo Forecast & Geocoding API.
 * Використовує стандартний Android HttpURLConnection без зовнішніх важких залежностей.
 * 
 * Обов'язкова атрибуція ліцензії: CC BY 4.0 Open-Meteo.com.
 */
object OpenMeteoHttpClient {

    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
    private const val CONNECT_TIMEOUT_MS = 10000
    private const val READ_TIMEOUT_MS = 10000

    /**
     * Отримує 14-денний та 24-годинний повноцінний прогноз погоди за координатами.
     */
    suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto"
    ): Result<OpenMeteoForecastResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedTz = URLEncoder.encode(timezone, "UTF-8")
            val urlString = "$FORECAST_BASE_URL?" +
                    "latitude=${String.format(Locale.US, "%.4f", latitude)}" +
                    "&longitude=${String.format(Locale.US, "%.4f", longitude)}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,surface_pressure,wind_speed_10m" +
                    "&hourly=temperature_2m,weather_code,precipitation_probability" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,sunrise,sunset" +
                    "&timezone=$encodedTz" +
                    "&forecast_days=14"

            val jsonString = httpGet(urlString)
            val json = JSONObject(jsonString)

            val currentObj = json.optJSONObject("current")
            val currentDto = if (currentObj != null) {
                OpenMeteoCurrentDto(
                    time = currentObj.optString("time", ""),
                    temperature_2m = currentObj.optDouble("temperature_2m", 0.0),
                    relative_humidity_2m = currentObj.optInt("relative_humidity_2m", 50),
                    apparent_temperature = currentObj.optDouble("apparent_temperature", 0.0),
                    weather_code = currentObj.optInt("weather_code", 0),
                    surface_pressure = currentObj.optDouble("surface_pressure", 1013.0),
                    wind_speed_10m = currentObj.optDouble("wind_speed_10m", 0.0),
                    uv_index = null
                )
            } else null

            val hourlyObj = json.optJSONObject("hourly")
            val hourlyDto = if (hourlyObj != null) {
                OpenMeteoHourlyDto(
                    time = jsonArrayToStringList(hourlyObj.optJSONArray("time")),
                    temperature_2m = jsonArrayToDoubleList(hourlyObj.optJSONArray("temperature_2m")),
                    weather_code = jsonArrayToIntList(hourlyObj.optJSONArray("weather_code")),
                    precipitation_probability = jsonArrayToIntList(hourlyObj.optJSONArray("precipitation_probability"))
                )
            } else null

            val dailyObj = json.optJSONObject("daily")
            val dailyDto = if (dailyObj != null) {
                OpenMeteoDailyDto(
                    time = jsonArrayToStringList(dailyObj.optJSONArray("time")),
                    weather_code = jsonArrayToIntList(dailyObj.optJSONArray("weather_code")),
                    temperature_2m_max = jsonArrayToDoubleList(dailyObj.optJSONArray("temperature_2m_max")),
                    temperature_2m_min = jsonArrayToDoubleList(dailyObj.optJSONArray("temperature_2m_min")),
                    precipitation_sum = jsonArrayToDoubleList(dailyObj.optJSONArray("precipitation_sum")),
                    precipitation_probability_max = jsonArrayToIntList(dailyObj.optJSONArray("precipitation_probability_max")),
                    wind_speed_10m_max = jsonArrayToDoubleList(dailyObj.optJSONArray("wind_speed_10m_max")),
                    sunrise = jsonArrayToStringList(dailyObj.optJSONArray("sunrise")),
                    sunset = jsonArrayToStringList(dailyObj.optJSONArray("sunset"))
                )
            } else null

            val response = OpenMeteoForecastResponse(
                latitude = json.optDouble("latitude", latitude),
                longitude = json.optDouble("longitude", longitude),
                timezone = json.optString("timezone", timezone),
                current = currentDto,
                hourly = hourlyDto,
                daily = dailyDto
            )

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Пошук міст через онлайн-геокодування Open-Meteo.
     */
    suspend fun searchCities(
        query: String,
        language: String = "uk"
    ): Result<GeocodingResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val urlString = "$GEOCODING_BASE_URL?name=$encodedQuery&count=10&language=$language&format=json"

            val jsonString = httpGet(urlString)
            val json = JSONObject(jsonString)

            val resultsArray = json.optJSONArray("results")
            val resultsList = mutableListOf<GeocodingItemDto>()

            if (resultsArray != null) {
                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    resultsList.add(
                        GeocodingItemDto(
                            id = item.optLong("id", 0L),
                            name = item.optString("name", ""),
                            latitude = item.optDouble("latitude", 0.0),
                            longitude = item.optDouble("longitude", 0.0),
                            country = if (item.has("country")) item.optString("country") else null,
                            admin1 = if (item.has("admin1")) item.optString("admin1") else null,
                            timezone = item.optString("timezone", "Europe/Kyiv"),
                            country_code = if (item.has("country_code")) item.optString("country_code") else null
                        )
                    )
                }
            }

            Result.success(GeocodingResponse(results = resultsList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "WeatherApp-Android/1.0 (contact: info@open-meteo.com)")

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) {
            throw Exception("HTTP помилка $responseCode: ${conn.responseMessage}")
        }

        return BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    private fun jsonArrayToStringList(array: org.json.JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    private fun jsonArrayToDoubleList(array: org.json.JSONArray?): List<Double> {
        if (array == null) return emptyList()
        val list = mutableListOf<Double>()
        for (i in 0 until array.length()) {
            list.add(array.getDouble(i))
        }
        return list
    }

    private fun jsonArrayToIntList(array: org.json.JSONArray?): List<Int> {
        if (array == null) return emptyList()
        val list = mutableListOf<Int>()
        for (i in 0 until array.length()) {
            list.add(array.getInt(i))
        }
        return list
    }
}
