package com.weather.app.data.remote

import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Виняток при помилці WeatherMetro API із зазначенням HTTP-коду та повідомлення сервера.
 */
class WeatherApiException(
    val statusCode: Int,
    message: String
) : Exception("HTTP $statusCode: $message")

/**
 * Легкий мережевий клієнт для взаємодії з WeatherMetro Forecast API v2.
 * Використовує стандартний Android HttpURLConnection без зовнішніх важких залежностей.
 * 
 * Обов'язкова атрибуція ліцензії: "Weather data by WeatherMetro".
 */
object WeatherMetroHttpClient {

    private const val FORECAST_BASE_URL = "https://api.weathermetro.com/v2/forecast"
    private const val CONNECT_TIMEOUT_MS = 10000
    private const val READ_TIMEOUT_MS = 10000

    /**
     * Отримує 16-денний та 384-годинний повноцінний прогноз погоди за координатами.
     */
    suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String = "auto",
        apiKey: String = ""
    ): Result<WeatherMetroResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedTz = URLEncoder.encode(timezone, "UTF-8")
            val latFormatted = String.format(Locale.US, "%.4f", latitude)
            val lngFormatted = String.format(Locale.US, "%.4f", longitude)

            var urlString = "$FORECAST_BASE_URL?" +
                    "lat=$latFormatted" +
                    "&lng=$lngFormatted" +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode,sunrise,sunset,wind_speed_10m_max" +
                    "&hourly=temperature_2m,precipitation_probability,weathercode,relative_humidity_2m,wind_speed_10m" +
                    "&days=16" +
                    "&timezone=$encodedTz"

            if (apiKey.isNotBlank()) {
                val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
                urlString += "&api_key=$encodedKey"
            }

            val jsonString = httpGet(urlString)
            val root = JSONObject(jsonString)

            val responseObj = root.optJSONObject("response")
            val dataObj = responseObj?.optJSONObject("data") ?: root

            val hourlyObj = dataObj.optJSONObject("hourly")
            val hourlyDto = if (hourlyObj != null) {
                WeatherMetroHourlyDto(
                    time = jsonArrayToStringList(hourlyObj.optJSONArray("time")),
                    temperature2m = jsonArrayToDoubleList(hourlyObj.optJSONArray("temperature_2m")),
                    precipitationProbability = jsonArrayToIntList(hourlyObj.optJSONArray("precipitation_probability")),
                    weathercode = jsonArrayToIntList(hourlyObj.optJSONArray("weathercode")),
                    relativeHumidity2m = jsonArrayToIntList(hourlyObj.optJSONArray("relative_humidity_2m")),
                    windSpeed10m = jsonArrayToDoubleList(hourlyObj.optJSONArray("wind_speed_10m"))
                )
            } else null

            val dailyObj = dataObj.optJSONObject("daily")
            val dailyDto = if (dailyObj != null) {
                WeatherMetroDailyDto(
                    time = jsonArrayToStringList(dailyObj.optJSONArray("time")),
                    temperature2mMax = jsonArrayToDoubleList(dailyObj.optJSONArray("temperature_2m_max")),
                    temperature2mMin = jsonArrayToDoubleList(dailyObj.optJSONArray("temperature_2m_min")),
                    precipitationProbabilityMax = jsonArrayToIntList(dailyObj.optJSONArray("precipitation_probability_max")),
                    weathercode = jsonArrayToIntList(dailyObj.optJSONArray("weathercode")),
                    sunrise = jsonArrayToStringList(dailyObj.optJSONArray("sunrise")),
                    sunset = jsonArrayToStringList(dailyObj.optJSONArray("sunset")),
                    windSpeed10mMax = jsonArrayToDoubleList(dailyObj.optJSONArray("wind_speed_10m_max"))
                )
            } else null

            val parsedResponse = WeatherMetroResponse(
                latitude = dataObj.optDouble("latitude", latitude),
                longitude = dataObj.optDouble("longitude", longitude),
                generationtimeMs = dataObj.optDouble("generationtime_ms"),
                utcOffsetSeconds = dataObj.optInt("utc_offset_seconds", 0),
                timezone = dataObj.optString("timezone", timezone),
                timezoneAbbreviation = if (dataObj.has("timezone_abbreviation")) dataObj.optString("timezone_abbreviation") else null,
                hourly = hourlyDto,
                daily = dailyDto
            )

            Result.success(parsedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("User-Agent", "WeatherApp-Android/1.0")
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errorBody = errorStream?.bufferedReader()?.use { it.readText() } ?: "No response body"
                throw WeatherApiException(responseCode, errorBody)
            }

            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i, ""))
        }
        return list
    }

    private fun jsonArrayToDoubleList(arr: JSONArray?): List<Double> {
        if (arr == null) return emptyList()
        val list = ArrayList<Double>(arr.length())
        for (i in 0 until arr.length()) {
            list.add(arr.optDouble(i, 0.0))
        }
        return list
    }

    private fun jsonArrayToIntList(arr: JSONArray?): List<Int> {
        if (arr == null) return emptyList()
        val list = ArrayList<Int>(arr.length())
        for (i in 0 until arr.length()) {
            list.add(arr.optInt(i, 0))
        }
        return list
    }
}
