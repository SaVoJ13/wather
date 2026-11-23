# План виконання Phase 5: Widgets & Animation
# PHASE_5_EXECUTION_PLAN.md

> **Проєкт:** Weather — Smart Forecast  
> **Координатор:** Agent 0 (Lead Architect / Technical Project Manager)  
> **Дата складання:** 2026-11-23 (Europe/Warsaw)  
> **Статус:** **ЗАТВЕРДЖЕНО ДО ВИКОНАННЯ (OWNER APPROVED ADR-016)**  
> **Нормативна база:** `docs/governance/CONSTITUTION.uk.md` v2.0, `docs/governance/DECISIONS.md` (ADR-001–ADR-016), `docs/architecture/WIDGET_AD_CONTRACTS.md`, `docs/ux/DESIGN_SYSTEM.md`, `docs/ux/UI_SPECIFICATION.md`.

---

## 1. Мета та пріоритети Phase 5

**Головна мета Phase 5:** Забезпечити бездоганну роботу домашніх Android-віджетів на базі **Jetpack Glance** у розмірах **2×2 (Compact)** та **4×2 (Expanded)** із реальними погодними даними, чесною роботою з офлайн-кешем (0 вигаданих значень і 0 Mock-фолбеку у виробничому віджеті), енергоощадним оновленням за правилами ОС Android (без примусових CPU wakeup та без хибних обіцянок точного 10-хвилинного інтервалу в режимі Doze), повною відсутністю фонового GPS і реклами у віджетах, а також удосконалити анімації в застосунку з повною підтримкою системного режиму **Reduced Motion**.

---

## 2. Непорушні обмеження та Червоні лінії Phase 5 (Constitution v2.0 & ADR-016)

1. **Заборона самоверифікації (п. 2.1, Червона лінія №9):** Жоден автор не ставить `QA Verified` на власну роботу. Незалежну верифікацію виконує **Agent 5 (`qa-privacy`)**.
2. **Жодних вигаданих даних у віджетах (Data Integrity in Glance):**
   - Вилучити з `WeatherGlanceWidget.kt` будь-який fallback на `MockWeatherRepository.createKyivForecast()`.
   - Якщо локальний дисковий кеш (`WeatherLocalCache`) порожній (наприклад, віджет додано на робочий стіл до першого завантаження прогнозу), відображати чесний локалізований стан **«Немає даних»** із підказкою натиснути для відкриття застосунку (`EmptyWidgetContent`), а не вигадану погоду.
   - Усунути підстановку `?: 0` для мінімальної та максимальної температури дня у віджеті (`minToday` / `maxToday`): діапазон `↑max° / ↓min°` відображається лише за наявності реального елемента `today` у `daily14Days`.
   - Відображати індикатор застарілого/офлайн-кешу (`isStale` або вік кешу > 60 хв) чесно біля мітки часу.
3. **Енергоощадне оновлення за правилами Android (Battery & OS Compliance):**
   - У `WeatherWidgetScheduler.kt` замінити `AlarmManager.ELAPSED_REALTIME_WAKEUP` на енергоощадний **`AlarmManager.ELAPSED_REALTIME`** (non-wakeup), щоб віджет ніколи не будив процесор смартфона під час глибокого сну (Doze Mode).
   - Чітко зафіксувати в коді та документації, що оновлення відбувається за дозволеним системою Android графіком (In-App push при відкритті застосунку/зміні міста + фоновий non-wakeup інтервал за розсудом ОС + `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED`), без гарантії точного 10-хвилинного інтервалу.
4. **Сувора заборона фонового GPS та реклами у віджетах:**
   - 0 `ACCESS_BACKGROUND_LOCATION` у маніфесті.
   - 0 викликів геолокації з пакета `com.weather.app.widget`.
   - 0% рекламних блоків чи банерів усередині віджетів `2×2` та `4×2`.
5. **Анімації та підтримка Reduced Motion:**
   - Плавна анімація переходу кольорів небесного градієнта (`animateColorAsState` 600 мс) при зміні погодного стану/міста з миттєвим перемиканням (`snap()`) при `LocalReducedMotion.current == true`.
   - Дотримання `LocalReducedMotion` у `Crossfade` між вкладками (`WeatherTabContent`) та станами екрана.
   - Оптимізація `SunriseSunsetCard`: не запускати `rememberInfiniteTransition` (пульсацію сонця), коли увімкнено системний режим `Reduced Motion`, щоб уникнути зайвих композицій кадрів.
