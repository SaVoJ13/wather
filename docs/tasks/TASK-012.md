# [TASK-012] Виправлення погодинного прогнозу від поточного часу, переміщення блоку та заміна швидкого вибору на вибрані міста

- **Фаза:** 3/4 (UX, точність даних та персоналізація пошуку)
- **Відповідальний:** android-ui / weather-location
- **Статус:** `QA_VERIFIED`
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/data/mapper/WeatherMapper.kt`
  - `app/src/main/java/com/weather/app/ui/screens/WeatherMainScreen.kt`
  - `app/src/main/java/com/weather/app/ui/components/SearchCityBottomSheet.kt`
  - `app/src/main/java/com/weather/app/ui/viewmodel/WeatherViewModel.kt`
  - `app/src/main/java/com/weather/app/data/local/WeatherLocalCache.kt`
  - `app/src/test/java/com/weather/app/data/WeatherMapperTest.kt`
  - `app/src/test/java/com/weather/app/ui/WeatherViewModelTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Погодинний прогноз від поточного часу (а не від 00:00):
   - у `WeatherMapper.kt` список `hourly24h` формується починаючи з поточної години (`currentHour = updatedAt.truncatedTo(ChronoUnit.HOURS)`), першим елементом є поточна година з підписом «Зараз» у `HourlyForecastRow`, далі 24 години наперед;
   - додано юніт-тест `toDomain_hourlyForecastStartsFromCurrentHourNotMidnight` у `WeatherMapperTest.kt`.
2. [x] Переміщення блоку погодинного прогнозу:
   - у `WeatherMainScreen.kt` блок `HourlyForecastRow` розміщено безпосередньо під `CurrentWeatherHero` (перед блоком метрик `WeatherMetricsGrid`).
3. [x] Заміна «Швидкого вибору» на «Вибрані міста»:
   - у `SearchCityBottomSheet.kt` повністю прибрано заголовок «Швидкий вибір:» та статичні популярні міста;
   - додано блок «Вибрані міста:» з горизонтальним списком чіпів;
   - збереження та завантаження вибраних міст реалізовано у `WeatherLocalCache` (постійний кеш у `SharedPreferences`);
   - при виборі населеного пункту з пошуку або по GPS він додається на початок списку вибраних міст (`addSelectedCity`);
   - реалізовано можливість видалення міста зі списку вибраних через кнопку-хрестик (`removeSelectedCity`);
   - при відкритті пошуку користувач бачить власні збережені міста в 1 клік.
4. [x] Тестування:
   - усі юніт-тести проходять на 100% (`BUILD SUCCESSFUL in 1m 7s`, 54/54 тестів успішно, 0 помилок).
5. [x] Незалежна верифікація QA:
   - перевірено та підтверджено агентом `qa-privacy` зі статусом `QA_VERIFIED` (відсутність витоків даних, безпечний GPS без background location, відсутність сторонніх трекерів).

---

## Журнал виконання (Append-only)

### 2026-11-20 11:15 — Agent 0
- Зроблено: Отримано запит Власника. Створено картку TASK-012 з трьома чіткими критеріями приймання.
- Стан: Початок реалізації.

### 2026-11-20 11:22 — android-ui / weather-location
- Зроблено: 
  - Реалізовано обрізання погодинного прогнозу за поточною годиною в `WeatherMapper.kt`.
  - Переміщено `HourlyForecastRow` під `CurrentWeatherHero` у `WeatherMainScreen.kt`.
  - Замінено «Швидкий вибір» на «Вибрані міста» у `SearchCityBottomSheet.kt`, додано збереження/видалення в `WeatherLocalCache.kt` та `WeatherViewModel.kt`.
  - Додано нові тести в `WeatherMapperTest.kt` та `WeatherViewModelTest.kt`.
- Стан: READY_FOR_REVIEW.

### 2026-11-20 11:27 — qa-privacy (Незалежна верифікація)
- Зроблено: 
  - Проведено незалежний код-рев'ю змін у `WeatherMapper.kt`, `WeatherMainScreen.kt`, `SearchCityBottomSheet.kt`, `WeatherViewModel.kt`, `WeatherLocalCache.kt`.
  - Запущено повний набір юніт-тестів: `./gradlew.bat testDebugUnitTest --rerun-tasks` -> 54/54 тестів успішно (100%), 0 помилок, 0 пропущено (`BUILD SUCCESSFUL`).
  - Перевірено відсутність витоків локації/секретів у логах (жодного виклику `Log.` чи `println`).
  - Перевірено маніфест: відсутність `ACCESS_BACKGROUND_LOCATION` та сторонніх аналітичних/рекламних SDK.
- Стан: QA_VERIFIED ✅.
