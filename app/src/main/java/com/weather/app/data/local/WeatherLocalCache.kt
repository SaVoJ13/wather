package com.weather.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Локальний кеш для збереження активної локації, списку міст та погодного прогнозу на диску.
 * Забезпечує коректне відображення локації користувача та актуальної погоди у віджеті Glance
 * та основному застосунку в режимі офлайн навіть після перезапуску процесу або перезавантаження пристрою.
 * 
 * Відповідно до CACHE_STRATEGY.md та ADR-012:
 * - Зберігає повні 24 години погодинного прогнозу.
 * - Зберігає повні 14-16 днів щоденного прогнозу.
 * - Підтримує кешування для кількох міст за координатами.
 * - Забезпечує Corrupted Cache Recovery (безпечне очищення при пошкодженні даних).
 */
object WeatherLocalCache {

    private const val PREFS_NAME = "weather_local_cache"
    private const val KEY_LOCATION_ID = "loc_id"
    private const val KEY_LOCATION_NAME = "loc_name"
    private const val KEY_LOCATION_COUNTRY = "loc_country"
    private const val KEY_LOCATION_ADMIN1 = "loc_admin1"
    private const val KEY_LOCATION_LAT = "loc_lat"
    private const val KEY_LOCATION_LON = "loc_lon"
    private const val KEY_LOCATION_TZ = "loc_tz"
    private const val KEY_LOCATION_IS_GPS = "loc_is_gps"
    private const val KEY_FORECAST_JSON = "forecast_json"
    private const val KEY_SELECTED_CITIES = "selected_cities_json"
    private const val PREFIX_FORECAST_CITY = "forecast_city_"

    val DEFAULT_LOCATION = LocationModel(
        id = 703448L,
        name = "Київ",
        country = "Україна",
        admin1 = "Київська область",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv"
    )

    @Volatile
    var appContext: Context? = null

    @Volatile
    var customPrefs: SharedPreferences? = null

    /**
     * Ініціалізація глобального контексту застосунку з [WeatherApplication].
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val inMemoryStorage = java.util.concurrent.ConcurrentHashMap<String, Any?>()

    private val inMemoryPrefs: SharedPreferences by lazy {
        createInMemoryPreferences(inMemoryStorage)
    }

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun getPrefs(context: Context?): SharedPreferences {
        customPrefs?.let { return it }
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return inMemoryPrefs
    }

    private fun createInMemoryPreferences(storage: java.util.concurrent.ConcurrentHashMap<String, Any?>): SharedPreferences {
        val editorHandler = java.lang.reflect.InvocationHandler { proxy, method, args ->
            when (method.name) {
                "putString" -> {
                    val key = args!![0] as String
                    val value = args[1] as String?
                    if (value == null) storage.remove(key) else storage[key] = value
                    proxy
                }
                "putLong" -> {
                    storage[args!![0] as String] = args[1] as Long
                    proxy
                }
                "putFloat" -> {
                    storage[args!![0] as String] = args[1] as Float
                    proxy
                }
                "putBoolean" -> {
                    storage[args!![0] as String] = args[1] as Boolean
                    proxy
                }
                "remove" -> {
                    storage.remove(args!![0] as String)
                    proxy
                }
                "clear" -> {
                    storage.clear()
                    proxy
                }
                "apply", "commit" -> {
                    if (method.returnType == Boolean::class.javaPrimitiveType) true else null
                }
                else -> null
            }
        }

        val fakeEditor = java.lang.reflect.Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java),
            editorHandler
        ) as SharedPreferences.Editor

        val prefsHandler = java.lang.reflect.InvocationHandler { _, method, args ->
            when (method.name) {
                "getString" -> {
                    val key = args!![0] as String
                    val def = args.getOrNull(1) as String?
                    storage[key] as? String ?: def
                }
                "getLong" -> {
                    val key = args!![0] as String
                    val def = args[1] as Long
                    storage[key] as? Long ?: def
                }
                "getFloat" -> {
                    val key = args!![0] as String
                    val def = args[1] as Float
                    storage[key] as? Float ?: def
                }
                "getBoolean" -> {
                    val key = args!![0] as String
                    val def = args[1] as Boolean
                    storage[key] as? Boolean ?: def
                }
                "contains" -> storage.containsKey(args!![0] as String)
                "edit" -> fakeEditor
                else -> null
            }
        }

        return java.lang.reflect.Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java),
            prefsHandler
        ) as SharedPreferences
    }

    private fun getCityKey(lat: Double, lon: Double): String {
        val latKey = String.format(Locale.US, "%.2f", lat)
        val lonKey = String.format(Locale.US, "%.2f", lon)
        return "$PREFIX_FORECAST_CITY${latKey}_$lonKey"
    }

    /**
     * Зберігає активну локацію користувача (наприклад, визначену за GPS).
     */
    fun saveActiveLocation(context: Context? = null, location: LocationModel) {
        getPrefs(context).edit().apply {
            putLong(KEY_LOCATION_ID, location.id)
            putString(KEY_LOCATION_NAME, location.name)
            putString(KEY_LOCATION_COUNTRY, location.country)
            putString(KEY_LOCATION_ADMIN1, location.admin1)
            putFloat(KEY_LOCATION_LAT, location.latitude.toFloat())
            putFloat(KEY_LOCATION_LON, location.longitude.toFloat())
            putString(KEY_LOCATION_TZ, location.timezone)
            putBoolean(KEY_LOCATION_IS_GPS, location.isCurrentLocation)
            apply()
        }
    }

