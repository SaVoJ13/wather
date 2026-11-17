# [TASK-007] Домашній погодний AppWidget на базі Jetpack Glance

- **Фаза:** 4 (Віджет та анімація)
- **Відповідальний:** android-ui
- **Статус:** `IN_REVIEW`
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/widget/WeatherGlanceWidget.kt`
  - `app/src/main/java/com/weather/app/widget/WeatherWidgetReceiver.kt`
  - `app/src/main/res/xml/weather_widget_info.xml`
  - `app/src/main/res/drawable/ic_widget_*.xml`
  - `app/src/main/res/drawable/widget_background.xml`
  - `app/src/test/java/com/weather/app/widget/WeatherGlanceWidgetTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Реалізовано компактний (2x2) та стандартний (4x2) варіанти віджета на базі Jetpack Glance (`androidx.glance.appwidget` 1.1.1, `SizeMode.Responsive`).
2. [x] Відображення ключових даних: поточна температура (великим кеглем), погодна іконка, назва міста, ймовірність опадів та час останнього оновлення.
3. [x] Енергоефективність: оновлення за системним розкладом (`updatePeriodMillis="1800000"`, 30 хв) або при оновленні в застосунку.
4. [x] Сувора відсутність покадрової нескінченної анімації у віджеті (дотримання системних лімітів RemoteViews).
5. [x] Клік на віджет коректно відкриває головний екран застосунку (`MainActivity`) через `actionStartActivity(openAppIntent)`.
6. [x] Стійкість до офлайн-режиму: показ останнього відомого прогнозу з позначкою часу або нейтрального стану без крашів.
7. [x] Сувора відсутність реклами у віджеті згідно з політиками Google Play та ADR-005.

---

## Докази перевірки

### 1. Результати збірки `./gradlew assembleDebug`
```
BUILD SUCCESSFUL in 55s
35 actionable tasks: 6 executed, 29 up-to-date
Артефакт: app/build/outputs/apk/debug/app-debug.apk (розмір: 20 813 887 байт)
```

### 2. Результати модульного тестування `./gradlew testDebugUnitTest`
```
BUILD SUCCESSFUL in 45s
22 actionable tasks: 7 executed, 15 up-to-date
```
Успішно виконано **37 unit-тестів** (0 failures, 0 errors, 100% успішних):
- `WeatherGlanceWidgetTest` (4 тести):
  - `testWeatherConditionDrawableResMapsAllConditions`: 100% покриття всіх значень `WeatherCondition` позитивними ID ресурсів;
  - `testSpecificWeatherConditionDrawables`: точний мапінг іконок для ясно, хмарно, дощ, сніг, гроза, туман;
  - `testWidgetReceiverProvidesGlanceAppWidget`: перевірка створення інстансу `WeatherGlanceWidget` ресивером;
  - `testWidgetResponsiveSizesConfigured`: перевірка наявності режимів розміру `COMPACT_SIZE` (120x100dp) та `EXPANDED_SIZE` (220x100dp).
- Усі попередні 33 тести домену, маперів, геокодування та UI також успішно виконано (100% зелені).

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Створено картку завдання віджета з акцентом на енергоефективність та обмеження Glance.
- Стан: Очікує старту Фази 4.
- Наступний крок: Реалізація віджета після стабілізації UI моделей.

### 2026-11-17 21:50 — android-ui
- Зроблено:
  1. Підключено бібліотеки Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1` та `androidx.glance:glance-material3:1.1.1`) до `app/build.gradle.kts`.
  2. Створено векторні ресурси погодних умов у `res/drawable/` (`ic_widget_sunny.xml`, `ic_widget_cloudy.xml`, `ic_widget_rain.xml`, `ic_widget_snow.xml`, `ic_widget_thunderstorm.xml`, `ic_widget_fog.xml`) та фоновий контейнер `widget_background.xml` (скруглення 20dp, напівпрозорий темний фон з тонкою обводкою).
  3. Створено конфігураційний XML провайдера `app/src/main/res/xml/weather_widget_info.xml` з `targetCellWidth="2"`, `targetCellHeight="2"`, `updatePeriodMillis="1800000"` (30 хв) та описом віджета.
  4. Зареєстровано `WeatherWidgetReceiver` у `app/src/main/AndroidManifest.xml` з дією `android.appwidget.action.APPWIDGET_UPDATE` та метаданими.
  5. Реалізовано `WeatherGlanceWidget`:
     - Режим адаптивності `SizeMode.Responsive` для компактного (2x2) та розширеного (4x2) макетів;
     - Компактний макет: назва міста, поточна температура великим шрифтом (32sp), іконка стану, мін/макс дня, час оновлення;
     - Розширений макет: ліва колонка (місто, температура, стан, відчувається як, час оновлення) + права колонка (міні-прогноз на найближчі 3 години з відсотком опадів);
     - Дія за кліком: `actionStartActivity(openAppIntent)` для миттєвого відкриття `MainActivity`;
     - Енергозбереження: 0 безперервних анімацій, оновлення суворо за системним таймером або оновленням у застосунку;
     - Безпека: сувора відсутність реклами у віджеті (дотримання гайдлайнів Android).
  6. Написано 4 unit-тести у `WeatherGlanceWidgetTest.kt`. Усі 37 unit-тестів проєкту виконано успішно.
  7. Збірка `./gradlew assembleDebug` успішна: згенеровано APK `app-debug.apk` (20.8 МБ).
- Стан: Реалізацію завершено. Відповідно до п. 2.1 Конституції, статус `QA_VERIFIED` автором не встановлюється і очікує перевірки незалежним QA-агентом (`qa-privacy`) або Власником.
- Наступний крок: Передати на аудит QA-агенту `qa-privacy`.
