# Архітектура системи / ARCHITECTURE
# Weather — Smart Forecast (v2.0)

> Версія: 2.0 | Дата: 2026-11-22  
> Автори: Agent 0 (Lead Architect), Agent 3 (Data Engineer)  
> Статус: Затверджено для Phase 0

---

## 1. Архітектурний патерн та структура шарів

Проєкт побудований за принципами **Clean Architecture** та **Unidirectional Data Flow (UDF)**. Код суворо розділений на три незалежні шари:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  - Jetpack Compose Screens (WeatherMainScreen)              │
│  - UI Components (Hero, Hourly, Daily, Metrics, Radar, Sun) │
│  - Jetpack Glance AppWidget (WeatherGlanceWidget)           │
│  - WeatherViewModel + WeatherUiState (StateFlow)            │
└──────────────────────────────┬──────────────────────────────┘
                               │ спостерігає за StateFlow
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                          │
│  - Pure Kotlin Models (CurrentWeather, DailyForecastItem)   │
│  - Repository Interfaces (WeatherRepository, LocationRepo)  │
│  - WeatherCondition & WMO Logic                             │
└──────────────────────────────▲──────────────────────────────┘
                               │ реалізує контракти
                               │
┌─────────────────────────────────────────────────────────────┐
│                        Data Layer                           │
│  - Remote: OpenMeteoHttpClient, GeocodingApiClient, DTOs   │
│  - Local: WeatherLocalCache (SharedPreferences, Disk Cache) │
│  - Location: LocationClient (Foreground LocationManager)    │
│  - ReverseGeocoder (OpenStreetMap Nominatim OSM)            │
│  - Mappers: WeatherMapper, WeatherCodeMapper, GeocodingMap  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Ключові архітектурні рішення (ADR Summary)

1. **Jetpack Compose + Material 3 (ADR-002, ADR-006):**
   - Повністю декларативний інтерфейс.
   - Динамічна зміна кольорової палітри (`LocalWeatherPalette`) залежно від погоди за вікном (ясно, хмарно, дощ, сніг, буря).
   - Підтримка системної доступності: контрастність WCAG AA ≥ 4.5:1, тач-таргети ≥ 48dp, семантика TalkBack, підтримка `reduced-motion`.

2. **Ізоляція погодного джерела через Repository Pattern (ADR-003):**
   - Інтерфейс `WeatherRepository` повертає чисті доменні моделі `FullWeatherForecast`.
   - Завдяки цьому провайдер погодних даних (Open-Meteo, WeatherAPI тощо) може бути змінений або підключений комерційний ключ без жодного рядка змін у Compose UI або ViewModel.
   - Для автономного тестування створено `MockWeatherRepository`.

3. **Jetpack Glance для домашнього віджета (ADR-002, ADR-005):**
   - Віджет компілюється в нативні RemoteViews Android, гарантуючи нульовий ризик витоків пам'яті.
   - Використовує Responsive Size Mode: компактний (2x2) та розширений (4x2).
   - Оновлюється через `WeatherWidgetScheduler` (AlarmManager `setAndAllowWhileIdle` раз на 10-15 хвилин), що гарантує енергоощадність без виснаження батареї.
   - Суворе дотримання політики: **жодної реклами у віджеті**.

4. **Безпека геолокації та приватність (ADR-004):**
   - Дозвіл на локацію запитується виключно за явної foreground-дії користувача (кнопка «Моє місцезнаходження»).
   - Відсутність дозволу `ACCESS_BACKGROUND_LOCATION`.
   - Пошук міст працює абсолютно автономно через інтернет без увімкненого GPS.
   - Координати ніколи не зберігаються у зовнішніх сервісах чи логах.

5. **Багаторівневе кешування (Offline-First):**
   - При старті додаток миттєво зчитує останній відомий прогноз із диска через `WeatherLocalCache.loadCachedForecast()`.
   - Якщо мережа відсутня, інтерфейс показує закешовані дані з бейджем «Офлайн-режим» та часом останнього оновлення.
   - У фоні надсилається асинхронний запит на оновлення; при отриманні свіжих даних UI плавно оновлюється через `Crossfade`.

6. **Система локалізації та Per-App Language:**
   - Повна підтримка 7 мов (`uk`, `pl`, `en`, `de`, `ru`, `es`, `it`).
   - На пристроях Android 13+ інтегровано з системним меню через `locales_config.xml`.
   - На старіших версіях підтримується через `AppCompatDelegate.setApplicationLocales` та `AppLocalesMetadataHolderService`.
   - Якщо мова системи пристрою не входить до списку підтримуваних — автоматичний безпечний fallback на англійську мову (`res/values/strings.xml`).