6. **Збереження стабільності ядра та бюджету $0.00:**
   - Не змінювати погодну бізнес-логіку, три основні вкладки («Зараз», «Прогноз», «Місця») та затверджений дизайн.
   - Не активувати бойову рекламу AdMob (Phase 7).
   - Не переходити до Phase 6 без окремого `Owner Approved`.

---

## 3. Розподіл завдань між 6 AI-агентами

```
                        ┌────────────────────────────────────────────────────────┐
                        │              AGENT 0: Lead Architect / PM              │
                        │     (ADR-016, Координація Phase 5, Контракти, CI)      │
                        └───────────────────────────┬────────────────────────────┘
                                                    │
         ┌───────────────────┬──────────────────────┼─────────────────────┬──────────────────┐
         │                   │                      │                     │                  │
         ▼                   ▼                      ▼                     ▼                  ▼
┌─────────────────┐ ┌─────────────────┐  ┌────────────────────┐ ┌──────────────────┐ ┌────────────────┐
│    AGENT 1      │ │    AGENT 2      │  │      AGENT 3       │ │     AGENT 4      │ │    AGENT 5     │
│   Product/UX    │ │   Android UI    │  │  Weather/Location  │ │     Widget &     │ │  QA & Privacy  │
│ (UX віджетів    │ │ (Анімації фону, │  │ (Цілісність кешу   │ │   Monetization   │ │ (Тести Glance  │
│ 2x2/4x2,        │ │ вкладок, Canvas │  │ для віджетів,      │ │ (Glance 2x2/4x2, │ │ 2x2/4x2, S24   │
│ Reduced Motion) │ │ Reduced Motion) │  │ свіжість даних)    │ │ non-wakeup sched)│ │ Ultra, Аудит)  │
└─────────────────┘ └─────────────────┘  └────────────────────┘ └──────────────────┘ └────────────────┘
```

### 3.1. Agent 0: Lead Architect & Technical Project Manager
- **TASK-059:** Фіксація рішення Власника `ADR-016` у `DECISIONS.md`, оновлення `CURRENT_STATE.md`, `TASK_BOARD.md`, `SESSION_LOG.md`, розробка `PHASE_5_EXECUTION_PLAN.md`.
- **TASK-060:** Контроль архітектурних меж між `widget/`, `ui/` та `data/local/`, координація збірок, перевірка на фізичному пристрої та GitHub Actions CI.

### 3.2. Agent 1: Product & UX Designer (`product-ux`)
- **TASK-061:** UX-специфікація станів домашніх віджетів **2×2 (Compact)** та **4×2 (Expanded)**:
  - *Active State:* актуальна погода, місто, діапазон температур, час оновлення (та 4-денний прогноз у 4×2).
  - *Stale / Offline State:* збережений прогноз із чесною позначкою застарілості, якщо дані старші за 60 хвилин або позначені `isStale`.
  - *Empty / No Data State:* охайний картковий стан без вигаданих цифр («Немає даних • Натисніть для оновлення»), який відкриває застосунок за тапом.
- **TASK-062:** Специфікація та перевірка критеріїв **Reduced Motion** і доступності (`contentDescription` на іконках віджетів та елементах анімації).

### 3.3. Agent 2: Android UI Engineer (`android-ui`)
- **TASK-063:** Удосконалення анімацій у застосунку згідно з дизайн-системою:
  - Плавна інтерполяція кольорів атмосфери неба в `WeatherMainScreen.kt` (`animateColorAsState` із тривалістю 600 мс у звичайному режимі та `snap()` при `reducedMotion = true`).
  - Підтримка `reducedMotion` у `Crossfade` при перемиканні між вкладками «Зараз», «Прогноз», «Місця» (`tween(if (reducedMotion) 0 else 220)`).
- **TASK-064:** Оптимізація Canvas-анімації в `SunriseSunsetCard.kt`:
  - Повне вимкнення створення `rememberInfiniteTransition` при `isReducedMotion == true` для нульового навантаження на GPU/CPU.

### 3.4. Agent 3: Weather & Location Engineer (`weather-location`)
- **TASK-065:** Забезпечення достовірності кешованих даних для віджетів:
  - Метод `WeatherLocalCache.isForecastStale(forecast, now)` для точного визначення застарілості даних (якщо `forecast.isStale == true` або вік `updatedAt` перевищує 60 хвилин).
  - Гарантія того, що жоден виклик віджета не звертається до `MockWeatherRepository` і не ініціює фонових запитів геолокації.

