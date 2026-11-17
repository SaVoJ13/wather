# Архітектурні контракти та розподіл файлів / CONTRACTS

> Відповідно до правил співпраці агентів (`AGENTS.md`):
> «Перед паралельною реалізацією узгодь контракти між UI і data/domain та розподіли файли так, щоб агенти не редагували ті самі ділянки одночасно».
> До однієї папки/файлу суворо заборонено призначати паралельно двох авторів.

---

## 1. Загальна модульна структура застосунку

```
app/src/main/java/com/weather/app/
├── core/                  # Спільні утиліти, моделі результатів, розширення часу
│   ├── model/             # Загальні типізовані енуми (Units, WeatherCode)
│   └── network/           # Базовий HTTP клієнт
├── domain/                # Бізнес-логіка (чистий Kotlin, без залежностей від UI або конкретних API)
│   ├── model/             # WeatherInfo, CurrentWeather, HourlyWeather, DailyForecast, LocationModel
│   └── repository/        # Інтерфейси: WeatherRepository, LocationRepository
├── data/                  # Реалізація даних (відповідальний: weather-location)
│   ├── remote/            # Open-Meteo DTO, API клієнт, мапери
│   ├── local/             # DataStore / кеш, збережені міста
│   ├── location/          # Foreground LocationProvider (Android LocationManager)
│   └── mock/              # MockWeatherRepository з фікстурами для UI та тестів
├── ui/                    # Інтерфейс користувача (відповідальний: android-ui)
│   ├── theme/             # Кольори, типографіка, теми (Material 3), анімаційні константи
│   ├── components/        # Багаторазові компоненти (погодинний ряд, картка дня, погодна іконка)
│   ├── screens/           # Головний екран, пошук міста, деталі дня
│   └── viewmodel/         # WeatherViewModel, SearchViewModel
└── widget/                # Android Glance віджет (відповідальний: android-ui)
    ├── WeatherGlanceWidget.kt
    └── WeatherWidgetReceiver.kt
```

---

## 2. Доменні моделі (Domain Models)

Ці моделі є незмінними (immutable) та слугують єдиним джерелом правди для UI та Widget.

```kotlin
package com.weather.app.domain.model

import java.time.LocalDateTime

enum class WeatherCondition(val descriptionUk: String) {
    CLEAR_SKY("Ясно"),
    MAINLY_CLEAR("Здебільшого ясно"),
    PARTLY_CLOUDY("Мінлива хмарність"),
    OVERCAST("Хмарно"),
    FOG("Туман"),
    DRIZZLE("Мряка"),
    RAIN("Дощ"),
    HEAVY_RAIN("Сильний дощ"),
    SNOW("Сніг"),
    THUNDERSTORM("Гроза"),
    UNKNOWN("Невідомо")
}

data class LocationModel(
    val id: Long = 0,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val isCurrentLocation: Boolean = false
)

data class CurrentWeather(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val relativeHumidityPercent: Int,
    val windSpeedKmh: Double,
    val surfacePressureHpa: Double,
    val uvIndex: Double,
    val time: LocalDateTime
)

data class HourlyForecastItem(
    val time: LocalDateTime,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int
)

data class DailyForecastItem(
    val date: LocalDateTime,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbabilityPercent: Int,
    val precipitationSumMm: Double,
    val maxWindSpeedKmh: Double,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null
)

data class FullWeatherForecast(
    val location: LocationModel,
    val current: CurrentWeather,
    val hourly24h: List<HourlyForecastItem>,
    val daily14Days: List<DailyForecastItem>,
    val updatedAt: LocalDateTime,
    val isStale: Boolean = false
)
```

---

## 3. Інтерфейси репозиторіїв (Domain Contracts)

```kotlin
package com.weather.app.domain.repository

import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    /**
     * Отримує повний прогноз (поточна, 24h погодинна, 14 днів) для заданих координат.
     * Працює за стратегією: кеш спочатку (швидкий показ) -> оновлення з мережі.
     */
    suspend fun getFullForecast(location: LocationModel, forceRefresh: Boolean = false): Result<FullWeatherForecast>
    
    /**
     * Потік останнього закешованого прогнозу.
     */
    fun observeCurrentForecast(): Flow<FullWeatherForecast?>
}

interface LocationRepository {
    /**
     * Пошук міст через онлайн-геокодування. Працює автономно без GPS.
     */
    suspend fun searchCities(query: String): Result<List<LocationModel>>
    
    /**
     * Отримання поточної геолокації користувача (тільки foreground).
     * Якщо дозвіл не надано, повертає помилку Result.failure(PermissionDeniedException).
     */
    suspend fun getCurrentLocation(): Result<LocationModel>
    
    /**
     * Отримання збережених обраних міст користувача.
     */
    fun getFavoriteLocations(): Flow<List<LocationModel>>
    suspend fun saveFavoriteLocation(location: LocationModel)
    suspend fun removeFavoriteLocation(location: LocationModel)
}
```

---

## 4. Стан інтерфейсу (UI State Contract)

```kotlin
package com.weather.app.ui.state

import com.weather.app.domain.model.FullWeatherForecast

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    
    data class Success(
        val forecast: FullWeatherForecast,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val message: String? = null
    ) : WeatherUiState
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val lastCachedForecast: FullWeatherForecast? = null
    ) : WeatherUiState
}
```

---

## 5. Розподіл зон відповідальності (Файлові зони)

| Агент | Дозволені директорії для запису | Заборонено редагувати |
|---|---|---|
| **product-ux** | `docs/ux/**`, `docs/design/**` | `app/**`, `docs/governance/**` |
| **weather-location** | `app/src/main/java/.../data/**`, `.../domain/**` | `app/src/main/java/.../ui/**`, `.../widget/**` |
| **android-ui** | `app/src/main/java/.../ui/**`, `.../widget/**`, `app/src/main/res/**` | `app/src/main/java/.../data/remote/**` |
| **qa-privacy** | `docs/tasks/**` (розділ QA), `docs/qa/**`, тести верифікації | Виправляти продуктовий код самостійно без повернення автору |
| **Agent 0** | `docs/governance/**`, `docs/tasks/**`, кореневі gradle-файли | Не ставить `QA Verified` на власний код |
