# Архітектура системи / ARCHITECTURE
# Weather — Smart Forecast (v2.1 — Phase 2 Foundation)

> Версія: 2.1 | Дата: 2026-11-22  
> Автори: Agent 0 (Lead Architect), Agent 3 (Weather & Data Engineer)  
> Статус: Затверджено для Phase 2 (ADR-011, ADR-012)

---

## 1. Архітектурний патерн та структура шарів

Проєкт побудований за принципами **Clean Architecture** та **Unidirectional Data Flow (UDF)**. Код суворо розділений на три незалежні шари:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Presentation Layer                            │
│  - Тривкладкова навігація: NowScreen, ForecastScreen, LocationsScreen   │
│  - Окремий екран: SettingsScreen (доступ через TopBar)                 │
│  - Jetpack Compose Components: Hero, Hourly, Daily, Metrics, Sun, TopBar│
│  - Jetpack Glance AppWidget (2x2, 4x2) без реклами                     │
│  - AdBannerPlaceholder (Anchored Adaptive Banner, 50-90dp, Zero CLS)   │
│  - WeatherViewModel + WeatherUiState (StateFlow)                       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ спостерігає за StateFlow
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                              Domain Layer                              │
│  - Pure Kotlin Models (CurrentWeather, DailyForecastItem, Hourly...)   │
│  - Repository Interfaces (WeatherRepository, LocationRepository)       │
│  - WeatherCondition, WMO Logic, WeatherDataSource                      │
│  - Чисті контракти (DATA_CONTRACTS.md), нуль залежностей від Android   │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ реалізує контракти
                                    │
┌────────────────────────────────────────────────────────────────────────┐
│                               Data Layer                               │
│  - WeatherRemoteDataSource: OpenMeteoDataSource, WeatherMetroDataSource│
│  - In-flight Request Deduplication & Quota Safety (Cooldown при 429)   │
│  - WeatherLocalCache: повноцінне збереження 24h + 14-16d, multi-city   │
│  - Corrupted Cache Recovery (безпечне очищення при пошкодженні)        │
│  - Location: LocationClient (Foreground LocationManager), ReverseGeo   │
│  - Mappers: WeatherMapper, WeatherCodeMapper, GeocodingMapper          │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Ключові архітектурні рішення (ADR Summary)

1. **Jetpack Compose + Material 3 (ADR-002, ADR-006):**
   - Повністю декларативний інтерфейс із тривкладковою моделлю Material 3 NavigationBar.
   - Окремий екран налаштувань (`SettingsScreen`) через TopBar.
   - Динамічна зміна кольорової палітри (`LocalWeatherPalette`) залежно від погоди за вікном.
   - Доступність: контрастність WCAG AA ≥ 4.5:1, тач-таргети ≥ 48dp, семантика TalkBack, підтримка `reduced-motion`.

2. **Ізоляція погодного джерела через `WeatherRemoteDataSource` (ADR-003, ADR-012):**
   - Інтерфейс `WeatherRepository` та шар `WeatherRemoteDataSource` повністю ізолюють DTO конкретних провайдерів.
   - Основний комерційний кандидат: **WeatherMetro Free** (до 16 днів, комерційний ToS, $0 витрат).
   - Некомерційне dev-середовище: **Open-Meteo Free** (CC BY 4.0; суворо заборонено у релізі з AdMob).
   - Для автономного тестування створено `MockWeatherRepository`.

3. **Локальне збереження та захист від збоїв (ADR-012, CACHE_STRATEGY.md):**
   - `WeatherLocalCache` зберігає повні 24 години погодинного та 14-16 днів денного прогнозу.
   - Підтримка кешу для кількох міст за ключем координат.
   - Автоматичне відновлення при пошкодженні (Corrupted Cache Recovery) без крашів додатку.

4. **Захист квот та дедуплікація (ADR-012):**
   - In-flight deduplication: паралельні запити для одних координат зливаються в один.
   - Cooldown при помилках HTTP 429 та 5xx (запобігання спаму сервера, віддача застарілого кешу з `isStale = true`).

5. **Jetpack Glance для домашнього віджета (ADR-002, ADR-005, WIDGET_AD_CONTRACTS.md):**
   - Нативні RemoteViews, компактний (2x2) та розширений (4x2) режими.
   - Читання даних виключно з локального кешу, без прямого звернення до мережі чи GPS.
   - Суворо 0% реклами у віджеті.

6. **Рекламний контейнер AdMob (ADR-005, ADR-009, ADR-011):**
   - Anchored Adaptive Banner із динамічним розрахунком висоти (50–90dp) та резервуванням слота для **Zero CLS**.
   - Виключно тестові Ad Unit ID під час розробки.

7. **Безпека геолокації та приватність (ADR-004):**
   - Лише foreground-дозвіл за явною ініціативою користувача.
   - Сувора відсутність `ACCESS_BACKGROUND_LOCATION`.
   - Пошук міст працює абсолютно автономно через інтернет без увімкненого GPS.
