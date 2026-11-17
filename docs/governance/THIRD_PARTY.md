# Реєстр сторонніх компонентів та ліцензій / THIRD_PARTY

> Відповідно до п. 3.1 та розділу 6 Конституції v2.0 і `PROJECT_PROFILE.md`:
> Будь-яка бібліотека, SDK або зовнішній API повинні бути зафіксовані в цьому реєстрі з перевіркою офіційного першоджерела та сумісності ліцензії.
> Невідомі або несумісні ліцензії заборонені.

---

## 1. Зовнішні API та джерела даних

| Компонент / Сервіс | Призначення | Ліцензія / Умови | Квоти та обмеження | Статус схвалення | Офіційне джерело |
|---|---|---|---|---|---|
| **Open-Meteo Weather Forecast API** | Погодинний прогноз (до 7-14 днів) та денний прогноз (до 16 днів) | [CC BY 4.0](https://open-meteo.com/en/terms) (лише для некомерційного використання) | До 10,000 запитів на добу безкоштовно. Заборонено для публічного релізу з рекламою без комерційної підписки. | **Схвалено лише для розробки та локального тестування (dev/test)**. Для релізу потрібна комерційна підписка або заміна провайдера. | https://open-meteo.com/ |
| **Open-Meteo Geocoding API** | Пошук міст та координат за назвою | [CC BY 4.0](https://open-meteo.com/en/terms) (некомерційне використання) | Дозволяє швидкий пошук без потреби реєстрації та ключів. Працює автономно без GPS. | **Кандидат для прототипу**. | https://open-meteo.com/en/docs/geocoding-api |
| **Android Location Services (LocationManager)** | Отримання поточної геопозиції за явним запитом користувача | Android Open Source Project (AOSP / Apache 2.0) | Тільки foreground location (`ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`). Жодної фонової локації. | **Схвалено**. | https://developer.android.com/develop/sensors-and-location/location |

---

## 2. Залежності платформи та бібліотеки коду (Android / Kotlin)

| Бібліотека / SDK | Опис та модуль | Ліцензія | Статус перевірки | Першоджерело |
|---|---|---|---|---|
| **Kotlin Standard Library & Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Apache 2.0 | Схвалено | https://github.com/Kotlin/kotlinx.coroutines |
| **KotlinX Serialization JSON** | Серіалізація та парсинг погодних DTO | Apache 2.0 | Схвалено | https://github.com/Kotlin/kotlinx.serialization |
| **AndroidX Core KTX** | `androidx.core:core-ktx` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/core |
| **Jetpack Compose BOM & UI** | `androidx.compose.ui`, `material3`, `foundation` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/compose |
| **Jetpack Lifecycle & ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/lifecycle |
| **Jetpack Glance (AppWidget)** | `androidx.glance:glance-appwidget`, `androidx.glance:glance-material3` | Apache 2.0 | Схвалено | https://developer.android.com/jetpack/androidx/releases/glance |
| **Ktor Client (або OkHttp)** | Легкий клієнт для HTTP-запитів до Weather API | Apache 2.0 | Схвалено | https://ktor.io/ |
| **AndroidX DataStore** | Локальне збереження обраних міст та налаштувань | Apache 2.0 | Схвалено | https://developer.android.com/topic/libraries/architecture/datastore |
| **Google Mobile Ads Lite SDK (AdMob)** | Показ банерної реклами (компонент комерційної моделі) | Google Play Services SDK License | Схвалено для розробки з тестовими Ad Unit ID. Бойові ключі — лише Власник перед релізом. | https://developers.google.com/admob/android/quick-start |

---

## 3. Заборонені компоненти

1. Будь-які SDK аналітики (Google Firebase Analytics, Amplitude, Mixpanel тощо) — не затверджені Власником.
2. Будь-які рекламні SDK (AdMob, Unity Ads тощо) — не затверджені Власником.
3. Сторонні crash-репортери, що надсилають системні логи на зовнішні сервери без явного рішення Власника.
4. Будь-які платні API або API з обов'язковою прив'язкою банківської картки.
