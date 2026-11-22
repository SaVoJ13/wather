# Дорожня карта розвитку / ROADMAP 2.0
# Weather — Smart Forecast

> Версія: 2.0 | Дата: 2026-11-22  
> Автори: Agent 0 (Lead Architect)  
> Статус: Затверджено структуру 10 фаз відповідно до MASTER PROMPT

---

## 1. Загальна концепція та фази

Дорожня карта ROADMAP 2.0 структурує розвиток комерційного міжнародного Android-застосунку погоди на 10 послідовних фаз.  
**Правило переходів:** Перехід до наступної фази можливий виключно після схвалення Власником (`Owner Approved`) та незалежного аудиту попередньої фази (`QA_VERIFIED`).

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7 ──► Phase 8 ──► Phase 9
Audit &     UX/UI       Arch &      Weather     Main        Widgets     Optimi-     Monetiza-   Testing &   Post-Launch
Product     Design      Foundation  API & Loc   Interface   & Motion    zation      tion & Priv Google Play Development
```

---

## 2. Деталізація фаз ROADMAP 2.0

### Phase 0: Audit & Product Definition (ПОТОЧНА ФАЗА)
- **Ціль:** Повний аудит репозиторію, усунення суперечностей, формування специфікації продукту (MVP + плани), аналіз ліцензій погодних API, реєстр ризиків, визначення ролей 6 агентів.
- **Артефакти:** `PHASE_0_REPORT.md`, `PRODUCT_SPECIFICATION.md`, `ARCHITECTURE.md`, `API_EVALUATION.md`, `TEST_PLAN.md`, `RISK_REGISTER.md`, оновлений `OWNER_INBOX.md`.
- **Ворота виходу:** `Owner Approved` звіту Phase 0. Жодного функціонального коду під час Phase 0.

### Phase 1: UX/UI Design
- **Відповідальний:** Agent 1 (Product & UX Designer).
- **Ціль:** Затвердження структури екранів, користувацьких сценаріїв (User Flows), колірної системи небосхилу (Day/Night, 7 станів атмосфери), дизайн-системи токенів, типографіки, розмірів віджетів (2x2, 4x2) та вимог доступності WCAG AA (контраст ≥ 4.5:1, тач-таргети ≥ 48dp, семантика TalkBack, reduced motion).
- **Артефакти:** `docs/ux/UX_SPEC.md`, `docs/ux/DESIGN_TOKENS.md`, мокапи станів.
- **Ворота виходу:** `Owner Approved` візуальної концепції.

### Phase 2: Architecture & Foundation
- **Відповідальний:** Agent 0 (Architect) & Agent 3 (Data Engineer).
- **Ціль:** Фіксація Clean Architecture контрактів, Kotlin Multiplatform-ready моделі, налаштування модулів та системи компіляції під Android 15 (`minSdk = 26`, `compileSdk/targetSdk = 35`), локальний кеш, налаштування базової конфігурації.
- **Артефакти:** Базові модулі `domain/model/*`, `domain/repository/*`, `data/local/*`.
- **Ворота виходу:** Успішна збірка, юніт-тести моделей.

### Phase 3: Weather API & Location
- **Відповідальний:** Agent 3 (Weather & Data Engineer).
- **Ціль:** Реалізація HTTP-клієнта, DTO-серіалізації, WMO-мапінгу (коди 0–99), розрахунку сходу й заходу сонця, безпечного визначення геопозиції за запитом (без фонового GPS) та геокодування міст через інтернет.
- **Артефакти:** `OpenMeteoHttpClient.kt`, `LocationClient.kt`, `ReverseGeocoder.kt`, `MockWeatherRepository.kt`.
- **Ворота виходу:** 100% тестів маперів та геолокації зеленими, `QA_VERIFIED`.

### Phase 4: Main Android Interface
- **Відповідальний:** Agent 2 (Android UI Engineer).
- **Ціль:** Створення повноцінного головного екрана на Jetpack Compose: TopBar, Hero з поточною погодою, погодинна стрічка (24 год від поточної години), прогноз на 14 днів, сітка метрик 2x2, анімована картка сонця, погодний радар, модальні вікна пошуку міст та деталей дня.
- **Артефакти:** `WeatherMainScreen.kt`, `ui/components/*.kt`.
- **Ворота виходу:** Інтерфейс функціонує, доступність перевірена, відсутність збоїв на емуляторі та фізичному пристрої.

### Phase 5: Widgets & Animation
- **Відповідальний:** Agent 4 (Widget & Monetization Engineer) & Agent 2.
- **Ціль:** Розробка преміального домашнього віджета Jetpack Glance (Glassmorphism, 2x2 та 4x2 режими, автооновлення раз на 10-15 хв, енергоощадність без постійних wake locks), плавні Compose-анімації у застосунку, системний reduced motion.
- **Артефакти:** `WeatherGlanceWidget.kt`, `WeatherWidgetScheduler.kt`.
- **Ворота виходу:** Віджет працює на робочому столі, коректно відображає дані в офлайні, `QA_VERIFIED`.

### Phase 6: Optimization
- **Відповідальний:** Agent 0 (Architect) & Agent 5 (QA & Security).
- **Ціль:** Оптимізація часу холодного старту (< 1.5 с), споживання оперативної пам'яті (< 80 МБ), розміру коду (R8 / ProGuard shrinkage, видалення невикористаних ресурсів), перевірка відсутності витоків пам'яті (Memory Leaks) та енерговитрат.
- **Артефакти:** `proguard-rules.pro`, оптимізовані збірки.
- **Ворота виходу:** Релізний APK < 10 МБ, нуль зависань (ANR).

### Phase 7: Monetization & Privacy
- **Відповідальний:** Agent 4 (Monetization Engineer) & Agent 5 (Privacy).
- **Ціль:** Безпечна інтеграція рекламного слота AdMob (лише тестові Ad Unit ID під час розробки), фіксована висота банера без зсувів макета (Zero CLS), сувора відсутність реклами у віджетах. Підготовка політики конфіденційності, перевірка відповідності правилам Google Play Data Safety.
- **Артефакти:** `AdBannerPlaceholder.kt`, `docs/release/PRIVACY_POLICY.md`.
- **Ворота виходу:** Схвалення Власником рекламної інтеграції, чистий аудит безпеки.

### Phase 8: Testing & Google Play
- **Відповідальний:** Agent 5 (Release Engineer) & Agent 0.
- **Ціль:** Комплексне релізне тестування (59/59+ юніт-тестів, UI-тести), збірка підписаного релізного бандлу (`app-release.aab`), інструкція з публікації в Google Play Console (Closed Testing), підготовка сторінки магазину.
- **Артефакти:** `app-release.aab`, `docs/release/CLOSED_TESTING_GUIDE.md`.
- **Ворота виходу:** Завантаження AAB Власником у Google Play Console під акаунтом `appvsdeveloper@gmail.com`.

### Phase 9: Post-launch Development
- **Відповідальний:** Мультиагентна команда.
- **Ціль:** Розвиток після першого релізу: персональний планувальник активностей (Activity Planner: спорт, авто, парасолька), сповіщення про екстремальні погодні явища, індекс якості повітря (AQI), перемикач одиниць (°C/°F), підписка без реклами (In-App Purchases).
- **Ворота виходу:** Версія 2.0+ з розширеним функціоналом.
