package com.weather.app.data.remote

import com.weather.app.data.remote.dto.WeatherMetroDailyDto
import com.weather.app.data.remote.dto.WeatherMetroHourlyDto
import com.weather.app.data.remote.dto.WeatherMetroResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
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
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,weathercode,sunrise,sunset,wind_speed_10m_max" +
                    "&hourly=temperature_2m,apparent_temperature,surface_pressure,uv_index,precipitation_probability,weathercode,relative_humidity_2m,wind_speed_10m" +
                    "&days=16" +
                    "&timezone=$encodedTz"

            if (apiKey.isNotBlank()) {
                val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
                urlString += "&api_key=$encodedKey"
            }

            val jsonString = httpGet(urlString)
            val root = JSONObject(jsonString)

            // Перевіряємо прапорець помилки у тілі відповіді сервера
            if (root.optBoolean("error", false)) {
                val reason = root.optString("reason", "WeatherMetro API повернуло статус помилки")
                throw WeatherApiException(400, reason)
            }

            val responseObj = root.optJSONObject("response")
            val dataObj = responseObj?.optJSONObject("data") ?: root

            // Сувора валідація наявності обов'язкових об'єктів
            val hourlyObj = dataObj.optJSONObject("hourly")
                ?: throw WeatherApiException(200, "Відповідь API не містить обов'язкового об'єкта 'hourly'")

            val dailyObj = dataObj.optJSONObject("daily")
                ?: throw WeatherApiException(200, "Відповідь API не містить обов'язкового об'єкта 'daily'")

            val hourlyTime = jsonArrayToStringList(hourlyObj.optJSONArray("time"))
            val hourlyTemp = jsonArrayToNullableDoubleList(hourlyObj.optJSONArray("temperature_2m"))

            if (hourlyTime.isEmpty() || hourlyTemp.isEmpty() || hourlyTemp.all { it == null }) {
                throw WeatherApiException(200, "Погодинний масив API не містить обов'язкових полів time або temperature_2m")
            }

            val hourlyDto = WeatherMetroHourlyDto(
                time = hourlyTime,
                temperature2m = hourlyTemp,
                apparentTemperature = jsonArrayToNullableDoubleList(hourlyObj.optJSONArray("apparent_temperature")),
                precipitationProbability = jsonArrayToNullableIntList(hourlyObj.optJSONArray("precipitation_probability")),
                weathercode = jsonArrayToNullableIntList(hourlyObj.optJSONArray("weathercode")),
                relativeHumidity2m = jsonArrayToNullableIntList(hourlyObj.optJSONArray("relative_humidity_2m")),
                windSpeed10m = jsonArrayToNullableDoubleList(hourlyObj.optJSONArray("wind_speed_10m")),
                surfacePressure = jsonArrayToNullableDoubleList(hourlyObj.optJSONArray("surface_pressure")),
                uvIndex = jsonArrayToNullableDoubleList(hourlyObj.optJSONArray("uv_index"))
            )

            val dailyTime = jsonArrayToStringList(dailyObj.optJSONArray("time"))
            val dailyTempMax = jsonArrayToNullableDoubleList(dailyObj.optJSONArray("temperature_2m_max"))
            val dailyTempMin = jsonArrayToNullableDoubleList(dailyObj.optJSONArray("temperature_2m_min"))

            if (dailyTime.isEmpty() || dailyTempMax.isEmpty() || dailyTempMin.isEmpty() ||
                dailyTempMax.all { it == null } || dailyTempMin.all { it == null }
            ) {
                throw WeatherApiException(200, "Щоденний масив API не містить обов'язкових полів time або temperature_2m_max/min")
            }

            val dailyDto = WeatherMetroDailyDto(
                time = dailyTime,
                temperature2mMax = dailyTempMax,
                temperature2mMin = dailyTempMin,
                precipitationSum = jsonArrayToNullableDoubleList(dailyObj.optJSONArray("precipitation_sum")),
                precipitationProbabilityMax = jsonArrayToNullableIntList(dailyObj.optJSONArray("precipitation_probability_max")),
                weathercode = jsonArrayToNullableIntList(dailyObj.optJSONArray("weathercode")),
                sunrise = jsonArrayToNullableStringList(dailyObj.optJSONArray("sunrise")),
                sunset = jsonArrayToNullableStringList(dailyObj.optJSONArray("sunset")),
                windSpeed10mMax = jsonArrayToNullableDoubleList(dailyObj.optJSONArray("wind_speed_10m_max"))
            )

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
        } catch (e: SocketTimeoutException) {
            throw WeatherApiException(408, "Таймаут з'єднання з погодним сервером: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            if (arr.isNull(i)) {
                throw JSONException("Елемент $i у масиві часу/дат є null")
            }
            val str = arr.getString(i)
            if (str.isBlank()) {
                throw JSONException("Елемент $i у масиві часу/дат є порожнім")
            }
            list.add(str)
        }
        return list
    }

    private fun jsonArrayToNullableStringList(arr: JSONArray?): List<String?> {
        if (arr == null) return emptyList()
        val list = ArrayList<String?>(arr.length())
        for (i in 0 until arr.length()) {
            if (arr.isNull(i)) {
                list.add(null)
            } else {
                val str = arr.optString(i, "")
                list.add(str.takeIf { it.isNotBlank() })
            }
        }
        return list
    }

    private fun jsonArrayToDoubleList(arr: JSONArray?): List<Double> {
        if (arr == null) return emptyList()
        val list = ArrayList<Double>(arr.length())
        for (i in 0 until arr.length()) {
            if (arr.isNull(i)) {
                throw JSONException("Елемент $i у числовому масиві є null")
            }
            list.add(arr.getDouble(i))
        }
        return list
    }

    private fun jsonArrayToNullableDoubleList(arr: JSONArray?): List<Double?> {
        if (arr == null) return emptyList()
        val list = ArrayList<Double?>(arr.length())
        for (i in 0 until arr.length()) {
            if (arr.isNull(i)) {
                list.add(null)
            } else {
                val d = arr.optDouble(i)
                if (d.isNaN()) {
                    list.add(null)
                } else {
                    list.add(d)
                }
            }
        }
        return list
    }

    internal fun jsonArrayToNullableIntList(arr: JSONArray?): List<Int?> {
        if (arr == null) return emptyList()
        val list = ArrayList<Int?>(arr.length())
        for (i in 0 until arr.length()) {
            if (arr.isNull(i)) {
                list.add(null)
            } else {
                val obj = arr.opt(i)
                when (obj) {
                    is Number -> {
                        val d = obj.toDouble()
                        // WMO коди є строго цілими числами (0..99). Дробові числа (наприклад, 0.5 або 1.7) є невалідними
                        // і не повинні автоматично усікатися до 0 (Clear Sky).
                        if (d == d.toLong().toDouble() && !d.isNaN() && !d.isInfinite()) {
                            list.add(d.toInt())
                        } else {
                            list.add(null)
                        }
                    }
                    is String -> list.add(obj.toIntOrNull())
                    else -> list.add(null)
                }
            }
        }
        return list
    }
}
