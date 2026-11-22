# Звіт юридичного аудиту ліцензій та атрибуції постачальників погодних даних (TASK B)
## Data Provider Licensing & Attribution Audit (Zero Budget Policy)

> **Автор:** Agent 3 (Data & API Integration Engineer)  
> **Дата:** 2026-11-22  
> **Статус:** ЗАВЕРШЕНО ТА ПЕРЕДАНО НА АУДИТ AGENT 5  

---

### 1. Мета та контекст аудиту

Відповідно до вимог Власника (Phase 1 Final Design Gate) та жорсткого архітектурного обмеження **ZERO BUDGET POLICY ($0.00 витрат, ADR-009)** з моделлю монетизації **Google AdMob**, проведено юридично-технічний аудит умов надання послуг (Terms of Service) та ліцензійних вимог активних та резервних постачальників метеорологічних даних:
1. **WeatherMetro Free API** (основний комерційний кандидат).
2. **Open-Meteo Free API** (поточний некомерційний адаптер).
3. **Visual Crossing Weather API** (резервний варіант).

---

### 2. Детальний аналіз постачальників

#### 2.1. WeatherMetro Free API (HatchSquare Technologies LLC)
- **Умови тарифу Free:**
  - Квота: **100 000 запитів на місяць**.
  - Глибина прогнозу: **16 днів** (384 години погодинного розрізу).
  - Комерційне використання: **ДОЗВОЛЕНО** на безкоштовному тарифі за умови обов'язкового розміщення видимої атрибуції.
- **Точні правила атрибуції:**
  - Офіційні Terms of Service WeatherMetro (Section: Attribution Requirement):
    > *"Free plan users must display a visible attribution stating 'Weather data by WeatherMetro' with a direct hyperlink back to https://weathermetro.com."*
- **Юридична класифікація ліцензії:**
  - API регулюється **пропрієтарною угодою користувача (SaaS Terms of Service)** компанії HatchSquare Technologies LLC.
  - ⚠️ **Критичне уточнення:** До сервісу WeatherMetro **НЕ застосовується ліцензія Creative Commons Attribution 4.0 (CC BY 4.0)**. Попереднє позначення CC BY 4.0 було технічно некоректним змішуванням ліцензії Open-Meteo з умовами WeatherMetro.
  - Upstream-джерела моделей (ECMWF, NOAA, DWD) відкриті, але пряма вимога ліцензіара WeatherMetro вимагає саме посилання на їхній сервіс.
- **Обов'язковий рядок атрибуції в інтерфейсі:**
  - Українською: **`«Погодні дані від WeatherMetro»`** (активне клікабельне посилання на `https://weathermetro.com`).
  - Англійською: **`"Weather data by WeatherMetro"`**.

---

#### 2.2. Open-Meteo Free API (Open-Meteo GmbH / Jochen Kerkow)
- **Умови тарифу Free:**
  - Квота: 10 000 запитів на добу (5 000/год, 600/хв).
  - Умови використання (Terms of Use): **ВИКЛЮЧНО НЕКОМЕРЦІЙНЕ ВИКОРИСТАННЯ (Non-Commercial Use Only)**.
- **Чи можна використовувати Open-Meteo Free у комерційному релізі з AdMob?**
  - **ВІДПОВІДЬ: КАТЕГОРИЧНО НІ.**
  - Офіційні Terms of Service Open-Meteo прямо забороняють використання безкоштовного API в застосунках із рекламою (Google AdMob), платних підписках або будь-яких комерційних сценаріях:
    > *"Free for non-commercial use only (e.g. personal projects, non-profit websites without ads/subscriptions). Any app featuring advertisements, paid subscriptions, or corporate use requires a commercial paid subscription starting at 19€–29€/month."*
  - **Наслідок для проєкту Weather:**
    - Оскільки проєкт має обмеження **$0.00 бюджету** і використовує Google AdMob для монетизації, платну підписку Open-Meteo ($29+/міс) підключати **заборонено**.
    - Відтак **Open-Meteo Free НЕ МОЖЕ бути джерелом погодних даних у релізній збірці з увімкненою рекламою**.
    - Open-Meteo Free дозволено використовувати **лише** для внутрішніх тестів, етапу розробки без показу бойової реклами або як локальний дебаг-стенд.
- **Ліцензія та атрибуція Open-Meteo (лише для некомерційного режиму):**
  - Дані Open-Meteo ліцензовані під **Creative Commons Attribution 4.0 International (CC BY 4.0)**.
  - Рядок атрибуції: **`«Погодні дані: Open-Meteo.com (CC BY 4.0)»`** (посилання на `https://open-meteo.com`).

---

#### 2.3. Visual Crossing Weather API (Резервний варіант)
- **Умови тарифу Free:**
  - Квота: 1 000 запитів на добу (при правильному групуванні 15 днів/погодинно = 1 запит).
  - Комерційне використання: Дозволено в межах безкоштовного ліміту з обов'язковою атрибуцією `"Weather data by Visual Crossing"`.

---

### 3. Затверджені матричні правила динамічної атрибуції

У коді застосунку реалізовано компонент `DynamicWeatherAttribution`, який динамічно відображає рядок відповідно до фактичного джерела:

| Активний провайдер (`WeatherDataSource`) | Комерційний реліз з AdMob? | Точний текст атрибуції | Клікабельне посилання | Позначка CC BY 4.0 |
|---|:---:|---|---|:---:|
| **`WEATHER_METRO` (Основний)** | **ТАК (Дозволено)** | **«Погодні дані від WeatherMetro»** | `https://weathermetro.com` | **НІ** (Terms of Service) |
| **`OPEN_METEO` (Резервний)** | **НІ (Лише Non-commercial)** | **«Погодні дані: Open-Meteo.com (CC BY 4.0)»** | `https://open-meteo.com` | **ТАК** (CC BY 4.0) |
| **`VISUAL_CROSSING` (Резервний)** | **ТАК (Обмежено 1к/добу)** | **«Погодні дані від Visual Crossing»** | `https://visualcrossing.com` | **НІ** (Terms of Service) |
| **`MOCK` (Офлайн-тести)** | **ТАК (Внутрішній тест)** | **«Автономний демонстраційний режим»** | — | **НІ** |

---

### 4. Висновок для Phase 1 Design Gate

1. В усіх нових графічних макетах інтерфейсу встановлено коректну атрибуцію для активного релізного провайдера: **`«Погодні дані від WeatherMetro»`**.
2. Позначку **CC BY 4.0 знято з WeatherMetro**, оскільки це некоректна юридична інтерпретація. Вона закріплена виключно за Open-Meteo у некомерційному контексті.
3. Документацію оновлено. Звіт передано Agent 5 для незалежної верифікації.
