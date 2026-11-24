package com.weather.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.data.location.ReverseGeocoder
import com.weather.app.data.repository.LocationRepositoryImpl
import com.weather.app.data.repository.WeatherRepositoryImpl
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.repository.LocationRepository
import com.weather.app.domain.repository.WeatherRepository
import com.weather.app.ui.state.WeatherUiState
import com.weather.app.widget.WeatherWidgetScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * ViewModel головного екрана погоди з підтримкою реального API Open-Meteo,
 * зворотного геокодування GPS (визначення конкретних сіл/міст) та пошуку.
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl(),
    private val locationRepository: LocationRepository = LocationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _selectedCities = MutableStateFlow<List<LocationModel>>(emptyList())
    val selectedCities: StateFlow<List<LocationModel>> = _selectedCities.asStateFlow()

    private val _cityForecasts = MutableStateFlow<Map<String, FullWeatherForecast>>(emptyMap())
    val cityForecasts: StateFlow<Map<String, FullWeatherForecast>> = _cityForecasts.asStateFlow()
    private val inFlightCityPreloads = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        const val MAX_CITY_CARD_API_PRELOADS_PER_BATCH = 3

        fun cityCacheKey(location: LocationModel): String {
            return String.format(java.util.Locale.US, "%.2f_%.2f", location.latitude, location.longitude)
        }
    }

    val popularCities: List<LocationModel> = listOf(
        LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            admin1 = "Київ",
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 3088006L,
            name = "Пшецлав (Przecław)",
            country = "Польща",
            admin1 = "Західнопоморське воєводство (передмістя Щецина)",
            latitude = 53.3745,
            longitude = 14.4725,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 3083829L,
            name = "Щецин (Szczecin)",
            country = "Польща",
            admin1 = "Західнопоморське воєводство",
            latitude = 53.4285,
            longitude = 14.5528,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 756135L,
            name = "Варшава (Warszawa)",
            country = "Польща",
            admin1 = "Мазовецьке воєводство",
            latitude = 52.2297,
            longitude = 21.0122,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 702550L,
            name = "Львів",
            country = "Україна",
            admin1 = "Львівська область",
            latitude = 49.8397,
            longitude = 24.0297,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 698740L,
            name = "Одеса",
            country = "Україна",
            admin1 = "Одеська область",
            latitude = 46.4825,
            longitude = 30.7233,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 706483L,
            name = "Харків",
            country = "Україна",
            admin1 = "Харківська область",
            latitude = 49.9935,
            longitude = 36.2304,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 709930L,
            name = "Дніпро",
            country = "Україна",
            admin1 = "Дніпропетровська область",
            latitude = 48.4647,
            longitude = 35.0462,
            timezone = "Europe/Kyiv"
        )
    )

    private var currentLocation: LocationModel = popularCities.first()
    private var fetchForecastJob: Job? = null

    init {
        observeCachedForecast()
        loadForecast(currentLocation)
    }

    private fun isSameCity(loc1: LocationModel, loc2: LocationModel): Boolean {
        if (loc1.id != 0L && loc2.id != 0L && loc1.id == loc2.id) return true
        val nameMatch = loc1.name.equals(loc2.name, ignoreCase = true)
        val coordMatch = abs(loc1.latitude - loc2.latitude) < 0.05 && abs(loc1.longitude - loc2.longitude) < 0.05
        return nameMatch || coordMatch
    }

    private fun observeCachedForecast() {
        viewModelScope.launch {
            weatherRepository.observeCurrentForecast()
                .catch { /* ігноруємо помилки спостереження */ }
                .collect { forecast ->
                    if (forecast != null && isSameCity(forecast.location, currentLocation) && _uiState.value is WeatherUiState.Loading) {
                        _uiState.value = WeatherUiState.Success(
                            forecast = forecast,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun loadForecast(
        location: LocationModel = currentLocation,
        forceRefresh: Boolean = false,
        onLoaded: ((FullWeatherForecast) -> Unit)? = null
    ) {
        fetchForecastJob?.cancel()
        currentLocation = location
        val currentState = _uiState.value
        val currentSuccess = currentState as? WeatherUiState.Success
        val isSameCity = currentSuccess?.forecast?.location?.let { isSameCity(it, location) } == true

        if (forceRefresh && isSameCity) {
            _uiState.value = currentSuccess.copy(isRefreshing = true)
        } else {
            _uiState.value = WeatherUiState.Loading
        }

        fetchForecastJob = viewModelScope.launch {
            val result = weatherRepository.getFullForecast(location, forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { forecast ->
                    _uiState.value = WeatherUiState.Success(
                        forecast = forecast,
                        isRefreshing = false,
                        isOffline = forecast.isStale
                    )
                    updateCityForecastEntry(location, forecast)
                    updateCityForecastEntry(forecast.location, forecast)
                    onLoaded?.invoke(forecast)
                },
                onFailure = { error ->
                    val lastForecast = if (isSameCity) currentSuccess?.forecast else null
                    _uiState.value = WeatherUiState.Error(
                        message = error.message ?: "Помилка завантаження погодних даних",
                        canRetry = true,
                        lastCachedForecast = lastForecast
                    )
                }
            )
        }
    }

    fun refresh(onLoaded: ((FullWeatherForecast) -> Unit)? = null) {
        loadForecast(currentLocation, forceRefresh = true, onLoaded = onLoaded)
    }

    fun loadSelectedCities(context: Context) {
        val loaded = WeatherLocalCache.loadSelectedCities(context)
        _selectedCities.value = loaded
        refreshCachedCityForecasts(context, loaded)
    }

    private fun updateCityForecastEntry(location: LocationModel, forecast: FullWeatherForecast) {
        val key = cityCacheKey(location)
        val updated = _cityForecasts.value.toMutableMap()
        updated[key] = forecast
        _cityForecasts.value = updated
    }

    fun refreshCachedCityForecasts(
        context: Context? = null,
        cities: List<LocationModel> = _selectedCities.value
    ) {
        val updated = _cityForecasts.value.toMutableMap()
        val activeSuccess = (_uiState.value as? WeatherUiState.Success)?.forecast
        if (activeSuccess != null) {
            updated[cityCacheKey(activeSuccess.location)] = activeSuccess
            updated[cityCacheKey(currentLocation)] = activeSuccess
        }
        for (city in cities) {
            val cached = WeatherLocalCache.loadCachedForecastForLocation(context, city)
            if (cached != null) {
                updated[cityCacheKey(city)] = cached
            }
        }
        _cityForecasts.value = updated
    }

    /**
     * Обмежене фонове довантаження прогнозів для збережених міст на вкладці «Місця»,
     * які ще не мають власного локального кешу (максимум [MAX_CITY_CARD_API_PRELOADS_PER_BATCH] запити),
     * відповідно до вимоги Власника (Stage B — Requirement 6: реальні дані або власний кеш міста з обмеженням API-запитів).
     */
    fun preloadMissingCityForecasts(context: Context? = null) {
        val cities = _selectedCities.value
        refreshCachedCityForecasts(context, cities)

        val currentMap = _cityForecasts.value
        val missingCities = cities.filter { city ->
            val key = cityCacheKey(city)
            !currentMap.containsKey(key) && !inFlightCityPreloads.contains(key)
        }.take(MAX_CITY_CARD_API_PRELOADS_PER_BATCH)

        for (city in missingCities) {
            val key = cityCacheKey(city)
            inFlightCityPreloads.add(key)
            viewModelScope.launch {
                try {
                    val result = weatherRepository.getFullForecast(city, forceRefresh = false)
                    result.onSuccess { forecast ->
                        WeatherLocalCache.saveForecastForLocation(context, city, forecast)
                        updateCityForecastEntry(city, forecast)
                    }
                } finally {
                    inFlightCityPreloads.remove(key)
                }
            }
        }
    }

    fun removeSelectedCity(context: Context?, city: LocationModel) {
        if (context != null) {
            WeatherLocalCache.removeSelectedCity(context, city)
            _selectedCities.value = WeatherLocalCache.loadSelectedCities(context)
        } else {
            val currentList = _selectedCities.value.toMutableList()
            currentList.removeAll { (it.id != 0L && it.id == city.id) || it.name.equals(city.name, ignoreCase = true) }
            _selectedCities.value = currentList
        }
    }

    fun selectLocation(
        location: LocationModel,
        context: Context? = null,
        onLoaded: ((FullWeatherForecast) -> Unit)? = null
    ) {
        if (context != null) {
            WeatherLocalCache.addSelectedCity(context, location)
            _selectedCities.value = WeatherLocalCache.loadSelectedCities(context)
        } else {
            val currentList = _selectedCities.value.toMutableList()
            currentList.removeAll { (it.id != 0L && it.id == location.id) || it.name.equals(location.name, ignoreCase = true) }
            currentList.add(0, location)
            _selectedCities.value = currentList
        }
        loadForecast(location, forceRefresh = false, onLoaded = onLoaded)
    }

    suspend fun searchCities(query: String): List<LocationModel> {
        val result = locationRepository.searchCities(query)
        return result.getOrDefault(emptyList())
    }

    /**
     * Отримує точні координати з GPS та розпізнає назву села / селища / міста
     * через системний Android Geocoder або онлайн Reverse Geocoding.
     */
    fun onGpsLocationAcquired(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val resolvedLocation = ReverseGeocoder.resolveLocation(context, latitude, longitude)
            WeatherLocalCache.saveActiveLocation(context, resolvedLocation)
            WeatherLocalCache.addSelectedCity(context, resolvedLocation)
            _selectedCities.value = WeatherLocalCache.loadSelectedCities(context)
            selectLocation(resolvedLocation, context = context, onLoaded = { forecast ->
                WeatherWidgetScheduler.updateWidgetNow(context, forecast)
            })
        }
    }
}
