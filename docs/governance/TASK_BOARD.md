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

---

## Легенда статусів
- TODO — Завдання заплановане, чекає своєї черги або готовності залежностей.
- IN_PROGRESS — Завдання в активній роботі.
- READY_FOR_REVIEW — Роботу автора завершено, очікується незалежна перевірка QA.
- QA_VERIFIED — Незалежний QA перевірив фактичні докази та підтвердив відповідність DoD.
- DONE — Зміни влито в main, документацію та картку оновлено.
- BLOCKED — Робота зупинена через блокер (зафіксовано в CURRENT_STATE.md).

