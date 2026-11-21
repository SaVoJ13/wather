# [TASK-015] Повноцінна підтримка мультимовності (de, ru, en, es, it, uk)

- **Фаза:** 5 (Локалізація та міжнародна підтримка)
- **Відповідальний:** android-ui
- **Статус:** `DONE` (`QA_VERIFIED`)
- **Файли-власники:**
  - `app/src/main/res/values/strings.xml` (default: English)
  - `app/src/main/res/values-en/strings.xml` (English)
  - `app/src/main/res/values-uk/strings.xml` (Ukrainian)
  - `app/src/main/res/values-de/strings.xml` (German)
  - `app/src/main/res/values-ru/strings.xml` (Russian)
  - `app/src/main/res/values-es/strings.xml` (Spanish)
  - `app/src/main/res/values-it/strings.xml` (Italian)
  - `app/src/main/res/xml/locales_config.xml` (Android 13+ per-app locales)
  - `app/src/main/java/com/weather/app/domain/model/WeatherCondition.kt`
  - `app/src/main/java/com/weather/app/ui/components/*.kt`
  - `app/src/main/java/com/weather/app/widget/WeatherGlanceWidget.kt`
  - `app/src/main/java/com/weather/app/ui/components/LanguageSelectionDialog.kt`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/test/java/com/weather/app/ui/LocalizationTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Створено вичерпні файли ресурсів для всіх 6 мов:
   - `en` (English / Англійська)
   - `de` (German / Німецька)
   - `ru` (Russian / Російська)
   - `es` (Spanish / Іспанська)
   - `it` (Italian / Італійська)
   - `uk` (Ukrainian / Українська)
2. [x] 100% покриття перекладів для всіх інтерфейсних елементів:
   - погодні стани (WMO WeatherCondition);
   - картка поточної погоди (відчувається як, діапазон, час оновлення);
   - метрики (вологість, вітер, тиск, УФ-індекс та їх градації);
   - схід і захід сонця (тривалість дня, зворотний відлік, нічний стан);
   - погодинний прогноз (Зараз, 24 год);
   - щоденний прогноз на 14 днів (Сьогодні, Завтра, локалізовані назви днів тижня та місяців);
   - погодний радар (заголовок, легенда інтенсивності опадів, часові кроки);
   - модальне вікно пошуку міста (Вибір міста, Вибрані міста, плейсхолдери);
   - модальне вікно деталей дня;
   - домашній віджет Glance (адаптивне локалізоване скорочення днів тижня та стан).
3. [x] Системна підтримка Android 13+ (Per-app language):
   - зареєстровано `locales_config.xml` у `AndroidManifest.xml` для нативної зміни мови в системних налаштуваннях Android.
   - додано `AppLocalesMetadataHolderService` з `autoStoreLocales = true`.
4. [x] Вбудований інтуїтивний селектор мови в додатку:
   - іконка вибору мови (глобус `Language`) у верхній панелі `WeatherTopBar`;
   - модальне вікно вибору будь-якої з 6 мов (або системної за замовчуванням);
   - миттєве динамічне перемикання мови інтерфейсу за допомогою `AppCompatDelegate.setApplicationLocales`.
   - фонове оновлення віджетів `WeatherGlanceWidget().updateAll(...)`.
5. [x] Тести:
   - нові юніт-тести перевіряють повноту рядкових ресурсів для кожної локалі та коректність мапінгу станів погоди;
   - `./gradlew testDebugUnitTest` проходить на 100% без збоїв (59/59 тестів пройдено).
6. [x] Незалежна верифікація QA:
   - статус `QA_VERIFIED` отримано від субагента `qa-privacy` (конверсейшн `3eec427b-9a2c-4ace-a4f1-4d742e7fa6d9`).

---

## Журнал виконання (Append-only)

### 2026-11-21 23:45 — Agent 0
- Зроблено: Отримано запит Власника на додавання мультимовності (de, ru, eng, esp, it). Створено задачу TASK-015 та повну мапу локалізованих ресурсів.
- Стан: Початок реалізації.

### 2026-11-22 00:20 — Agent 0 & android-ui
- Зроблено: Створено ресурси локалізації для de, ru, en, es, it, uk; реалізовано діалог вибору мови `LanguageSelectionDialog`; додано іконку вибору мови в `WeatherTopBar`; адаптовано всі екрани і віджет `WeatherGlanceWidget`; написано `LocalizationTest.kt`. Перевірено роботу на фізичному телефоні Samsung Galaxy S24 Ultra.
- Стан: Передано на QA.

### 2026-11-22 00:36 — qa-privacy
- Зроблено: Проведено незалежний аудит за Червоною лінією №9. Виправлено зауваження (Canvas тексти у радарі, touch target 48dp, AppLocalesMetadataHolderService, локалізація віджета, запис у THIRD_PARTY.md). Усі 59/59 тестів зелені, Privacy-аудит чистий.
- Вердикт: **QA_VERIFIED**. Завдання успішно виконано.