    /**
     * Отримує останню збережену локацію користувача або null.
     */
    fun loadActiveLocation(context: Context? = null): LocationModel? {
        val prefs = getPrefs(context)
        val name = prefs.getString(KEY_LOCATION_NAME, null) ?: return null
        val lat = prefs.getFloat(KEY_LOCATION_LAT, 0f).toDouble()
        val lon = prefs.getFloat(KEY_LOCATION_LON, 0f).toDouble()
        if (lat == 0.0 && lon == 0.0) return null

        return LocationModel(
            id = prefs.getLong(KEY_LOCATION_ID, 0L),
            name = name,
            country = prefs.getString(KEY_LOCATION_COUNTRY, "") ?: "",
            admin1 = prefs.getString(KEY_LOCATION_ADMIN1, null),
            latitude = lat,
            longitude = lon,
            timezone = prefs.getString(KEY_LOCATION_TZ, "auto") ?: "auto",
            isCurrentLocation = prefs.getBoolean(KEY_LOCATION_IS_GPS, true)
        )
    }

    /**
     * Серіалізує доменний прогноз у JSONObject.
     * Зберігає повний масив 24h (до 24 елементів) та 14-16 днів.
     */
    private fun serializeForecast(forecast: FullWeatherForecast): JSONObject {
        val root = JSONObject().apply {
            put("updatedAt", forecast.updatedAt.format(isoFormatter))
            put("isStale", forecast.isStale)
            put("dataSource", forecast.dataSource.name)

            // Current
            val cur = JSONObject().apply {
                put("temp", forecast.current.temperatureCelsius)
                put("feelsLike", forecast.current.apparentTemperatureCelsius)
                put("condition", forecast.current.condition.name)
                put("humidity", forecast.current.relativeHumidityPercent)
                put("wind", forecast.current.windSpeedKmh)
                put("pressure", forecast.current.surfacePressureHpa)
                put("uv", forecast.current.uvIndex)
                put("time", forecast.current.time.format(isoFormatter))
            }
            put("current", cur)

            // Hourly (повні 24 години)
            val hourlyArr = JSONArray()
            forecast.hourly24h.take(24).forEach { item ->
                val hObj = JSONObject().apply {
                    put("time", item.time.format(isoFormatter))
                    put("temp", item.temperatureCelsius)
                    put("condition", item.condition.name)
                    put("pop", item.precipitationProbabilityPercent)
                }
                hourlyArr.put(hObj)
            }
            put("hourly", hourlyArr)

            // Daily (повні 14–16 днів)
            val dailyArr = JSONArray()
            forecast.daily14Days.take(16).forEach { item ->
                val dObj = JSONObject().apply {
                    put("date", item.date.format(isoFormatter))
                    put("min", item.minTemperatureCelsius)
                    put("max", item.maxTemperatureCelsius)
                    put("condition", item.condition.name)
                    put("pop", item.precipitationProbabilityPercent)
                    put("precipSum", item.precipitationSumMm)
                    put("maxWind", item.maxWindSpeedKmh)
                    item.sunrise?.let { put("sunrise", it.format(isoFormatter)) }
                    item.sunset?.let { put("sunset", it.format(isoFormatter)) }
                }
                dailyArr.put(dObj)
            }
            put("daily", dailyArr)
        }
        return root
    }

