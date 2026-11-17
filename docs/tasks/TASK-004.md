# [TASK-004] Інтеграція Open-Meteo API-адаптера, WMO-мапінг та Mock-провайдер

- **Фаза:** 2 (Технічний фундамент і погодні дані)
- **Відповідальний:** weather-location
- **Статус:** `QA_VERIFIED`
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/domain/model/WeatherCondition.kt`
  - `app/src/main/java/com/weather/app/domain/model/LocationModel.kt`
  - `app/src/main/java/com/weather/app/domain/model/CurrentWeather.kt`
  - `app/src/main/java/com/weather/app/domain/model/HourlyForecastItem.kt`
  - `app/src/main/java/com/weather/app/domain/model/DailyForecastItem.kt`
  - `app/src/main/java/com/weather/app/domain/model/FullWeatherForecast.kt`
  - `app/src/main/java/com/weather/app/domain/repository/WeatherRepository.kt`
  - `app/src/main/java/com/weather/app/data/remote/dto/OpenMeteoDto.kt`
  - `app/src/main/java/com/weather/app/data/mapper/WeatherCodeMapper.kt`
  - `app/src/main/java/com/weather/app/data/mapper/WeatherMapper.kt`
  - `app/src/main/java/com/weather/app/data/mock/MockWeatherRepository.kt`
  - `app/src/main/java/com/weather/app/data/repository/WeatherRepositoryImpl.kt`
  - `app/src/test/java/com/weather/app/data/WeatherCodeMapperTest.kt`
  - `app/src/test/java/com/weather/app/data/MockWeatherRepositoryTest.kt`
  - `app/src/test/java/com/weather/app/data/WeatherMapperTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Створено типізовані DTO для відповідей Open-Meteo Forecast API (`OpenMeteoForecastResponse`, `OpenMeteoCurrentDto`, `OpenMeteoHourlyDto`, `OpenMeteoDailyDto`).
2. [x] Реалізовано чистий мапінг WMO Weather Interpretation Codes (0-99) у строгий `WeatherCondition` enum з українськими описами.
3. [x] Реалізовано `MockWeatherRepository` з повноцінними тестовими фікстурами (Київ — помірно-сонячно, Варшава — дощ/гроза) для автономної розробки UI без інтернету.
4. [x] Реалізовано `WeatherRepositoryImpl` із кешуванням (in-memory з TTL), підтримкою офлайн-фолбеку та позначкою застарілості `isStale`.
5. [x] Написано 16 unit-тестів (`WeatherCodeMapperTest`, `MockWeatherRepositoryTest`, `WeatherMapperTest`) — 100% тестів успішні (`OK (16 tests)`).
6. [x] Забезпечено обов'язкове дотримання атрибуції CC BY 4.0 у коментарях та конфігураціях джерела згідно з ADR-003/ADR-005.

---

## Докази перевірки

```
JUnit version 4.13.2
................
Time: 0,209

OK (16 tests)
```
Успішно виконано 16 unit-тестів:
- `WeatherCodeMapperTest` (9 тестів): перевірка всіх категорій кодів WMO (ясно, хмарно, туман, мряка, дощ, сильний дощ, сніг, гроза, невідомі/null значення).
- `MockWeatherRepositoryTest` (3 тести): перевірка фікстур Києва (24 години, 14 днів), Варшави (24 години, 14 днів, дощ) та реактивного потоку `observeCurrentForecast`.
- `WeatherMapperTest` (4 тести): перевірка парсингу ISO дат/часу, захист від null/некоректних форматів, мапінг повної відповіді API в `FullWeatherForecast`.

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Сформовано завдання з чіткими межами та контрактами.
- Стан: Очікує початку Фази 2.
- Наступний крок: Передати в роботу агенту `weather-location`.

### 2026-11-17 21:12 — weather-location
- Зроблено:
  1. Створено чисті доменні моделі у `domain/model/` (`WeatherCondition`, `LocationModel`, `CurrentWeather`, `HourlyForecastItem`, `DailyForecastItem`, `FullWeatherForecast`) та інтерфейс `domain/repository/WeatherRepository.kt` згідно з `CONTRACTS.md`.
  2. Реалізовано DTO `OpenMeteoDto.kt` з фіксацією ліцензії CC BY 4.0 та умов некомерційного використання.
  3. Реалізовано `WeatherCodeMapper.kt` із мапінгом усіх діапазонів кодів WMO та локалізованими описами.
  4. Реалізовано `WeatherMapper.kt` із безпечним парсингом ISO-8601 часу та формуванням 24h / 14-денного прогнозу.
  5. Створено `MockWeatherRepository.kt` із автономними фікстурами для Києва та Варшави.
  6. Створено `WeatherRepositoryImpl.kt` із кешуванням та підтримкою `isStale`.
  7. Написано 16 unit-тестів, успішно скомпільовано через `kotlinc` та пройдено через `JUnitCore` (`OK (16 tests)`).
- Стан: Реалізацію завершено. Відповідно до п. 2.1 Конституції, статус `QA_VERIFIED` не встановлюється автором і очікує перевірки незалежним QA-агентом або Власником.
- Наступний крок: Передати на аудит QA-агенту `qa-privacy`.

### 2026-11-17 21:16 — qa-privacy (Незалежна верифікація)
- Зроблено: Проведено незалежний аудит кодової бази pp/src/main/java/com/weather/app/ та запуск unit-тестів:
  1. Чистота домену: моделі в domain/model/ та інтерфейс domain/repository/WeatherRepository.kt повністю ізольовані від API DTO та зовнішніх бібліотек серіалізації.
  2. WMO мапінг: WeatherCodeMapper.kt покриває всі категорії кодів (0-99) із коректним українським перекладом (WeatherCondition.descriptionUk).
  3. Мок-репозиторій: MockWeatherRepository.kt надає повні реалістичні фікстури Києва (сонячно, 24h + 14 днів) та Варшави (дощ, 24h + 14 днів), а також потік observeCurrentForecast.
  4. Ліцензія джерела: у коментарях до OpenMeteoDto.kt чітко зафіксовано статус некомерційного використання CC BY 4.0 та вимогу атрибуції.
  5. Фактичний запуск тестів: самостійно скомпільовано через kotlinc та виконано через JUnitCore 16 тестів (WeatherCodeMapperTest, MockWeatherRepositoryTest, WeatherMapperTest). Результат: OK (16 tests), час 0.229s, 0 помилок/падінь.
- Вердикт: **QA VERIFIED** (Критерії 1-6 підтверджено фактичними доказами).
- Стан: Завдання верифіковано.
