# [TASK-005] Пошук міст через онлайн-геокодування та безпечне визначення локації

- **Фаза:** 2 / 3
- **Відповідальний:** weather-location
- **Статус:** `IN_PROGRESS` (Реалізовано, очікує перевірки QA)
- **Файли-власники:**
  - `app/src/main/java/com/weather/app/domain/repository/LocationRepository.kt`
  - `app/src/main/java/com/weather/app/data/remote/dto/GeocodingDto.kt`
  - `app/src/main/java/com/weather/app/data/mapper/GeocodingMapper.kt`
  - `app/src/main/java/com/weather/app/data/remote/GeocodingApiClient.kt`
  - `app/src/main/java/com/weather/app/data/location/LocationClient.kt`
  - `app/src/main/java/com/weather/app/data/repository/LocationRepositoryImpl.kt`
  - `app/src/test/java/com/weather/app/data/GeocodingMapperTest.kt`
  - `app/src/test/java/com/weather/app/data/LocationRepositoryTest.kt`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Реалізовано клієнт `GeocodingApiClient` для швидкого пошуку міст за назвою (з підтримкою мок-бази українських та європейських міст для роботи офлайн без інтернету).
2. [x] Пошук міст працює абсолютно автономно і не вимагає жодних дозволів на геолокацію (перевірено unit-тестом `searchCities_returnsMatchingMockCities_withoutGps`).
3. [x] Доступ до геолокації спроєктовано через foreground контракт `LocationClient` з обробкою `PermissionDeniedException` та `LocationDisabledException`.
4. [x] Жодного використання `ACCESS_BACKGROUND_LOCATION` — фонова локація не підтримується та суворо заборонена.
5. [x] Коректна обробка відмови користувача: повертається керований `Result.failure(PermissionDeniedException)` без падінь чи блокування UI.
6. [x] Жодних записів точних GPS-координат користувача у файли журналу чи логи.

---

## Докази перевірки

```
JUnit version 4.13.2
........................
Time: 0,314

OK (24 tests)
```

Успішно пройдено всі 24 unit-тести:
- `GeocodingMapperTest` (4 тести): мапінг полів, фолбек відсутньої країни на country_code, обробка null/empty відповідей, мапінг списку міст.
- `LocationRepositoryTest` (4 тести): автономний пошук міст без GPS, обробка порожнього запиту, додавання/видалення/дедуплікація обраних міст, коректне повернення `PermissionDeniedException` при відхиленні дозволу.
- `WeatherCodeMapperTest`, `MockWeatherRepositoryTest`, `WeatherMapperTest` (16 тестів): регресійний прогін погодного модуля.

---

## Журнал виконання (Append-only)

### 2026-11-17 19:26 — Agent 0
- Зроблено: Створено картку завдання з безпеки геолокації.
- Стан: Очікує виконання.
- Наступний крок: Реалізація геокодування та foreground location client.

### 2026-11-17 21:22 — weather-location
- Зроблено:
  1. Оголошено доменний інтерфейс `domain/repository/LocationRepository.kt` згідно з `CONTRACTS.md`.
  2. Створено типізовані DTO `GeocodingDto.kt` з ліцензією CC BY 4.0.
  3. Реалізовано `GeocodingMapper.kt` для конвертації у `LocationModel`.
  4. Реалізовано `GeocodingApiClient.kt` з автономною базою міст (Київ, Львів, Одеса, Харків, Дніпро, Варшава, Лондон, Берлін) для розробки та тестів без інтернету.
  5. Реалізовано безпечний клієнт переднього плану `LocationClient.kt` з винятками `PermissionDeniedException` та `LocationDisabledException`. Жодної фонової локації!
  6. Реалізовано `LocationRepositoryImpl.kt` із підтримкою реактивного списку обраних міст (`Flow<List<LocationModel>>`).
  7. Написано 8 нових unit-тестів (`GeocodingMapperTest`, `LocationRepositoryTest`). Загалом 24 тести успішно виконано через `JUnitCore` (`OK (24 tests)`).
- Стан: Реалізацію завершено. Відповідно до п. 2.1 Конституції статус залишено у `IN_PROGRESS` до перевірки незалежним QA-агентом.
- Наступний крок: Передати на аудит QA-агенту `qa-privacy`.
