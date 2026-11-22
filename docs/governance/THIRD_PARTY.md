# Реєстр сторонніх компонентів та ліцензій / THIRD_PARTY

> Відповідно до п. 3.1 та розділу 6 Конституції v2.0 і `PROJECT_PROFILE.md`:
> Будь-яка бібліотека, SDK або зовнішній API повинні бути зафіксовані в цьому реєстрі з перевіркою офіційного першоджерела та сумісності ліцензії.
> Невідомі або несумісні ліцензії заборонені.

---

## 1. Зовнішні API та джерела даних

| Компонент / Сервіс | Призначення | Ліцензія / Умови | Квоти та обмеження | Статус схвалення | Офіційне джерело |
|---|---|---|---|---|---|
| **Open-Meteo Weather Forecast API** | Погодинний прогноз (до 7-14 днів) та денний прогноз (до 16 днів) | [CC BY 4.0](https://open-meteo.com/en/terms) (лише для некомерційного використання) | До 10,000 запитів на добу безкоштовно. Заборонено для публічного релізу з рекламою без комерційної підписки. | **Схвалено для розробки та локального тестування (dev/test)**. | https://open-meteo.com/ |
| **WeatherMetro Free** | Погодинний і щоденний прогноз до 16 днів для комерційного релізу з рекламою AdMob під ZERO BUDGET POLICY | [WeatherMetro Terms](https://weathermetro.com/) | 100,000 HTTP-запитів на місяць за $0. Дозволено показ реклами AdMob за наявності атрибуції («Weather data by WeatherMetro»). Без банківської карти, жорсткий HTTP 429. | **Кандидат №1 (Рекомендовано командою та QA для затвердження Власником)**. | https://weathermetro.com/ |
| **WeatherAPI.com API** | Погодинний і денний прогноз, астрономія, пошук міст для комерційного релізу з рекламою | [WeatherAPI Terms](https://www.weatherapi.com/terms.aspx) | До 1,000,000 запитів на місяць безкоштовно з рекламою AdMob (атрибуція обов'язкова). Обмеження: лише 3 дні прогнозу на Free-плані (14 днів вимагають платний Pro-план). | **Кандидат №3 (Резервний варіант у разі скорочення прогнозу до 3 днів)**. | https://www.weatherapi.com/ |
| **Visual Crossing Weather API** | Погодинний і щоденний прогноз | [Visual Crossing Terms](https://www.visualcrossing.com/) | 1,000 записів (records) на добу. 1 виклик = 360 records (15 днів x 24 год). Вичерпується за 2.7 запитів на добу на весь додаток. | ❌ **Відхилено (Непридатний для мобільного клієнта)**. | https://www.visualcrossing.com/ |
| **Open-Meteo Geocoding API** | Пошук міст та координат за назвою | [CC BY 4.0](https://open-meteo.com/en/terms) (некомерційне використання) | Дозволяє швидкий пошук без потреби реєстрації та ключів. Працює автономно без GPS. | **Кандидат для прототипу (dev/test)**. | https://open-meteo.com/en/docs/geocoding-api |
| **OpenStreetMap Nominatim API** | Зворотне геокодування GPS-координат у назву населеного пункту (село/місто) | [ODbL 1.0](https://www.openstreetmap.org/copyright) | До 1 запиту на секунду, обов'язковий User-Agent. Використовується виключно за явної foreground дії користувача. | **Схвалено для dev/test**. | https://nominatim.org/ |
| **Android Location Services (LocationManager)** | Отримання поточної геопозиції за явним запитом користувача | Android Open Source Project (AOSP / Apache 2.0) | Тільки foreground location (`ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`). Жодної фонової локації. | **Схвалено**. | https://developer.android.com/develop/sensors-and-location/location |

---

## 2. Залежності платформи та бібліотеки коду (Android / Kotlin)

| Бібліотека / SDK | Опис та модуль | Ліцензія | Статус перевірки | Першоджерело |
|---|---|---|---|---|
| **Kotlin Standard Library & Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Apache 2.0 | Схвалено | https://github.com/Kotlin/kotlinx.coroutines |
| **KotlinX Serialization JSON** | Серіалізація та парсинг погодних DTO | Apache 2.0 | Схвалено | https://github.com/Kotlin/kotlinx.serialization |
| **AndroidX Core KTX** | `androidx.core:core-ktx` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/core |
| **AndroidX AppCompat** | `androidx.appcompat:appcompat:1.7.0` (підтримка мовного перемикання на Android < 13 та теми DayNight) | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/appcompat |
| **Jetpack Compose BOM & UI** | `androidx.compose.ui`, `material3`, `foundation` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/compose |
| **Jetpack Lifecycle & ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/lifecycle |
| **Jetpack Glance (AppWidget)** | `androidx.glance:glance-appwidget`, `androidx.glance:glance-material3` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/glance |
| **Ktor Client (або OkHttp)** | Легкий клієнт для HTTP-запитів до Weather API | Apache 2.0 | Схвалено | https://ktor.io/ |
| **AndroidX DataStore** | Локальне збереження обраних міст та налаштувань | Apache 2.0 | Схвалено | https://developer.android.com/topic/libraries/architecture/datastore |
| **Google Mobile Ads Lite SDK (AdMob)** | Показ банерної реклами (компонент комерційної моделі) | Google Play Services SDK License | Схвалено для розробки з тестовими Ad Unit ID. Бойові ключі — лише Власник перед релізом. | https://developers.google.com/admob/android/quick-start |


### 2.1 Тестові залежності (testImplementation, 0 байт у релізному APK)

| Бібліотека / SDK | Опис та модуль | Ліцензія | Статус перевірки | Першоджерело |
|---|---|---|---|---|
| **org.json** | `org.json:json:20240303` (автономна чиста JVM-реалізація org.json для локальних unit-тестів без Android framework runtime) | **Public Domain** (офіційно в Maven Central metadata `pom.xml` та LICENSE файлі; історичний перехід від JSON License до вільного суспільного надбання) | Схвалено для testImplementation (строго 0 байт у релізі, $0) | https://github.com/stleary/JSON-java |
---

## 3. Заборонені компоненти

1. Будь-які SDK аналітики (Google Firebase Analytics, Amplitude, Mixpanel тощо) — не затверджені Власником.
2. Будь-які неавторизовані рекламні SDK (Unity Ads, AppLovin тощо, крім офіційно схваленого Власником Google AdMob згідно з ADR-009).
3. Сторонні crash-репортери, що надсилають системні логи на зовнішні сервери без явного рішення Власника.
4. Будь-які платні API або API з обов'язковою прив'язкою банківської картки (порушення ZERO BUDGET POLICY).

