# [TASK-005] Пошук міст через онлайн-геокодування та безпечне визначення локації

- **Фаза:** 2 / 3
- **Відповідальний:** weather-location
- **Статус:** `TODO`
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/data/remote/GeocodingApiClient.kt`
  - `app/src/main/java/com/weather/app/data/location/LocationClient.kt`
  - `app/src/main/java/com/weather/app/data/repository/LocationRepositoryImpl.kt`
  - `app/src/test/java/com/weather/app/data/GeocodingParserTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. Реалізовано клієнт Open-Meteo Geocoding API для швидкого пошуку міст за назвою (з підтримкою української та англійської мов).
2. Пошук міст працює абсолютно автономно і не вимагає жодних дозволів на геолокацію.
3. Доступ до геолокації реалізовано виключно через foreground API (`ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`).
4. Жодного використання `ACCESS_BACKGROUND_LOCATION`.
5. Коректна обробка відмови користувача (повернення безпечного результату без аварійного завершення програми).
6. Жодних записів точних GPS-координат користувача у файли журналу чи логи.

---

## Докази перевірки

- Unit-тести парсингу геокодування та перевірка маніфесту на відсутність background location.

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Створено картку завдання з безпеки геолокації.
- Стан: Очікує виконання.
- Наступний крок: Реалізація геокодування та foreground location client.
