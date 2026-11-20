# [TASK-013] Преміум-редизайн домашнього віджета Jetpack Glance (Glassmorphism & Balanced Layout)

- **Фаза:** 4 (Візуальна система віджета та персоналізація)
- **Відповідальний:** android-ui
- **Статус:** `QA_VERIFIED`
- **Файли-власники:**
  - `app/src/main/res/drawable/widget_background.xml`
  - `app/src/main/java/com/weather/app/widget/WeatherGlanceWidget.kt`
  - `app/src/test/java/com/weather/app/widget/WeatherGlanceWidgetTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Фон віджета (widget_background.xml):
   - градієнтна скляна підкладка (Glassmorphism):
     - заокруглені кути: 28dp;
     - лінійний градієнт під кутом 135° (від лівого верхнього до правого нижнього кута):
       - startColor: #E6232D48 (глибокий сапфір із легким просвітом);
       - centerColor: #EA1C243B;
       - endColor: #F2141A2B (темний насичений тон);
     - делікатна тонка рамка: stroke 1dp кольору #38FFFFFF (ефект відблиску на фасці скла).
2. [x] Ліва колонка (ExpandedWidgetContent у WeatherGlanceWidget.kt):
   - усунуто порожнечу, створено насичену і збалансовану ієрархію:
     - велика виразна погодна іконка (48dp);
     - температура: велика, чітка (40sp, FontWeight.Bold, Color.White);
     - стан погоди: forecast.current.condition.descriptionUk (13sp, FontWeight.Medium, Color(0xFFF1F5F9));
     - рядок з відчутною температурою та діапазоном: Відчувається як $feelsLikeString • ↑$maxToday° / ↓$minToday° (11sp, Color(0xFF94A3B8));
     - рядок мікро-метрик: вологість 💧 % та вітер 💨  км/год (11sp, Color(0xFFCBD5E1), FontWeight.Medium);
     - рядок локації та часу: мітка ic_widget_location + ${forecast.location.name} • $updateTime (11sp, Color(0xFF94A3B8)).
3. [x] Права колонка (щоденний прогноз):
   - усунуто надлишкові проміжки:
     - день тижня: formatDayOfWeekUk (13sp, FontWeight.Medium, Color(0xFFCBD5E1)), ширина 26dp;
     - по центру: іконка погоди дня (22dp) + якщо ймовірність опадів pop > 15%, поруч бейдж 💧 % кольору Color(0xFF38BDF8);
     - праворуч: чітка ієрархія температур — денна (максимальна) ${max}° (14sp, FontWeight.Bold, Color.White) та нічна (мінімальна) ${min}° (13sp, FontWeight.Normal, Color(0xFF94A3B8));
     - тонкі горизонтальні розділювачі: 1dp напівпрозорого кольору Color(0x1FFFFFFF).
4. [x] Компактний режим 2x2 (CompactWidgetContent):
   - освіжено кольори та шрифти під новий сапфіровий градієнт.
5. [x] Тести:
   - усі юніт-тести в WeatherGlanceWidgetTest проходять успішно, збірка повністю зелена (55/55 тестів успішно, 0 помилок).
6. [x] Незалежна верифікація QA та приватність:
   - підтверджено агентом qa-privacy: відсутність сторонніх трекерів, відсутність реклами у віджеті, відсутність ACCESS_BACKGROUND_LOCATION, відсутність координат у логах.

---

## Журнал виконання (Append-only)

### 2026-11-20 11:35 — Agent 0
- Зроблено: Поставлено завдання на преміум-редизайн домашнього віджета Jetpack Glance згідно з TASK-013.

### 2026-11-20 11:41 — android-ui
- Зроблено:
  - Оновлено widget_background.xml: заокруглення 28dp, лінійний градієнт 135° (startColor #E6232D48, centerColor #EA1C243B, endColor #F2141A2B), фаска 1dp #38FFFFFF.
  - Збагачено ліву колонку ExpandedWidgetContent: іконка 48dp, температура 40sp bold біла, стан погоди 13sp medium #F1F5F9, відчувається як + діапазон дня 11sp #94A3B8, мікро-метрики вологість + вітер 11sp #CBD5E1, локація з міткою + час 11sp #94A3B8.
  - Збагачено праву колонку ExpandedWidgetContent: день тижня 13sp medium #CBD5E1, іконка 22dp + бейдж 💧 pop% при pop > 15%, температури ${max}° (14sp bold) та ${min}° (13sp), розділювачі 1dp Color(0x1FFFFFFF).
  - Освіжено палітру та типографіку для CompactWidgetContent (2x2).
  - Додано тест розмірів віджета testWidgetSizes у WeatherGlanceWidgetTest.kt.
- Стан: IMPLEMENTED, READY_FOR_QA.

### 2026-11-20 11:50 — qa-privacy (Незалежна верифікація)
- Зроблено:
  - Проведено незалежний код-рев'ю `widget_background.xml` (градієнт 135°, радіус 28dp, кольори #E6232D48 / #EA1C243B / #F2141A2B, обведення 1dp #38FFFFFF), `WeatherGlanceWidget.kt` та `WeatherGlanceWidgetTest.kt`.
  - Запущено повний цикл юніт-тестів: `./gradlew.bat testDebugUnitTest` -> 55/55 тестів успішно (100%), 0 помилок, 0 пропущено (включно з 6/6 тестами для віджета).
  - Зібрано APK `assembleDebug` (`BUILD SUCCESSFUL in 38s`) та встановлено на підключений пристрій Samsung Galaxy S24 Ultra (`adb install -r`, Success).
  - Надіслано широкомовне повідомлення оновлення віджета `ACTION_UPDATE_WIDGET_10_MIN` (`result=0`).
  - Перевірено відсутність витоків локації, ключів та секретів у логах (`Log.` / `println` відсутні в коді).
  - Перевірено маніфест і залежності: відсутність `ACCESS_BACKGROUND_LOCATION`, відсутність аналітики та сторонніх рекламних SDK.
  - Візуальне відображення віджета на фізичному заблокованому екрані: *не перевірено на пристрої* (очікує розблокування екрана Власником).
- Стан: `QA_VERIFIED` ✅.
