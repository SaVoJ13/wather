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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

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

    init {
        observeCachedForecast()
        loadForecast(currentLocation)
    }

    private fun observeCachedForecast() {
        viewModelScope.launch {
            weatherRepository.observeCurrentForecast()
                .catch { /* ігноруємо помилки спостереження */ }
                .collect { forecast ->
                    if (forecast != null && _uiState.value is WeatherUiState.Loading) {
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
        currentLocation = location
        val currentState = _uiState.value

        if (forceRefresh && currentState is WeatherUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        } else if (!forceRefresh && currentState !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        viewModelScope.launch {
            val result = weatherRepository.getFullForecast(location, forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { forecast ->
                    _uiState.value = WeatherUiState.Success(
                        forecast = forecast,
                        isRefreshing = false,
                        isOffline = forecast.isStale
                    )
                    onLoaded?.invoke(forecast)
                },
                onFailure = { error ->
                    val lastForecast = (currentState as? WeatherUiState.Success)?.forecast
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
        _selectedCities.value = WeatherLocalCache.loadSelectedCities(context)
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
