# План виконання Phase 2 / PHASE_2_EXECUTION_PLAN
# Architecture & Foundation | ROADMAP 2.0

> **Проєкт:** WEATHER (Weather — Smart Forecast)  
> **Репозиторій:** https://github.com/SaVoJ13/wather  
> **Роль:** Agent 0 (Lead Architect / Technical Project Manager)  
> **Дата складання:** 2026-11-22  
> **Статус:** Підготовлено для затвердження Власником (`Ready for Owner Approval`)  
> **Нормативна основа:** `docs/governance/CONSTITUTION.uk.md` (п. 1, 2.1, 3.2, Червоні лінії №4, №9), `docs/governance/DECISIONS.md`, `MASTER PROMPT — WEATHER PHASE 2`.

---

## 1. Контекст та перевірка воріт фази (Phase Gate Check)

Відповідно до інструкції **MASTER PROMPT — WEATHER PHASE 2 (Розділ 0 та Розділ 17)**:
> *«Цей промпт є обмеженим дозволом на виконання Phase 2, якщо Phase 1 має статус Owner Approved. Якщо такого рішення ще немає в журналі DECISIONS.md, підготуй Phase 2 Execution Plan і зупини реалізацію до формального підтвердження Власником.»*  
> *«КОМАНДА СТАРТУ: Почни з перевірки Phase 1 Owner Approved. Якщо формального погодження Phase 1 немає — нічого не реалізовуй. Підготуй план виконання та питання Власнику.»*

### Результати перевірки:
1. **Стан Phase 1:** Усі роботи над Phase 1 (UX/UI Design) повністю виконані.
   - Створено та верифіковано 8 фізичних графічних PNG-макетів у `docs/design/previews/`.
   - Проведено правовий аудит ліцензій у `docs/governance/DATA_PROVIDER_LICENSING_AUDIT.md`.
   - Незалежний аудитор **Agent 5 (qa-privacy)** підтвердив повну відповідність критеріям у звіті `docs/qa/PHASE_1_FINAL_DESIGN_GATE_QA.md` зі статусом **QA VERIFIED ✅**.
   - Усі 62 unit-тести пройшли успішно (100% PASS).
2. **Стан журналу `DECISIONS.md`:**
   - Останній чинний запис — **[ADR-010]** (від 2026-11-22), яким Власник затвердив закриття Phase 0 зі статусом `Owner Approved` та дозволив старт робіт Phase 1.
   - Пункт 4 [ADR-010] прямо встановлює: *«Не переходити до Phase 2 без окремого погодження Власника (Owner Approved)»*.
   - Формального запису ADR-011 щодо схвалення Phase 1 (`Owner Approved`) у журналі `DECISIONS.md` наразі ще немає.
3. **Дія згідно з Конституцією v2.0:**
   - Відповідно до п. 3.2 Конституції («Лише Власник: перехід між етапами (Owner Approved)») та Червоної лінії №4 («дії з 3.2 без Власника»), команда **не розпочинає самочинну модифікацію функціонального коду Phase 2**, а надає Власнику вичерпний **Phase 2 Execution Plan** та очікує формального погодження.

---

## 2. Попередній технічний аудит (Baseline Audit Summary)

Перед складанням плану Agent 0 та Agent 5 провели неруйнівний аудит репозиторію та середовища:

| Параметр | Фактичний стан у проєкті | Примітки |
|---|---|---|
| **Репозиторій Git** | `https://github.com/SaVoJ13/wather.git`, гілка `main` | Останній коміт: `1f345f862b40958481f4019bfd42fd4dd65a3d3a` (clean tree) |
| **Kotlin версія** | `2.0.21` (з Compose комcompiler plugin) | Сучасний стабільний реліз |
| **Android Gradle Plugin** | `8.7.2` | Gradle 8.11.1 / 9.6.0 |
| **Android SDK** | `minSdk = 26`, `compileSdk = 35`, `targetSdk = 35` | Android 15 (повна сумісність із Google Play 2026) |
| **Compose версія** | Compose BOM `2024.11.00`, Material 3 | Декларативний UI + Material You |
| **AppWidget** | Jetpack Glance `1.1.1` (`glance-appwidget`, `glance-material3`) | Нативні RemoteViews, енергоощадність |
| **Поточні Unit-тести** | **62 / 62 PASS (100%)** | 13 тестових класів (Domain, Data, UI, Widget) |
| **Дозволи Android** | `INTERNET`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `RECEIVE_BOOT_COMPLETED` | **Суворо відсутній** `ACCESS_BACKGROUND_LOCATION` |
| **Фінанси та бюджет** | **$0.00** (Zero Budget Policy) | Відсутні платні сервери, платні SDK та платні API |
| **Поточний кеш** | `WeatherLocalCache.kt` (SharedPreferences + JSON) | Працездатний для поточного обсягу даних |
| **Провайдери погоди** | `WeatherMetro Free` (основний комерційний кандидат), `Open-Meteo` (dev/test), `Mock` | Повна ізоляція через `WeatherRepository` |

