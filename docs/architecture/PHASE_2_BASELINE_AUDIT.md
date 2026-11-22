# Звіт базового аудиту архітектури та кодової бази / PHASE_2_BASELINE_AUDIT
# Weather — Smart Forecast | ROADMAP 2.0 (Phase 2)

> **Проєкт:** WEATHER  
> **Дата аудиту:** 2026-11-22  
> **Автори аудиту:** Agent 0 (Lead Architect), Agent 5 (QA & Security Engineer)  
> **Статус:** ЗАВЕРШЕНО  
> **Нормативна база:** `docs/governance/CONSTITUTION.uk.md` (п. 1, 2.1, 5.1), `MASTER PROMPT — WEATHER PHASE 2` (Завдання A0.1 та A5.1).

---

## 1. Точний стан репозиторію Git

- **URL репозиторію:** `https://github.com/SaVoJ13/wather.git`
- **Активна гілка:** `main`
- **Останній зафіксований коміт на початку Phase 2:** `1f345f8` (*feat(design): deliver real PNG mockups, licensing audit, and independent QA verification for Phase 1 Final Design Gate*)
- **Стан робочого дерева:** Чисте (clean working tree), невідстежуваних секретів чи тимчасових бінарних артефактів немає.
- **Підключений тестовий пристрій:** Samsung Galaxy S24 Ultra (`RZCX326349A`).

---

## 2. Версії інструментів, середовища та SDK

| Компонент / Інструмент | Фактична версія у проєкті | Вимоги та стандарти Google Play | Статус сумісності |
|---|:---:|---|:---:|
| **Kotlin** | `2.0.21` | Підтримка K2 Compiler, Compose Compiler Gradle Plugin | **СУЧАСНИЙ (PASS)** |
| **Android Gradle Plugin** | `8.7.2` | Офіційний AGP для Android Studio Ladybug/Meerkat | **СУЧАСНИЙ (PASS)** |
| **Gradle** | `8.11.1` (демон: `9.6.0`) | Сумісність із JDK 17/21 | **СУЧАСНИЙ (PASS)** |
| **Java / JVM Target** | Java `17` (`JavaVersion.VERSION_17`, `JVM_17`) | Стандарт для AGP 8.x | **СУЧАСНИЙ (PASS)** |
| **Android compileSdk** | `35` (Android 15) | Вимога Google Play 2026 | **ВІДПОВІДАЄ (PASS)** |
| **Android targetSdk** | `35` (Android 15) | Вимога Google Play 2026 | **ВІДПОВІДАЄ (PASS)** |
| **Android minSdk** | `26` (Android 8.0 Oreo) | Покриття >95% пристроїв, нативні `java.time.*` без дешугарингу | **ВІДПОВІДАЄ (PASS)** |
| **Jetpack Compose BOM** | `2024.11.00` | Стабільні Material 3, Foundation, UI | **СУЧАСНИЙ (PASS)** |
| **Jetpack Glance** | `1.1.1` (`glance-appwidget`, `glance-material3`) | Офіційний декларативний інструмент віджетів | **СУЧАСНИЙ (PASS)** |
| **Coroutines** | `1.9.0` (`kotlinx-coroutines-android`, `kotlinx-coroutines-test`) | Flow, StateFlow | **СУЧАСНИЙ (PASS)** |

---

## 3. Структура пакетів та модулів

Проєкт побудований як мономодульний Android-застосунок (`:app`) із чітким пакетом `com.weather.app`, декомпозованим за шарами Clean Architecture:

```
app/src/main/java/com/weather/app/
├── data/
│   ├── local/
│   │   └── WeatherLocalCache.kt          # SharedPreferences + org.json кеш погоди та локації
│   ├── location/
│   │   ├── LocationClient.kt             # Foreground LocationManager (GPS + Network)
│   │   └── ReverseGeocoder.kt            # Android Geocoder + OSM Nominatim fallback
│   ├── mapper/
│   │   ├── GeocodingMapper.kt            # Перетворення DTO геокодування в LocationModel
│   │   ├── WeatherCodeMapper.kt          # WMO-коди 0-99 в описи та умови погоди
│   │   └── WeatherMapper.kt              # Open-Meteo DTO -> FullWeatherForecast
│   ├── mock/
│   │   └── MockWeatherRepository.kt      # Автономні фікстури Києва та Варшави
│   ├── remote/
│   │   ├── GeocodingApiClient.kt         # Пошук міст через Open-Meteo Geocoding
│   │   ├── OpenMeteoHttpClient.kt        # Мережевий клієнт Open-Meteo
│   │   └── dto/
│   │       ├── GeocodingDto.kt           # DTO відповіді геокодування
│   │       └── OpenMeteoDto.kt           # DTO відповіді прогнозу погоди
│   └── repository/
│       ├── LocationRepositoryImpl.kt     # Реалізація пошуку та геокодування
│       └── WeatherRepositoryImpl.kt      # Реалізація репозиторію погоди
├── domain/
│   ├── model/
│   │   ├── CurrentWeather.kt             # Поточний стан (температура, вітер, тиск, вологість)
│   │   ├── DailyForecastItem.kt          # Денний прогноз (min/max, WMO, опади, схід/захід)
│   │   ├── FullWeatherForecast.kt        # Повний агрегат (Current + 24h + 14d + Location + Stale)
│   │   ├── HourlyForecastItem.kt         # Погодинний прогноз
│   │   ├── LocationModel.kt              # Модель населеного пункту (lat, lon, tz, назва)
│   │   ├── WeatherCondition.kt           # Перелік умов (CLEAR, CLOUDY, RAIN, SNOW, STORM...)
│   │   └── WeatherDataSource.kt          # Перелік джерел (WEATHER_METRO, OPEN_METEO, MOCK...)
│   └── repository/
│       ├── LocationRepository.kt         # Інтерфейс пошуку та координат
│       └── WeatherRepository.kt          # Інтерфейс отримання та спостереження прогнозу
├── ui/
│   ├── MainActivity.kt                   # Головна точка входу Android Activity
│   ├── components/
│   │   ├── AdBannerPlaceholder.kt        # Контейнер банера (50dp, Zero CLS)
│   │   ├── CurrentWeatherHero.kt         # Головна картка поточної погоди
│   │   ├── DailyForecastList.kt          # Список 14 днів із Range Bars
│   │   ├── DayDetailsBottomSheet.kt      # Модальне вікно деталей вибраного дня
│   │   ├── HourlyForecastRow.kt          # 24-годинна горизонтальна стрічка від поточної години
│   │   ├── LanguageSelectionDialog.kt    # In-app вибір із 7 мов
│   │   ├── SearchCityBottomSheet.kt      # Пошук міст із динамічними обраними
│   │   ├── SunriseSunsetCard.kt          # Canvas-дуга небосхилу із сонцем
│   │   ├── WeatherIcon.kt                # Векторні погодні піктограми
│   │   ├── WeatherMetricsGrid.kt         # 2x2 сітка показників (вологість, вітер, тиск, УФ)
│   │   ├── WeatherRadarCard.kt           # Canvas-радар (360° sweep, MVP-заглушка)
│   │   └── WeatherTopBar.kt              # Шапка з пошуком, локацією, мовою та налаштуваннями
│   ├── screens/
│   │   └── WeatherMainScreen.kt          # Монолітний екран поточної версії
│   ├── state/
│   │   └── WeatherUiState.kt             # Loading, Success, Error
│   ├── theme/
│   │   ├── Color.kt                      # Палітра кольорів
│   │   ├── Type.kt                       # Шрифтові стилі
│   │   └── WeatherTheme.kt               # Тема Material 3 з динамічними градієнтами неба
│   └── viewmodel/
│       └── WeatherViewModel.kt           # Управління UI-станом та взаємодією з репозиторіями
└── widget/
    ├── WeatherGlanceWidget.kt            # Jetpack Glance віджет (2x2 та 4x2)
    ├── WeatherWidgetReceiver.kt          # BroadcastReceiver оновлень віджета
    └── WeatherWidgetScheduler.kt         # Планувальник AlarmManager (10 хв)
```

---

## 4. Фактичний стан модульних тестів (Baseline Verification A5.1)

На момент старту Phase 2 проведено перезапуск усіх тестів за допомогою `gradlew.bat testDebugUnitTest --rerun-tasks`:

| Сьют тестів (Клас) | Кількість тестів | Пройдено | Провалено | Помилок | Пропущено |
|---|:---:|:---:|:---:|:---:|:---:|
| `GeocodingMapperTest` | 4 | 4 | 0 | 0 | 0 |
| `ReverseGeocoderTest` | 3 | 3 | 0 | 0 | 0 |
| `LocationRepositoryTest` | 4 | 4 | 0 | 0 | 0 |
| `MockWeatherRepositoryTest` | 3 | 3 | 0 | 0 | 0 |
| `WeatherCodeMapperTest` | 9 | 9 | 0 | 0 | 0 |
| `WeatherMapperTest` | 5 | 5 | 0 | 0 | 0 |
| `DynamicAttributionTest` | 3 | 3 | 0 | 0 | 0 |
| `LocalizationTest` | 4 | 4 | 0 | 0 | 0 |
| `SunriseSunsetTest` | 8 | 8 | 0 | 0 | 0 |
| `WeatherThemeTest` | 4 | 4 | 0 | 0 | 0 |
| `WeatherViewModelTest` | 7 | 7 | 0 | 0 | 0 |
| `WeatherGlanceWidgetTest` | 6 | 6 | 0 | 0 | 0 |
| `WeatherWidgetSchedulerTest` | 2 | 2 | 0 | 0 | 0 |
| **РАЗОМ** | **62** | **62** | **0** | **0** | **0** |

**Фактичний показник:** **100% PASS (62/62)**. Усі тести працездатні, збірка повністю зелена.

---

