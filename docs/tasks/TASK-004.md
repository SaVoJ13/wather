# [TASK-004] Інтеграція Open-Meteo API-адаптера, WMO-мапінг та Mock-провайдер

- **Фаза:** 2 (Технічний фундамент і погодні дані)
- **Відповідальний:** weather-location
- **Статус:** `TODO`
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/data/remote/OpenMeteoApiClient.kt`
  - `app/src/main/java/com/weather/app/data/remote/OpenMeteoDto.kt`
  - `app/src/main/java/com/weather/app/data/mapper/WeatherMapper.kt`
  - `app/src/main/java/com/weather/app/data/repository/WeatherRepositoryImpl.kt`
  - `app/src/main/java/com/weather/app/data/mock/MockWeatherRepository.kt`
  - `app/src/test/java/com/weather/app/data/WeatherMapperTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. Створено типізовані DTO для відповідей Open-Meteo Forecast API (температура, опади, WMO коди, вітер, тиск, UV, погодинний та 14-денний масиви).
2. Реалізовано чистий мапінг WMO Weather Interpretation Codes (0-99) у строгий `WeatherCondition` enum.
3. Реалізовано `MockWeatherRepository` з повноцінними тестовими фікстурами (Київ, Варшава) для автономної розробки UI без інтернету.
4. Реалізовано `WeatherRepositoryImpl` із кешуванням та коректною обробкою часових поясів локації.
5. Написано unit-тести для мапера погодних кодів і парсингу часу (100% покриття критичних шляхів).
6. Забезпечено обов'язкове дотримання атрибуції CC BY 4.0 у коментарях та конфігураціях джерела.

---

## Докази перевірки

- Буде зафіксовано вивід тестів `WeatherMapperTest`.

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Сформовано завдання з чіткими межами та контрактами.
- Стан: Очікує початку Фази 2.
- Наступний крок: Передати в роботу агенту `weather-location`.
