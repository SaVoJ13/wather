# Специфікація контрактів даних / DATA_CONTRACTS
# Weather — Smart Forecast | ROADMAP 2.0 (Phase 2)

> **Версія:** 2.0 | **Дата:** 2026-11-22  
> **Автори:** Agent 0 (Architect), Agent 3 (Weather & Data Engineer)  
> **Статус:** ЗАТВЕРДЖЕНО  
> **Нормативна основа:** `docs/governance/CONSTITUTION.uk.md`, `MASTER PROMPT — WEATHER PHASE 2` (Завдання A3.2).

---

## 1. Загальні принципи Domain Contracts

1. **Clean Architecture:** Усі сутності Domain Layer є чистими класами Kotlin (POKO — Plain Old Kotlin Objects), не мають жодних імпортів із пакетів `android.*` або сторонніх бібліотек серіалізації (JSON/XML).
2. **Незмінність (Immutability):** Усі поля є `val`. Зміна стану здійснюється виключно через виклик `.copy()`.
3. **Одиниці вимірювання за замовчуванням (Standard Units):**
   - Температура: градуси Цельсія (°C, `Double`).
   - Швидкість вітру: кілометри за годину (км/год, `Double`).
   - Атмосферний тиск: гектопаскалі (гПа / hPa, `Double`). 1 гПа ≈ 0.750062 мм рт. ст.
   - Відносна вологість: відсотки (%, `Int` від 0 до 100).
   - Ймовірність опадів: відсотки (%, `Int` від 0 до 100).
   - Кількість опадів: міліметри (мм, `Double`).
   - УФ-індекс: безрозмірна величина (`Double` від 0.0 до 15.0+).
4. **Часові пояси та дати:** Усі дати й час фіксуються в об'єктах `java.time.LocalDateTime` та прив'язуються до часового поясу запитуваної локації (наприклад, `Europe/Warsaw`, `Europe/Kyiv`), а не автоматично до таймзони пристрою користувача (Непорушне правило №9).

---

## 2. Доменні моделі (Domain Models)

### 2.1. `LocationModel` (Населений пункт)
```kotlin
data class LocationModel(
    val id: Long,                          // Унікальний числовий ідентифікатор місця (наприклад, Open-Meteo ID або хеш)
    val name: String,                      // Назва (наприклад, "Пшецлав (Przecław)" або "Київ")
    val country: String,                   // Країна (наприклад, "Польща", "Україна")
    val admin1: String? = null,            // Область/воєводство (наприклад, "Західнопоморське воєводство")
    val latitude: Double,                  // Широта (-90.0 .. +90.0)
    val longitude: Double,                 // Довгота (-180.0 .. +180.0)
    val timezone: String = "auto",         // IANA часовий пояс (наприклад, "Europe/Warsaw")
    val isCurrentLocation: Boolean = false // Прапорець: чи визначено координати за GPS користувача
)
```

### 2.2. `CurrentWeather` (Поточна погода)
```kotlin
data class CurrentWeather(
    val temperatureCelsius: Double,             // Фактична температура
    val apparentTemperatureCelsius: Double,     // Температура "відчувається як"
    val condition: WeatherCondition,            // Типізований стан погоди (CLEAR_SKY, RAIN...)
    val relativeHumidityPercent: Int,           // Вологість 0-100%
    val windSpeedKmh: Double,                   // Швидкість вітру
    val surfacePressureHpa: Double,             // Тиск на рівні поверхні
    val uvIndex: Double = 0.0,                  // УФ-індекс
    val time: LocalDateTime,                    // Локальний час вимірювання
    val weatherCode: Int = 0                    // Первинний WMO-код
)
```

### 2.3. `HourlyForecastItem` (Погодинний крок прогнозу)
```kotlin
data class HourlyForecastItem(
    val time: LocalDateTime,                    // Дата і час години (00:00 .. 23:00)
    val temperatureCelsius: Double,             // Прогнозна температура на цю годину
    val condition: WeatherCondition,            // Стан погоди
    val precipitationProbabilityPercent: Int = 0,// Ймовірність опадів 0-100%
    val precipitationMm: Double = 0.0,          // Очікуваний об'єм опадів за годину
    val windSpeedKmh: Double = 0.0,             // Швидкість вітру
    val weatherCode: Int = 0                    // Первинний WMO-код
)
```

