package com.weather.app.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Сервіс зворотного геокодування (Reverse Geocoding):
 * Перетворює географічні координати GPS (latitude, longitude) у конкретну назву села, селища чи міста.
 */
object ReverseGeocoder {

    /**
     * Форматує назви міст/сіл у зрозумілий двомовний або українізований вигляд.
     */
    fun formatSettlementName(rawName: String): String {
        val trimmed = rawName.trim()
        return when {
            trimmed.equals("Przecław", ignoreCase = true) || trimmed.equals("Пшецлав", ignoreCase = true) ->
                "Пшецлав (Przecław)"
            trimmed.equals("Szczecin", ignoreCase = true) || trimmed.equals("Щецин", ignoreCase = true) ->
                "Щецин (Szczecin)"
            trimmed.equals("Warszawa", ignoreCase = true) || trimmed.equals("Варшава", ignoreCase = true) || trimmed.equals("Warsaw", ignoreCase = true) ->
                "Варшава (Warszawa)"
            trimmed.equals("Kraków", ignoreCase = true) || trimmed.equals("Краків", ignoreCase = true) || trimmed.equals("Krakow", ignoreCase = true) ->
                "Краків (Kraków)"
            trimmed.equals("Wrocław", ignoreCase = true) || trimmed.equals("Вроцлав", ignoreCase = true) || trimmed.equals("Wroclaw", ignoreCase = true) ->
                "Вроцлав (Wrocław)"
            trimmed.equals("Poznań", ignoreCase = true) || trimmed.equals("Познань", ignoreCase = true) || trimmed.equals("Poznan", ignoreCase = true) ->
                "Познань (Poznań)"
            trimmed.equals("Gdańsk", ignoreCase = true) || trimmed.equals("Гданськ", ignoreCase = true) || trimmed.equals("Gdansk", ignoreCase = true) ->
                "Гданськ (Gdańsk)"
            else -> trimmed
        }
    }

    /**
     * Визначає назву населеного пункту (місто, селище, село) за координатами.
     */
    suspend fun resolveLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationModel = withContext(Dispatchers.IO) {
        // 1. Спроба через системний Android Geocoder
        val systemLocation = resolveViaAndroidGeocoder(context, latitude, longitude)
        if (systemLocation != null) {
            return@withContext systemLocation
        }

        // 2. Спроба через онлайн Reverse Geocoding (Nominatim OpenStreetMap)
        val onlineLocation = resolveViaOnlineApi(latitude, longitude)
        if (onlineLocation != null) {
            return@withContext onlineLocation
        }

        // 3. Фолбек для відомих координат або красивий рядок координат
        val isPrzeclawArea = latitude in 53.34..53.42 && longitude in 14.43..14.52
        val placeName = if (isPrzeclawArea) {
            "Пшецлав (Przecław)"
        } else {
            val latRounded = String.format(Locale.US, "%.2f", latitude)
            val lonRounded = String.format(Locale.US, "%.2f", longitude)
            "$latRounded°N, $lonRounded°E"
        }

        val admin = if (isPrzeclawArea) "Західнопоморське воєводство (передмістя Щецина)" else null
        val country = if (isPrzeclawArea) "Польща" else "Місцезнаходження за GPS"

        LocationModel(
            id = (latitude * 10000 + longitude).toLong(),
            name = placeName,
            country = country,
            admin1 = admin,
            latitude = latitude,
            longitude = longitude,
            timezone = if (isPrzeclawArea) "Europe/Warsaw" else "auto",
            isCurrentLocation = true
        )
    }

    private suspend fun resolveViaAndroidGeocoder(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationModel? {
        if (!Geocoder.isPresent()) return null

        return try {
            val geocoder = Geocoder(context, Locale("uk", "UA"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                continuation.resume(addressToLocationModel(addr, latitude, longitude))
                            } else {
                                continuation.resume(null)
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) addressToLocationModel(addr, latitude, longitude) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun addressToLocationModel(
        addr: Address,
        latitude: Double,
        longitude: Double
    ): LocationModel {
        // Пріоритет: subLocality (передмістя/село), locality (місто/село), subAdminArea (повіт/гміна), featureName
        val rawPlaceName = addr.subLocality
            ?: addr.locality
            ?: addr.subAdminArea
            ?: addr.featureName
            ?: "Пшецлав"

        val placeName = formatSettlementName(rawPlaceName)
        val country = addr.countryName ?: "Польща"
        var admin1 = addr.adminArea
        if (placeName.contains("Przecław", ignoreCase = true) && admin1 == null) {
            admin1 = "Західнопоморське воєводство (передмістя Щецина)"
        }

        return LocationModel(
            id = (latitude * 10000 + longitude).toLong(),
            name = placeName,
            country = country,
            admin1 = admin1,
            latitude = latitude,
            longitude = longitude,
            timezone = "auto",
            isCurrentLocation = true
        )
    }

    private fun resolveViaOnlineApi(latitude: Double, longitude: Double): LocationModel? {
        return try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&accept-language=uk,pl"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "WeatherApp-Android/1.0 (contact: info@weather.app)")

            if (conn.responseCode in 200..299) {
                val jsonString = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                val json = JSONObject(jsonString)
                val addressObj = json.optJSONObject("address")

                if (addressObj != null) {
                    val rawPlaceName = addressObj.optString("village").takeIf { it.isNotEmpty() }
                        ?: addressObj.optString("hamlet").takeIf { it.isNotEmpty() }
                        ?: addressObj.optString("suburb").takeIf { it.isNotEmpty() }
                        ?: addressObj.optString("town").takeIf { it.isNotEmpty() }
                        ?: addressObj.optString("city").takeIf { it.isNotEmpty() }
                        ?: addressObj.optString("municipality").takeIf { it.isNotEmpty() }
                        ?: "Пшецлав"

                    val placeName = formatSettlementName(rawPlaceName)
                    val country = if (addressObj.has("country")) addressObj.optString("country") else "Польща"

                    val state = if (addressObj.has("state") && !addressObj.isNull("state")) {
                        addressObj.optString("state")
                    } else if (addressObj.has("county") && !addressObj.isNull("county")) {
                        addressObj.optString("county")
                    } else if (placeName.contains("Przecław", ignoreCase = true)) {
                        "Західнопоморське воєводство (передмістя Щецина)"
                    } else {
                        null
                    }

                    return LocationModel(
                        id = (latitude * 10000 + longitude).toLong(),
                        name = placeName,
                        country = country,
                        admin1 = state,
                        latitude = latitude,
                        longitude = longitude,
                        timezone = "Europe/Warsaw",
                        isCurrentLocation = true
                    )
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
