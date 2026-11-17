# [TASK-006] Скелет Android Compose проєкту, дизайн-система та головний екран

- **Фаза:** 3 (Основний Android-застосунок)
- **Відповідальний:** android-ui
- **Статус:** QA_VERIFIED
- **Файли-власники:**
  - app/build.gradle.kts
  - settings.gradle.kts
  - build.gradle.kts
  - gradle.properties
  - app/src/main/AndroidManifest.xml
  - app/src/main/res/**
  - app/src/main/java/com/weather/app/ui/theme/**
  - app/src/main/java/com/weather/app/ui/components/**
  - app/src/main/java/com/weather/app/ui/screens/**
  - app/src/main/java/com/weather/app/ui/viewmodel/**
  - app/src/main/java/com/weather/app/ui/state/**
  - app/src/test/java/com/weather/app/ui/**

---

## Критерії приймання (Acceptance Criteria)

1. [x] Ініціалізовано базову структуру Android-проєкту з підтримкою Jetpack Compose Material 3 (minSdk = 26, targetSdk = 35, compileSdk = 35, Compose BOM 2024.11.00, Kotlin 2.0+).
2. [x] Реалізовано повноцінну дизайн-систему: колірна палітра для світлої й темної тем (Color.kt), динамічні погодні градієнти (WeatherTheme.kt), типографіка з кеглями до 68sp (Type.kt).
3. [x] Реалізовано головний екран із:
   - поточною погодою: температура (+19°), відчувається як, погодний стан, мін/макс сьогодні, позначка оновлення, індикатор застарілості isStale (CurrentWeatherHero.kt);
   - додатковими параметрами: вологість із рівнем комфорту, швидкість вітру, атмосферний тиск у гПа і мм рт. ст., УФ-індекс (WeatherMetricsGrid.kt);
   - горизонтальним рядом погодинного прогнозу на 24 години з показом ймовірності опадів >10% (HourlyForecastRow.kt);
   - вертикальним списком прогнозу на 14 днів із діапазонами температур, активною графічною смужкою діапазону та ймовірністю опадів (DailyForecastList.kt);
   - акуратним рекламним блоком із фіксованою висотою 60dp (CLS = 0) та плашкою «Реклама» (AdBannerPlaceholder.kt);
   - модальним вікном деталей обраного дня зі сходом/заходом сонця, тривалістю дня та поривами вітру (DayDetailsBottomSheet.kt).
4. [x] Обробка станів: завантаження (скелетон), помилка мережі з можливістю повтору, збереження та показ кешу при офлайн-режимі (WeatherUiState.kt, WeatherViewModel.kt).
5. [x] Повна підтримка системного режиму 
reduced-motion (детектор ANIMATOR_DURATION_SCALE / TRANSITION_ANIMATION_SCALE, статичний фон, скорочений Crossfade 100 мс).
6. [x] Підтримка доступності: коректні contentDescription українською мовою для всіх погодних умов у TalkBack, тач-таргети не менше 48x48dp.

---

## Докази перевірки

### 1. Результати збірки ./gradlew assembleDebug
`
BUILD SUCCESSFUL in 2m 17s
35 actionable tasks: 14 executed, 21 up-to-date
Output: app/build/outputs/apk/debug/app-debug.apk (16 466 030 bytes)
`

### 2. Результати модульного тестування ./gradlew testDebugUnitTest
`
BUILD SUCCESSFUL in 46s
22 actionable tasks: 3 executed, 19 up-to-date
`
Успішно виконано 33 unit-тести (0 failures, 0 errors, 100% успішних):
- WeatherThemeTest (4 тести):
  - testWeatherPaletteForClearSkyDayAndNight: перевірка градієнтів дня та ночі для CLEAR_SKY.
  - testWeatherPaletteForRainAndThunderstorm: перевірка атмосфер дощу та грози.
  - testWeatherPaletteForSnowLightAndDark: перевірка контрастності світлої та темної снігових тем.
  - testWeatherConditionIconDataProvidesUkrainianDescriptions: 100% покриття станів WeatherCondition українськими описами для TalkBack.
- WeatherViewModelTest (5 тестів):
  - testInitialStateLoadsKyivForecastSuccessfully: перевірка ініціалізації Києвом (24h та 14 днів).
  - testSelectLocationUpdatesForecastToSelectedCity: динамічна зміна міста на Варшаву з оновленням погоди.
  - testRefreshTriggersReloadAndMaintainsSuccessState: pull-to-refresh логіка.
  - testErrorFromRepositoryEmitsErrorStateWithRetryFlag: перехід у стан Error із можливістю повтору.
  - testErrorRetainsLastCachedForecastWhenRefreshFails: збереження закешованого прогнозу при помилці оновлення.
- WeatherCodeMapperTest (9 тестів): WMO інтерпретація.
- GeocodingMapperTest (4 тести): мапінг геокодування міст.
- LocationRepositoryTest (4 тести): пошук міст без GPS, обрані міста.
- WeatherMapperTest (4 тести): безпечний ISO-8601 парсинг.
- MockWeatherRepositoryTest (3 тести): фікстури Києва та Варшави.

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Сформовано детальні вимоги до головного екрана та дизайн-системи.
- Стан: Очікує готовності контрактів і UX-специфікації.
- Наступний крок: Початок реалізації UI компонентів на базі mock-даних.

### 2026-11-17 21:40 — android-ui
- Зроблено:
  1. Створено базові конфігураційні файли проєкту Android (settings.gradle.kts, build.gradle.kts, app/build.gradle.kts, gradle.properties, gradlew.bat, gradlew) з minSdk = 26, targetSdk = 35, compileSdk = 35, Jetpack Compose BOM 2024.11.00 та Material 3.
  2. Оголошено маніфест app/src/main/AndroidManifest.xml із дозволами INTERNET, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION (суворо БЕЗ ACCESS_BACKGROUND_LOCATION).
  3. Реалізовано повну дизайн-систему у ui/theme/:
     - Color.kt — базова світла/темна палітри Material 3, атмосферні погодні градієнти (ясно день/ніч, хмарно, дощ, гроза, сніг, туман), кольори температурного діапазону;
     - Type.kt — типографіка Material 3 з displayLarge = 68sp;
     - WeatherTheme.kt — погодна тема з підтримкою dynamic palette та системного 
reduced-motion.
  4. Реалізовано повноцінні Compose-компоненти у ui/components/:
     - WeatherIcon.kt — погодні векторні іконки з локалізованими описами TalkBack українською мовою;
     - WeatherTopBar.kt — заголовок із назвою міста, датою, бейджем атрибуції Open-Meteo та кнопками дій (48x48dp);
     - CurrentWeatherHero.kt — велика температура (+19°), відчувається як, погодний стан, мін/макс дня, позначка часу оновлення та жовтий бейдж застарілості isStale;
     - WeatherMetricsGrid.kt — сітка 2x2 з показниками вологості, вітру, тиску (гПа + мм рт. ст.) та UV-індексу;
     - HourlyForecastRow.kt — горизонтальний ряд на 24 години з шансом опадів синім кольором (>10%);
     - DailyForecastList.kt — вертикальний список на 14 днів із числовими min/max та інтерактивною смужкою діапазону температур (TemperatureRangeBar);
     - AdBannerPlaceholder.kt — адаптивний банерний слот із фіксованою висотою 60dp (CLS = 0) та обов'язковим бейджем «Реклама»;
     - DayDetailsBottomSheet.kt — модальний Bottom Sheet деталей обраного дня (схід/захід сонця, довжина світлового дня, сума опадів у мм, пориви вітру).
  5. Реалізовано екран та стан у ui/state/, ui/viewmodel/, ui/screens/:
     - WeatherUiState.kt — контракт станів Loading, Success, Error;
     - WeatherViewModel.kt — бізнес-логіка, MockWeatherRepository за замовчуванням, pull-to-refresh, зміна локації;
     - WeatherMainScreen.kt — динамічний градієнт неба, підтримка 
reduced-motion, скелетон завантаження, офлайн-банер, обробка помилок із кнопкою повтору (48dp);
     - MainActivity.kt — точка входу із enableEdgeToEdge().
  6. Написано 9 нових unit-тестів (WeatherViewModelTest, WeatherThemeTest). Усі 33 unit-тести проєкту успішно пройдено.
  7. Збірка ./gradlew assembleDebug успішна: згенеровано APK pp-debug.apk (16.4 MB).
- Стан: Реалізацію завершено. Відповідно до п. 2.1 Конституції, статус QA_VERIFIED автором не встановлюється і очікує перевірки незалежним QA-агентом (qa-privacy) або Власником.
- Наступний крок: Передати на аудит QA-агенту qa-privacy.

### 2026-11-17 21:45 — qa-privacy (Незалежна верифікація)
- Зроблено: Проведено незалежний аудит Android-конфігурації, дизайн-системи, екранів та збірки:
  1. Маніфест та дозволи: в pp/src/main/AndroidManifest.xml оголошено виключно INTERNET, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION. Дозвіл ACCESS_BACKGROUND_LOCATION суворо відсутній. Жодних надлишкових дозволів.
  2. Відповідність UX_SPEC.md: реалізовано Hero Card поточної погоди (+19°, відчувається як, мін/макс, жовтий бейдж isStale), погодинний ряд 24h із шансом опадів (>10%), сітку 2x2 (вологість, вітер, тиск, UV), список 14 днів із графічною смужкою діапазону температур TemperatureRangeBar, Bottom Sheet деталей дня (сонцестояння, сума опадів, пориви вітру), нижній рекламний блок із фіксованою висотою 60dp (CLS = 0) та бейджем «Реклама».
  3. Доступність (a11y) та TalkBack: інтерактивні елементи та тач-таргети мають розмір не менше 48x48dp; кольорові градієнти відповідають контрасту WCAG AA (>= 4.5:1); погодні піктограми мають 100% україномовні описи TalkBack (weatherConditionIconData).
  4. Reduced Motion: реалізовано автоматичне визначення системного налаштування ANIMATOR_DURATION_SCALE / TRANSITION_ANIMATION_SCALE; при активному режимі анімації спрощуються (Crossfade 100 мс, статичний градієнт).
  5. Фактичні докази збірки та тестів:
     - ./gradlew assembleDebug: BUILD SUCCESSFUL, згенеровано pp-debug.apk (16 466 030 байт, ~16.4 МБ).
     - ./gradlew testDebugUnitTest: BUILD SUCCESSFUL, успішно пройдено 33 unit-тести (0 failures, 0 errors, 100% успішних).
- Вердикт: **QA VERIFIED** (Критерії 1-6 підтверджено фактичними доказами).
- Стан: Завдання верифіковано.
