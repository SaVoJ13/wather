# [TASK-009] Віджет: автооновлення кожні 10 хвилин та збереження локації користувача

- **Фаза:** 4/5 (Віджет, стабільність та збереження стану)
- **Відповідальний:** android-ui / weather-location
- **Статус:** QA_VERIFIED (Верифіковано qa-privacy, 2026-11-17)
- **Файли-власники:**
  - pp/src/main/java/com/weather/app/data/local/WeatherLocalCache.kt
  - pp/src/main/java/com/weather/app/widget/WeatherWidgetScheduler.kt
  - pp/src/main/java/com/weather/app/widget/WeatherWidgetReceiver.kt
  - pp/src/main/java/com/weather/app/widget/WeatherGlanceWidget.kt
  - pp/src/main/java/com/weather/app/ui/viewmodel/WeatherViewModel.kt
  - pp/src/main/java/com/weather/app/ui/screens/WeatherMainScreen.kt
  - pp/src/main/AndroidManifest.xml
  - pp/src/test/java/com/weather/app/widget/WeatherWidgetSchedulerTest.kt

---

## Критерії приймання (Acceptance Criteria)

1. [x] Віджет зберігає та відображає активну локацію користувача (наприклад, «Пшецлав (Przecław)») навіть після перезапуску застосунку чи процесу:
   - створено WeatherLocalCache на базі SharedPreferences + JSON;
   - зберігаються локація (
ame, country, latitude, longitude, 	imezone) та повний знімок прогнозу погоди (WeatherForecast).
2. [x] Автоматичне періодичне оновлення кожні 10 хвилин:
   - системне обмеження ndroid:updatePeriodMillis (мінімум 30 хв) обійдено за допомогою AlarmManager.setAndAllowWhileIdle з дельтою 10 хвилин (UPDATE_INTERVAL_MS = 600_000L);
   - WeatherWidgetScheduler реєструє точний аларм для WeatherWidgetReceiver.ACTION_UPDATE_WIDGET_10_MIN;
   - ресивер запускає оновлення прогнозу у фоні (performBackgroundRefresh), кешує свіжі дані, оновлює Glance-віджет через WeatherGlanceWidget().updateAll(context) та планує наступний 10-хвилинний аларм.
3. [x] Перезавантаження пристрою:
   - ресивер обробляє ACTION_BOOT_COMPLETED та ACTION_MY_PACKAGE_REPLACED, негайно відновлюючи 10-хвилинний розклад без втрати кешованої локації.
4. [x] Приватність:
   - сувора відсутність ACCESS_BACKGROUND_LOCATION; оновлення погоди виконується виключно за вже обраною/визначеною користувачем локацією.
5. [x] Написано модульні тести WeatherWidgetSchedulerTest для перевірки констант та інтервалу оновлення (10 хв).

---

## Докази незалежної перевірки (QA & Privacy Evidence)

1. **Unit-тести та збірка:**
   - Команда: ./gradlew testDebugUnitTest --rerun-tasks
   - Результат: BUILD SUCCESSFUL (22 actionable tasks, 22 executed).
   - Кількість тестів: **39 із 39 пройдено успішно** (0 failed, 0 errors, 0 skipped).
     - GeocodingMapperTest: 4/4
     - ReverseGeocoderTest: 3/3
     - LocationRepositoryTest: 4/4
     - MockWeatherRepositoryTest: 3/3
     - WeatherCodeMapperTest: 9/9
     - WeatherMapperTest: 4/4
     - WeatherThemeTest: 4/4
     - WeatherViewModelTest: 5/5
     - WeatherGlanceWidgetTest: 4/4
     - WeatherWidgetSchedulerTest: 2/2 (	estUpdateIntervalIsExactlyTenMinutes, 	estWidgetReceiverActionConstant)

2. **Перевірка дозволів та приватності:**
   - AndroidManifest.xml містить виключно:
     - ndroid.permission.INTERNET
     - ndroid.permission.ACCESS_COARSE_LOCATION
     - ndroid.permission.ACCESS_FINE_LOCATION
     - ndroid.permission.RECEIVE_BOOT_COMPLETED
   - Дозвіл ACCESS_BACKGROUND_LOCATION **суворо відсутній** у маніфесті та коді.
   - Фонове оновлення віджета (performBackgroundRefresh) не звертається до GPS-провайдерів і не виконує фонового відстеження координат; воно використовує зафіксовану користувачем локацію з WeatherLocalCache.loadActiveLocation(context).
   - Відсутні трекери, аналітичні SDK та реклама.

3. **Перевірка механізму 10-хвилинного оновлення:**
   - Константа WeatherWidgetScheduler.UPDATE_INTERVAL_MS = 10 * 60 * 1000L (600 000 мс).
   - Використано AlarmManager.setAndAllowWhileIdle з типом ELAPSED_REALTIME_WAKEUP та прапорцями PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE.
   - Ресивер WeatherWidgetReceiver обробляє ACTION_UPDATE_WIDGET_10_MIN, ACTION_BOOT_COMPLETED, ACTION_MY_PACKAGE_REPLACED.
   - У блоках inally функцій performBackgroundRefresh та updateWidgetNow гарантовано викликається scheduleNextUpdate(context, UPDATE_INTERVAL_MS).

4. **Перевірка дискового збереження стану:**
   - WeatherLocalCache реалізує збереження в SharedPreferences (MODE_PRIVATE) повної інформації про активну локацію (LocationModel) та JSON-знімка прогнозу (FullWeatherForecast).
   - WeatherGlanceWidget.provideGlance спочатку читає WeatherLocalCache.loadCachedForecast(context), забезпечуючи миттєве відображення активної локації після перезапуску процесу/пристрою.

---

## Журнал виконання (Append-only)

### 2026-11-17 23:05 — Agent 0
- Зроблено:
  - Розроблено WeatherLocalCache.kt для постійного збереження локації та прогнозу в дисковому кеші.
  - Реалізовано WeatherWidgetScheduler.kt з механізмом AlarmManager.setAndAllowWhileIdle кожні 10 хвилин.
  - Оновлено WeatherWidgetReceiver.kt, WeatherGlanceWidget.kt, WeatherViewModel.kt, WeatherMainScreen.kt, AndroidManifest.xml.
  - Додано WeatherWidgetSchedulerTest.kt.
  - Збірка APK та unit-тести: 39/39 успішно.
  - Встановлено на пристрій Samsung Galaxy S24 Ultra (RZCX326349A).
  - Перевірено відображення віджета 4x2: назва села «Пшецлав (Przecław)» та поточна температура відображаються коректно.
- Стан: Реалізовано. Передається qa-privacy для незалежної перевірки (Редлайн №9).

### 2026-11-17 23:25 — qa-privacy
- Проведено незалежний аудит коду та верифікацію критеріїв згідно з Конституцією v2.0 (п. 2.1, Червона лінія №9).
- Перевірено відсутність ACCESS_BACKGROUND_LOCATION.
- Виконано повний прогін unit-тестів: 39/39 успішно (0 помилок, 0 пропущено).
- Перевірено коректність констант та логіки збереження стану й планування алармів.
- Вердикт: **QA_VERIFIED**. Завдання повністю відповідає Definition of Done.