---

## 3. Ключові архітектурні рішення Phase 2 (Architecture Decisions)

В рамках Phase 2 заплановано формування наступних ADR:

1. **Локальне збереження (Persistence Strategy):**
   - *Аналіз:* Наразі `WeatherLocalCache` використовує `SharedPreferences` із серіалізацією в `org.json`. Це рішення надійне, не має накладних витрат, повністю покриває збереження поточної погоди, 24-годинної стрічки, 14 днів прогнозу та обраних міст.
   - *Рішення:* Для MVP зберегти та оптимізувати легковаговий підхід або впровадити `AndroidX DataStore Preferences` / типізований кеш без надлишкової важкої бази Room, якщо реляційна структура не є критичною. Це захистить додаток від збільшення розміру APK та складних міграцій SQL.
2. **Ізоляція погодного провайдера (API Provider Abstraction):**
   - Чітке розмежування DTO мережевих клієнтів від доменних моделей `FullWeatherForecast`.
   - Забезпечення можливості перемикання між `WeatherMetro Free` та резервним `Visual Crossing Free` виключно на рівні конфігурації репозиторію без жодних змін у Presentation Layer.
   - Сувора заборона автоматичного перемикання на `Open-Meteo Free` у релізних збірках з AdMob.
3. **Захист квот та мережева стійкість (Quota & Resilience Policy):**
   - In-memory дебаунс одночасних запитів (об'єднання паралельних викликів для однієї локації).
   - Мінімальний інтервал між примусовими мережевими оновленнями (TTL кешу не менше 15-30 хвилин).
   - Експоненційний відкат (exponential backoff) при отриманні HTTP 429 (Too Many Requests) або HTTP 5xx.
   - Показ чіткого статусу «Офлайн / Застарілі дані» із зазначенням часу замість аварійного падіння.
4. **Навігаційний каркас та архітектура UI-станів:**
   - Декомпозиція екранів під тривкладкову модель Material 3 (`NowScreen`, `ForecastScreen`, `LocationsScreen`) та окремий `SettingsScreen`.
   - UDF (Unidirectional Data Flow): ViewModel експонує незмінний `StateFlow<WeatherUiState>`.

---

## 4. План розподілу завдань між 6 агентами команди

### 👑 Agent 0: Lead Architect / Technical Project Manager
- **A0.1 — Baseline Audit:** Складання офіційного звіту `docs/architecture/PHASE_2_BASELINE_AUDIT.md` із повною інвентаризацією кодової бази, модулів, залежностей та техборгу.
- **A0.2 — Architecture Decisions:** Підготовка ADR щодо локального збереження, ізоляції API, обробки помилок та ліцензійного контролю.
- **A0.3 — CI & Automation:** Перевірка скриптів збірки, тестів та валідації відсутності витоків ключів.
- **A0.4 — Integration Management:** Координація злиття змін агентів без конфліктів редагування однакових файлів.

### 🎨 Agent 1: Product & UX Designer
- **A1.1 — UX Contract Review:** Перевірка відповідності кодових структур затвердженим специфікаціям `USER_FLOWS.md` та `DESIGN_SYSTEM.md`.
- **A1.2 — UI State Matrix:** Складання вичерпної матриці 11 UI-станів (Loading, Success, Refreshing, Empty, Network Error, Offline Cache, Stale Cache, Quota 429, GPS Denied, Approximate Location, Missing Data) для всіх вкладок.
- **A1.3 — Accessibility Specifications:** Вимоги доступності TalkBack, масштабування тексту до 200%, тач-таргети ≥ 48dp, Reduced Motion.
- **Вихідний артефакт:** `docs/architecture/UI_STATE_CONTRACT.md`.

### 📱 Agent 2: Android UI Engineer
- **A2.1 — Navigation Foundation:** Створення чистого каркаса навігації між вкладками («Зараз», «Прогноз», «Місця») та екраном налаштувань (`SettingsScreen`) без передчасного хардкоду складної анімації Phase 4.
- **A2.2 — State Integration:** Підключення навігації та компонентів до типізованих UI-State через StateFlow ViewModel.
- **A2.3 — Design Consistency:** Забезпечення коректності використання токенів кольорів, динамічних палітр Material You та світлої/темної тем.
- **Вихідний артефакт:** Скомпільований Presentation Foundation.

### ⚡ Agent 3: Weather & Data Engineer (Критична роль Phase 2)
- **A3.1 — Repository Audit:** Аналіз наявних класів `WeatherRepositoryImpl`, `MockWeatherRepository`, маперів та локального кешу.
- **A3.2 — Domain Contracts:** Затвердження строго типізованих інтерфейсів та чистих доменних моделей без залежностей від Android SDK.
- **A3.3 — Local Persistence:** Оптимізація збереження прогнозу для кількох міст, часу оновлення, індикатора `isStale` та відновлення після збоїв.
- **A3.4 — API Isolation:** Створення інтерфейсу абстракції погодних постачальників із конфігураційним підключенням `WeatherMetro Free`.
- **A3.5 — Quota Safety:** Реалізація захисту від вичерпання безкоштовної квоти (100k запитів/міс) та бекоффу для HTTP 429/5xx.
- **Вихідні артефакти:** `docs/architecture/DATA_CONTRACTS.md`, `docs/architecture/CACHE_STRATEGY.md`, модульні тести.

### 🧩 Agent 4: Widget & Monetization Engineer
- **A4.1 — Widget Architecture:** Перевірка контрактів Jetpack Glance для компактного (2x2) та розширеного (4x2) режимів з читанням даних виключно з локального кешу.
- **A4.2 — Widget Update Policy:** Політика енергоощадного оновлення через AlarmManager (інтервал ≥ 10-15 хв) без фонового GPS та без частих wake-locks.
- **A4.3 — AdMob Foundation:** Підготовка контракту контейнера `Anchored Adaptive Banner` із динамічним розрахунком висоти (50-90dp) для гарантії Zero CLS. Суворо 0% реклами у віджетах.
- **Вихідний артефакт:** `docs/architecture/WIDGET_AD_CONTRACTS.md`.

### 🛡️ Agent 5: QA / Security / Release Engineer (Незалежна верифікація)
- **A5.1 — Baseline Verification:** Фіксація базового стану тестів (62/62 PASS) та відсутності дефектів у збірці.
- **A5.2 — Architecture Tests:** Перевірка чистоти архітектури (відсутність залежностей Domain від Android, ізоляція DTO від UI).
- **A5.3 — Error Handling & Edge Cases:** Тестування поведінки при відсутності мережі, HTTP 401, 429, 500, таймаутах, некоректному JSON та перетині півночі/зміні часових поясів.
- **A5.4 — Security & Privacy Audit:** Перевірка відсутності API-ключів у Git, заборона `ACCESS_BACKGROUND_LOCATION`, контроль ліцензій бібліотек у `THIRD_PARTY.md`.
- **A5.5 — Independent Verdict:** Складання підсумкового звіту `docs/qa/PHASE_2_QA_REPORT.md` (PASS / FAIL / BLOCKED / NOT VERIFIED).

---

## 5. Графік та послідовність виконання робіт

```
[ Gate 1: Схвалення Phase 1 Власником (Owner Approved) ]
                         │
                         ▼
             ┌───────────────────────┐
             │   P2.0 — Baseline     │  (Agent 0 + Agent 5: Baseline Audit & Tests)
             └───────────┬───────────┘
                         │
                         ▼
             ┌───────────────────────┐
             │ P2.1 — Arch Contracts │  (Agent 0 + Agent 3: Data/Domain контракти, ADR)
             └───────────┬───────────┘
                         │
       ┌─────────────────┴─────────────────┐
       ▼                                   ▼
┌──────────────────────┐        ┌──────────────────────┐
│P2.2 Local Persistence│        │P2.3 UI Foundation    │  (Паралельні роботи в ізольованих файлах)
│(Agent 3: Кеш & Тести)│        │(Agent 1 + Agent 2:   │
└──────────────┬───────┘        │Навігація & UI-стани) │
               │                └──────────┬───────────┘
               └─────────────────┬─────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │ P2.4 Widget & Ads     │  (Agent 4: Glance інфраструктура & AdMob контр.)
                     └───────────┬───────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │ P2.5 Integration      │  (Agent 0: Загальна інтеграція та збірка)
                     └───────────┬───────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │ P2.6 Independent QA   │  (Agent 5: Повний незалежний аудит і тести)
                     └───────────┬───────────┘
                                 │
                                 ▼
                     ┌───────────────────────┐
                     │ P2.7 Phase Gate 2     │  (Agent 0: Фінальний звіт Власнику PHASE_GATE_2)
                     └───────────────────────┘
```

---

## 6. Критерії готовності до виходу з Phase 2 (Definition of Done Phase 2)

Phase 2 буде вважатися виконаною, якщо:
1. Збережено стабільну компіляцію проєкту (`debug` та `release`).
2. Усі існуючі 62 unit-тести проходять успішно, додано нові тести контрактів та маперів.
3. Domain Layer повністю ізольований від Android Framework.
4. Презентаційний шар не оперує DTO погодних сервісів.
5. Локальний кеш надійно відновлює дані після перезапуску застосунку та імітує офлайн-роботу.
6. Провайдер `WeatherMetro Free` ізольований контрактом, закладено захист від переліміту (HTTP 429).
7. Заборонено підключення некомерційного `Open-Meteo` у релізних конфігураціях з рекламою.
8. Відсутні будь-які витоки API-ключів, персональних координат чи секретів у коді чи логах.
9. Віджет не використовує заборонений фоновий GPS і живиться виключно з дискового кешу.
10. Незалежний аудитор **Agent 5** надав звіт `docs/qa/PHASE_2_QA_REPORT.md` зі статусом **QA VERIFIED**.
11. Дотримано **ZERO BUDGET POLICY ($0.00 витрат)**.