### 3.5. Agent 4: Widget & Monetization Engineer (`widget-monetization`)
- **TASK-066:** Вдосконалення `WeatherGlanceWidget.kt` (розміри 2×2 та 4×2):
  - Видалення імпорту й виклику `MockWeatherRepository.createKyivForecast()`.
  - Реалізація `EmptyWidgetContent()` для випадку `forecast == null`.
  - Чесне відображення nullable-показників: відсутність підстановки `?: 0` для `minToday`/`maxToday`, приховування відсутніх метрик вологості/вітру/«відчувається як», відображення позначки офлайн/застарілості для старого кешу.
  - Підтримка конфігурації розмірів `2×2` (`COMPACT_SIZE`) та `4×2` (`EXPANDED_SIZE`) у `SizeMode.Responsive`, а також коректних метаданих у `res/xml/weather_widget_info.xml`.
- **TASK-067:** Енергоощадний `WeatherWidgetScheduler.kt`:
  - Перехід на `AlarmManager.ELAPSED_REALTIME` (non-wakeup) замість `ELAPSED_REALTIME_WAKEUP`.
  - Безпечна обробка помилок та чітка документація системних обмежень Android (Doze Mode / App Standby) без гарантії точного 10-хвилинного інтервалу.
  - Сувора відсутність будь-якої реклами у віджетах.

### 3.6. Agent 5: QA & Privacy Specialist (`qa-privacy`)
- **TASK-068:** Розширення автоматизованих тестів:
  - Unit-тести для станів віджета (`WeatherGlanceWidgetTest`), планувальника (`WeatherWidgetSchedulerTest`), перевірки застарілості кешу та логіки `Reduced Motion`.
  - Інструментальний тест на Android для перевірки роботи кешу віджета та конфігурації `AppWidgetManager`.
- **TASK-069:** Перевірка на фізичному пристрої **Samsung Galaxy S24 Ultra (`RZCX326349A`)**:
  - Встановлення оновленої збірки через `adb -s RZCX326349A install -r`.
  - Перевірка реєстрації провайдера віджета в системі Android (`dumpsys appwidget`), тестування режимів 2×2 і 4×2 та плавності анімацій.
- **TASK-070:** Незалежний фінальний аудит Phase 5:
  - Перевірка відсутності `ACCESS_BACKGROUND_LOCATION`, відсутності реклами у віджетах, відсутності `MockWeatherRepository` у виробничому коді віджета.
  - Формування `docs/qa/PHASE_5_QA_REPORT.md` та `docs/reports/PHASE_GATE_5.md`.

---

## 4. Критерії приймання Phase 5 (Definition of Done)

1. [x] Рішення Власника зафіксовано як `ADR-016` у `DECISIONS.md`; оновлено `TASK_BOARD.md`, `SESSION_LOG.md`, `CURRENT_STATE.md`.
2. [x] Підготовлено детальний план `PHASE_5_EXECUTION_PLAN.md` для 6 агентів.
3. [ ] З `WeatherGlanceWidget.kt` повністю вилучено `MockWeatherRepository` та фіктивні нулі (`?: 0`); додано чесний стан `EmptyWidgetContent` та індикацію застарілого офлайн-кешу.
4. [ ] Віджети `2×2` та `4×2` працюють із реальними погодними даними та підтримують локалізацію у всіх 8 мовах.
5. [ ] `WeatherWidgetScheduler.kt` використовує енергоощадний `AlarmManager.ELAPSED_REALTIME` (non-wakeup) відповідно до обмежень ОС Android без обіцянки гарантованого 10-хвилинного інтервалу в Doze Mode.
6. [ ] У віджетах 0% реклами та 0 фонового GPS.
7. [ ] Анімації небесних градієнтів, перемикання вкладок та Canvas сонця підтримують системний режим `Reduced Motion`.
8. [ ] Усі unit-тести (Debug та Release), інструментальні тести Android та суворий Android Lint (`abortOnError = true`) проходять зі статусом 100% PASS (0 errors).
9. [ ] Роботу застосунку та віджетів перевірено на фізичному смартфоні **Samsung Galaxy S24 Ultra (`RZCX326349A`)**.
10. [ ] Отримано незалежний висновок `PHASE_5_QA_REPORT.md` від Agent 5 (`QA_VERIFIED` ✅), підготовлено `PHASE_GATE_5.md` та підтверджено зелений GitHub Actions CI.
11. [ ] Роботи зупинено перед Phase 6 до окремого рішення Власника (`Owner Approved`).
