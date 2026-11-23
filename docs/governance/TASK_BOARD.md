# Дошка завдань проєкту / TASK_BOARD

> Відповідно до розділу 5.1 Конституції v2.0:
> Відображає актуальний статус завдань, виконавців та прив'язку до фаз дорожньої карти.
> Оновлюється Агентом 0 під час переходу завдань між статусами.

---

## Статуси завдань

| ID | Назва завдання | Фаза | Виконавець | Статус | QA Verified |
|---|---|---|---|---|---|
| **TASK-001** | Ініціалізація governance-пам'яті, інвентаризація та технічні ADR | Фаза 0 | Agent 0 | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-002** | Архітектурні контракти та моделі між UI, Domain та Data | Фаза 0/1 | Agent 0 | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-003** | UX-специфікація інформаційної архітектури, станів та reduced-motion | Фаза 1 | product-ux | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-004** | Інтеграція Open-Meteo API-адаптера, WMO-мапінг та Mock-провайдер | Фаза 2 | weather-location | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-005** | Пошук міст через геокодування та безпечне визначення локації | Фаза 2/3 | weather-location | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-006** | Скелет Android Compose проєкту, дизайн-система та головний екран | Фаза 3 | android-ui | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-007** | Домашній погодний AppWidget на базі Jetpack Glance | Фаза 4 | android-ui | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-008** | Комплексний аудит приватності, дозволів та незалежна верифікація | Фаза 5 | qa-privacy | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-009** | Віджет: автооновлення кожні 10 хвилин та збереження локації користувача | Фаза 4/5 | android-ui / weather-location | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-010** | Редизайн домашнього віджета за зразком користувача | Фаза 4 | android-ui | QA_VERIFIED | ✅ 2026-11-17 |
| **TASK-011** | Анімований схід/захід сонця, погодний радар, вилучення GPS-снікбару | Фаза 3/4 | android-ui | QA_VERIFIED | ✅ 2026-11-18 |
| **TASK-012** | Погодинний прогноз від поточної години та збереження вибраних міст | Фаза 3/4 | android-ui / weather-location | QA_VERIFIED | ✅ 2026-11-20 |
| **TASK-013** | Преміум-редизайн домашнього віджета (Glassmorphism & Balanced Layout) | Фаза 4 | android-ui | QA_VERIFIED | ✅ 2026-11-20 |
| **TASK-014** | Підготовка продукту до закритого тестування в Google Play Console | Фаза 5 | Agent 0 / qa-privacy | QA_VERIFIED | ✅ 2026-11-20 |
| **TASK-015** | Повноцінна підтримка мультимовності (pl, de, ru, en, es, it, uk) | Фаза 5 | android-ui / qa-privacy | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-016** | UX-архітектура, 3 розділи навігації та детальні сценарії (USER_FLOWS.md) | Phase 1 | product-ux | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-017** | Преміум дизайн-система, погодні фони та специфікація UI (DESIGN_SYSTEM & UI_SPEC) | Phase 1 | android-ui | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-018** | Незалежний аудит доступності (WCAG AA), логіки станів та вимог Phase 1 | Phase 1 | qa-privacy | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-019** | Baseline Audit, інвентаризація кодової бази та початкова верифікація | Phase 2 | Agent 0 / qa-privacy | DONE | ✅ 2026-11-22 |
| **TASK-020** | Архітектурні контракти даних, ADR та стратегія кешування (DATA_CONTRACTS & CACHE_STRATEGY) | Phase 2 | Agent 0 / weather-location | DONE | ✅ 2026-11-22 |
| **TASK-021** | Локальне збереження, ізоляція API-провайдерів та захист квот WeatherMetro/Open-Meteo | Phase 2 | weather-location | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-022** | Каркас навігації (3 вкладки + Settings), матриця 11 UI-станів та доступність | Phase 2 | product-ux / android-ui | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-023** | Контракти віджетів Glance (2x2, 4x2) та AdMob Anchored Adaptive Banner Zero CLS | Phase 2 | widget-monetization | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-024** | Комплексний незалежний аудит Phase 2, архітектурні тести та безпека (PHASE_2_QA_REPORT) | Phase 2 | qa-privacy | QA_VERIFIED | ✅ 2026-11-22 |
| **TASK-025** | Затвердження плану Phase 3, фіксація ADR-014 та оновлення governance-пам'яті | Phase 3 | Agent 0 | DONE | ✅ 2026-11-23 |
| **TASK-026** | Безпека конфігурації та читання ключів API (BuildConfig & local.properties) | Phase 3 | Agent 0 | IN_PROGRESS | - |
| **TASK-027** | Координація інтеграції, контроль сумісності Gradle 8.11.1 та CI | Phase 3 | Agent 0 | IN_PROGRESS | - |
| **TASK-028** | Підготовка фінального звіту етапних воріт PHASE_GATE_3 | Phase 3 | Agent 0 | TODO | - |
| **TASK-029** | UX-специфікація динамічних станів оновлення, Pull-to-Refresh та збоїв мережі | Phase 3 | product-ux | IN_PROGRESS | - |
| **TASK-030** | UX контекстного дозволу GPS та обробки відмови користувача | Phase 3 | product-ux | IN_PROGRESS | - |
| **TASK-031** | Стандартизація розміщення та контрастності правової атрибуції WeatherMetro | Phase 3 | product-ux | IN_PROGRESS | - |
| **TASK-032** | Інтеграція реактивних UI-станів та Pull-to-Refresh на головному екрані | Phase 3 | android-ui | IN_PROGRESS | - |
| **TASK-033** | Контекстний запит дозволу GPS у Jetpack Compose за явною дією користувача | Phase 3 | android-ui | IN_PROGRESS | - |
| **TASK-034** | Повнофункціональний екран міст та BottomSheet пошуку з дебаунсом | Phase 3 | android-ui | IN_PROGRESS | - |
| **TASK-035** | Мережевий клієнт та DTO для WeatherMetro Free API (16 днів, 384 години) | Phase 3 | weather-location | IN_PROGRESS | - |
| **TASK-036** | Реалізація WeatherMetroRemoteDataSource та інтеграція репозиторію зі Stale Cache | Phase 3 | weather-location | IN_PROGRESS | - |
| **TASK-037** | Надійний клієнт геолокації LocationClient (Foreground Only, 0 Background GPS) | Phase 3 | weather-location | IN_PROGRESS | - |
| **TASK-038** | Зворотне геокодування та мультимістове дискове збереження у WeatherLocalCache | Phase 3 | weather-location | IN_PROGRESS | - |
| **TASK-039** | Інтеграція Glance AppWidget із бойовим кешем локації (нуль прямих HTTP-запитів) | Phase 3 | widget-monetization | IN_PROGRESS | - |
| **TASK-040** | Фоновий планувальник оновлення віджета (AlarmManager 10 хв, енергоощадність) | Phase 3 | widget-monetization | IN_PROGRESS | - |
| **TASK-041** | Валідація тестового AdMob Anchored Adaptive Banner (Zero CLS, 0% реклами у віджетах) | Phase 3 | widget-monetization | IN_PROGRESS | - |
| **TASK-042** | Модульне та інтеграційне тестування Phase 3 (≥ 80 тестів 100% PASS) | Phase 3 | qa-privacy | IN_PROGRESS | - |
| **TASK-043** | Незалежний аудит приватності, Manifest та безпеки секретів (нуль ключів у git) | Phase 3 | qa-privacy | TODO | - |
| **TASK-044** | Фінальний аудит Phase 3, незалежний звіт PHASE_3_QA_REPORT та статус QA_VERIFIED | Phase 3 | qa-privacy | TODO | - |

---

## Легенда статусів
- TODO — Завдання заплановане, чекає своєї черги або готовності залежностей.
- IN_PROGRESS — Завдання в активній роботі.
- READY_FOR_REVIEW — Роботу автора завершено, очікується незалежна перевірка QA.
- QA_VERIFIED — Незалежний QA перевірив фактичні докази та підтвердив відповідність DoD.
- DONE — Зміни влито в main, документацію та картку оновлено.
- BLOCKED — Робота зупинена через блокер (зафіксовано в CURRENT_STATE.md).