### 2.4. `DailyForecastItem` (Добовий прогноз)
```kotlin
data class DailyForecastItem(
    val date: LocalDateTime,                    // Дата прогнозу (початок доби 00:00)
    val minTemperatureCelsius: Double,          // Мінімальна добова температура
    val maxTemperatureCelsius: Double,          // Максимальна добова температура
    val condition: WeatherCondition,            // Домінуючий стан доби
    val precipitationProbabilityPercent: Int = 0,// Максимальна ймовірність опадів
    val precipitationSumMm: Double = 0.0,       // Сумарна кількість опадів за добу
    val maxWindSpeedKmh: Double = 0.0,          // Максимальний порив вітру
    val sunrise: LocalDateTime? = null,         // Час сходу сонця
    val sunset: LocalDateTime? = null,          // Час заходу сонця
    val uvIndexMax: Double = 0.0,               // Максимальний добовий УФ-індекс
    val weatherCode: Int = 0                    // Домінуючий WMO-код
)
```

### 2.5. `WeatherDataSource` (Джерела погодних даних)
```kotlin
enum class WeatherDataSource {
    WEATHER_METRO,      // HatchSquare Technologies LLC (Основний комерційний кандидат v1)
    OPEN_METEO,         // Open-Meteo GmbH (Некомерційне dev/test середовище, CC BY 4.0)
    VISUAL_CROSSING,    // Visual Crossing Corp (Резервний комерційний кандидат)
    MOCK                // Локальні автономні фікстури для UI-тестів та офлайну
}
```

### 2.6. `FullWeatherForecast` (Головний агрегат)
```kotlin
data class FullWeatherForecast(
    val location: LocationModel,                // Локація
    val current: CurrentWeather,                // Поточні умови
    val hourly24h: List<HourlyForecastItem>,    // Погодинна стрічка (мінімум 24 години від поточної)
    val daily14Days: List<DailyForecastItem>,   // Щоденний прогноз (від 14 до 16 днів)
    val updatedAt: LocalDateTime,               // Час останнього успішного оновлення
    val isStale: Boolean = false,               // Прапорець застарілості даних (офлайн/помилка мережі)
    val dataSource: WeatherDataSource = WeatherDataSource.WEATHER_METRO
)
```

---

## 3. Контракти репозиторіїв та джерел даних (Data Layer Interfaces)

### 3.1. Інтерфейс віддаленого джерела даних `WeatherRemoteDataSource`
```kotlin
interface WeatherRemoteDataSource {
    val sourceName: WeatherDataSource
    
    suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String
    ): Result<FullWeatherForecast>
}
```

### 3.2. Інтерфейс репозиторію `WeatherRepository`
```kotlin
interface WeatherRepository {
    /**
     * Отримує повний прогноз для заданої локації.
     * Працює за стратегією Stale-While-Revalidate:
     * 1. Якщо forceRefresh = false і кеш свіжий — повертає кеш.
     * 2. Якщо forceRefresh = true або кеш прострочений — запитує віддалене джерело.
     * 3. У разі мережевої помилки повертає застарілий кеш з isStale = true.
     */
    suspend fun getFullForecast(
        location: LocationModel,
        forceRefresh: Boolean = false
    ): Result<FullWeatherForecast>

    /**
     * Реактивний потік оновлень прогнозу для активної локації.
     */
    fun observeCurrentForecast(): Flow<FullWeatherForecast?>
}
```

### 3.3. Обробка станів помилок та квот (`WeatherErrorState`)
```kotlin
sealed class WeatherErrorState {
    object NoInternet : WeatherErrorState()
    object QuotaExceeded429 : WeatherErrorState()
    data class ServerError5xx(val code: Int) : WeatherErrorState()
    object LocationPermissionDenied : WeatherErrorState()
    data class Unknown(val message: String) : WeatherErrorState()
}
```