## 5. Виявлений технічний борг та архітектурні ризики

В ході глибокого аудиту кодової бази виявлено наступні технічні обмеження, які вимагають вирішення в рамках Phase 2:

1. **Обмеження дискового кешування (`WeatherLocalCache.kt`):**
   - **Факт:** `WeatherLocalCache.saveForecast()` наразі записує в JSON лише перші 6 годин (`hourly24h.take(6)`) та лише 5 днів (`daily14Days.take(5)`).
   - **Ризик:** У разі запуску застосунку в режимі офлайн користувач бачить скорочений прогноз замість повноцінних 24 годин та 14 днів, що порушує продуктовий контракт.
   - **Факт:** Зберігається кеш прогнозу лише для однієї активної локації. При перемиканні між збереженими містами без інтернету дані попередніх міст втрачаються.
   - **Рішення для Phase 2:** Розширити збереження повного масиву прогнозу (24 години, 14 днів) та реалізувати кешування прогнозу за ключем `locationId` або координатами.

2. **Статичний спільний стан у репозиторії (`WeatherRepositoryImpl.kt`):**
   - **Факт:** Клас містить `companion object { @Volatile var lastKnownForecast: FullWeatherForecast? = null }`.
   - **Ризик:** Використання статичного мутабельного стану ускладнює незалежне тестування та може спричиняти стан гонитви (race condition) при роботі декількох інстансів.
   - **Рішення для Phase 2:** Перевести керування останнім прогнозом на рівень локального сховища та реактивного `StateFlow` у межах репозиторію.

3. **Жорстка прив'язка до провайдера Open-Meteo в репозиторії:**
   - **Факт:** `WeatherRepositoryImpl` напряму залежить від `OpenMeteoHttpClient` та `OpenMeteoForecastResponse` DTO через конструктор.
   - **Ризик:** Відсутній спільний контракт `WeatherRemoteDataSource` або `WeatherProviderAdapter`, що унеможливлює просте підключення `WeatherMetro Free` як основного комерційного джерела без переписування репозиторію.
   - **Рішення для Phase 2:** Виділити інтерфейс `WeatherRemoteDataSource` з методами отримання сирих або проміжних даних, забезпечивши підтримку `WeatherMetroDataSource` та `OpenMeteoDataSource`.

4. **Захист від переліміту API (Quota Protection):**
   - **Факт:** Наразі єдиним захистом є проста перевірка `cacheTtlMinutes = 30L`. Немає in-memory об'єднання паралельних викликів (request deduplication) та немає експоненційного бекоффу для кодів HTTP 429 та 5xx.
   - **Рішення для Phase 2:** Створити `QuotaTracker` та дебаунсер запитів.

5. **Монолітний екран `WeatherMainScreen.kt`:**
   - **Факт:** `WeatherMainScreen.kt` містить понад 300 рядків і поєднує в собі відображення поточної погоди, застарілий радар, вибір мови та діалоги.
   - **Рішення для Phase 2:** Забезпечити модульний навігаційний каркас із 3 вкладками (`NowScreen`, `ForecastScreen`, `LocationsScreen`) та окремим `SettingsScreen`.

6. **Поведінка Android Lint у збірці (`app/build.gradle.kts`):**
   - **Факт:** Налаштування `lint { checkReleaseBuilds = false; abortOnError = false }` маскують можливі приховані проблеми доступності та маніфесту.
   - **Рішення для Phase 2:** Увімкнути базові перевірки Lint без блокування експериментальних попереджень.

---

## 6. Аудит безпеки, ліцензій та бюджету (A5.4)

1. **API-ключі та конфіденційні дані:**
   - Перевірено всі файли коду, ресурсів та документації. В Git відсутні захардкоджені бойові API-ключі чи секрети.
   - Для WeatherMetro створено безпечний тестовий скрипт `scripts/test_weathermetro_secure.py`, який зчитує ключ виключно зі змінної оточення або файлу `local.properties`.
2. **Дозволи AndroidManifest:**
   - Підтверджено повну відсутність `ACCESS_BACKGROUND_LOCATION`.
   - Запити GPS виконуються виключно у foreground-контексті за явною ініціативою користувача.
3. **Ліцензії сторонніх компонентів:**
   - Усі сторонні бібліотеки в `app/build.gradle.kts` мають дозволені відкриті ліцензії Apache 2.0 (Google AndroidX, KotlinX, JUnit).
   - Жодних комерційних або вірусних ліцензій (GPL/AGPL) не підключено.
4. **Бюджет:**
   - Фактичні витрати становлять рівно **$0.00**. Вимога **ZERO BUDGET POLICY** дотримана на 100%.

---

## 7. Підсумковий висновок Baseline Audit

Кодова база проєкту перебуває у стабільному, компільованому та повністю протестованому стані (62/62 тестів успішні). Проєкт готовий до проведення рефакторингу архітектурного шару Data/Domain, посилення локального збереження, ізоляції погодних джерел та створення чистих презентаційних контрактів у рамках Phase 2.