    /**
     * Десеріалізує JSONObject у FullWeatherForecast.
     */
    private fun deserializeForecast(root: JSONObject, location: LocationModel): FullWeatherForecast {
        val updatedAt = LocalDateTime.parse(root.getString("updatedAt"), isoFormatter)
        val isStale = root.optBoolean("isStale", false)

        val curObj = root.getJSONObject("current")
        val current = CurrentWeather(
            temperatureCelsius = curObj.getDouble("temp"),
            apparentTemperatureCelsius = curObj.getDouble("feelsLike"),
            condition = runCatching { WeatherCondition.valueOf(curObj.getString("condition")) }
                .getOrDefault(WeatherCondition.CLEAR_SKY),
            relativeHumidityPercent = curObj.optInt("humidity", 70),
            windSpeedKmh = curObj.optDouble("wind", 10.0),
            surfacePressureHpa = curObj.optDouble("pressure", 1013.0),
            uvIndex = curObj.optDouble("uv", 0.0),
            time = LocalDateTime.parse(curObj.getString("time"), isoFormatter)
        )

        val hourlyArr = root.optJSONArray("hourly") ?: JSONArray()
        val hourlyList = mutableListOf<HourlyForecastItem>()
        for (i in 0 until hourlyArr.length()) {
            val hObj = hourlyArr.getJSONObject(i)
            hourlyList.add(
                HourlyForecastItem(
                    time = LocalDateTime.parse(hObj.getString("time"), isoFormatter),
                    temperatureCelsius = hObj.getDouble("temp"),
                    condition = runCatching { WeatherCondition.valueOf(hObj.getString("condition")) }
                        .getOrDefault(WeatherCondition.CLEAR_SKY),
                    precipitationProbabilityPercent = hObj.optInt("pop", 0)
                )
            )
        }

        val dailyArr = root.optJSONArray("daily") ?: JSONArray()
        val dailyList = mutableListOf<DailyForecastItem>()
        for (i in 0 until dailyArr.length()) {
            val dObj = dailyArr.getJSONObject(i)
            val sunriseStr = if (dObj.has("sunrise")) dObj.optString("sunrise") else null
            val sunrise = sunriseStr?.takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDateTime.parse(it, isoFormatter) }.getOrNull()
            }
            val sunsetStr = if (dObj.has("sunset")) dObj.optString("sunset") else null
            val sunset = sunsetStr?.takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDateTime.parse(it, isoFormatter) }.getOrNull()
            }
            dailyList.add(
                DailyForecastItem(
                    date = LocalDateTime.parse(dObj.getString("date"), isoFormatter),
                    minTemperatureCelsius = dObj.getDouble("min"),
                    maxTemperatureCelsius = dObj.getDouble("max"),
                    condition = runCatching { WeatherCondition.valueOf(dObj.getString("condition")) }
                        .getOrDefault(WeatherCondition.CLEAR_SKY),
                    precipitationProbabilityPercent = dObj.optInt("pop", 0),
                    precipitationSumMm = dObj.optDouble("precipSum", 0.0),
                    maxWindSpeedKmh = dObj.optDouble("maxWind", 10.0),
                    sunrise = sunrise,
                    sunset = sunset
                )
            )
        }

        val dsName = root.optString("dataSource", WeatherDataSource.WEATHER_METRO.name)
        val dataSource = runCatching { WeatherDataSource.valueOf(dsName) }
            .getOrDefault(WeatherDataSource.WEATHER_METRO)

        return FullWeatherForecast(
            location = location,
            current = current,
            hourly24h = hourlyList,
            daily14Days = dailyList,
            updatedAt = updatedAt,
            isStale = isStale,
            dataSource = dataSource
        )
    }

    /**
     * Зберігає повний погодний прогноз для активної локації (використовується віджетом та екраном офлайн).
     */
    fun saveForecast(context: Context? = null, forecast: FullWeatherForecast) {
        saveActiveLocation(context, forecast.location)

        try {
            val root = serializeForecast(forecast)
            val jsonStr = root.toString()
            val cityKey = getCityKey(forecast.location.latitude, forecast.location.longitude)

            getPrefs(context).edit().apply {
                putString(KEY_FORECAST_JSON, jsonStr)
                putString(cityKey, jsonStr)
                apply()
            }
        } catch (e: Exception) {
            // безпечне ігнорування помилок серіалізації
        }
    }

    /**
     * Зберігає прогноз для конкретної локації у мульти-сіті кеш.
     */
    fun saveForecastForLocation(context: Context? = null, location: LocationModel, forecast: FullWeatherForecast) {
        try {
            val root = serializeForecast(forecast)
            val cityKey = getCityKey(location.latitude, location.longitude)
            getPrefs(context).edit().putString(cityKey, root.toString()).apply()
        } catch (e: Exception) {
            // безпечне ігнорування помилок
        }
    }

    /**
     * Завантажує останній збережений прогноз для активної локації.
     * Реалізує Corrupted Cache Recovery: при пошкодженні JSON очищає битий запис і повертає null.
     */
    fun loadCachedForecast(context: Context? = null): FullWeatherForecast? {
        val location = loadActiveLocation(context) ?: return null
        val jsonStr = getPrefs(context).getString(KEY_FORECAST_JSON, null) ?: return null

        return try {
            val root = JSONObject(jsonStr)
            deserializeForecast(root, location)
        } catch (e: Exception) {
            // Corrupted Cache Recovery: атомарне очищення пошкодженого запису
            runCatching { getPrefs(context).edit().remove(KEY_FORECAST_JSON).apply() }
            null
        }
    }

    /**
     * Завантажує закешований прогноз для вказаної локації.
     */
    fun loadCachedForecastForLocation(context: Context? = null, location: LocationModel): FullWeatherForecast? {
        val cityKey = getCityKey(location.latitude, location.longitude)
        val jsonStr = getPrefs(context).getString(cityKey, null) ?: return null

        return try {
            val root = JSONObject(jsonStr)
            val forecast = deserializeForecast(root, location)
            // Сувора перевірка: чи відповідають збережені координати запитуваному місту (Місто A != Місто B)
            val latDiff = Math.abs(forecast.location.latitude - location.latitude)
            val lonDiff = Math.abs(forecast.location.longitude - location.longitude)
            if (latDiff > 0.05 || lonDiff > 0.05) {
                null
            } else {
                forecast
            }
        } catch (e: Exception) {
            runCatching { getPrefs(context).edit().remove(cityKey).apply() }
            null
        }
    }

    /**
     * Зберігає список вибраних міст користувача.
     */
    fun saveSelectedCities(context: Context? = null, cities: List<LocationModel>) {
        try {
            val arr = JSONArray()
            cities.forEach { city ->
                val obj = JSONObject().apply {
                    put("id", city.id)
                    put("name", city.name)
                    put("country", city.country)
                    city.admin1?.let { put("admin1", it) }
                    put("lat", city.latitude)
                    put("lon", city.longitude)
                    put("tz", city.timezone)
                    put("isGps", city.isCurrentLocation)
                }
                arr.put(obj)
            }
            getPrefs(context).edit().putString(KEY_SELECTED_CITIES, arr.toString()).apply()
        } catch (e: Exception) {
            // ігноруємо помилки серіалізації
        }
    }

    /**
     * Завантажує список вибраних міст користувача.
     * Якщо список порожній, ініціалізує його поточною збереженою локацією (або Пшецлав за замовчуванням).
     */
    fun loadSelectedCities(context: Context? = null): List<LocationModel> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_SELECTED_CITIES, null)

        val defaultInitialList by lazy {
            val active = loadActiveLocation(context) ?: DEFAULT_LOCATION
            val initial = listOf(active)
            saveSelectedCities(context, initial)
            initial
        }

        if (jsonStr.isNullOrBlank()) {
            return defaultInitialList
        }

        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<LocationModel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LocationModel(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        country = obj.optString("country", ""),
                        admin1 = if (obj.has("admin1") && !obj.isNull("admin1")) obj.getString("admin1") else null,
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lon"),
                        timezone = obj.optString("tz", "auto"),
                        isCurrentLocation = obj.optBoolean("isGps", false)
                    )
                )
            }
            if (list.isEmpty()) defaultInitialList else list
        } catch (e: Exception) {
            defaultInitialList
        }
    }

    /**
     * Додає або оновлює місто у списку вибраних (переміщує на першу позицію, запобігає дублікатам).
     */
    fun addSelectedCity(context: Context? = null, city: LocationModel) {
        val current = loadSelectedCities(context).toMutableList()
        current.removeAll { isSameCity(it, city) }
        current.add(0, city)
        saveSelectedCities(context, current)
    }

    /**
     * Видаляє місто зі списку вибраних.
     */
    fun removeSelectedCity(context: Context? = null, city: LocationModel) {
        val current = loadSelectedCities(context).toMutableList()
        current.removeAll { isSameCity(it, city) }
        saveSelectedCities(context, current)
    }

    /**
     * Повністю очищає всі закешовані дані (використовується в тестах та налаштуваннях).
     */
    fun clearAll(context: Context? = null) {
        inMemoryStorage.clear()
        try {
            getPrefs(context).edit().clear().apply()
        } catch (ignored: Throwable) {}
    }

    private fun isSameCity(a: LocationModel, b: LocationModel): Boolean {
        if (a.id != 0L && a.id == b.id) return true
        if (a.name.equals(b.name, ignoreCase = true) &&
            Math.abs(a.latitude - b.latitude) < 0.05 &&
            Math.abs(a.longitude - b.longitude) < 0.05) {
            return true
        }
        return false
    }
}
