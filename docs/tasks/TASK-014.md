# [TASK-014] Підготовка продукту до закритого тестування в Google Play Console

- **Фаза:** 5 (Підготовка до закритого тестування / Реліз)
- **Відповідальний:** Agent 0 / android-ui / qa-privacy
- **Статус:** `QA_VERIFIED`
- **QA Verified:** ✅ 2026-11-20 (qa-privacy)
- **Файли-власники:**
  - `app/build.gradle.kts`
  - `app/proguard-rules.pro`
  - `docs/tasks/TASK-014.md`
  - `docs/release/CLOSED_TESTING_GUIDE.md`
  - `docs/release/PRIVACY_POLICY.md`

---

## Критерії приймання (Acceptance Criteria)

1. [x] Конфігурація релізу в `app/build.gradle.kts`:
   - увімкнено R8 / ProGuard (`isMinifyEnabled = true`, `isShrinkResources = true`);
   - налаштовано `proguard-rules.pro` (правила для Jetpack Compose, Glance, KotlinX Serialization / Reflection);
   - перевірено унікальний `applicationId = "com.weather.app"` (або рекомендація для Play Store);
   - налаштовано `versionCode = 1`, `versionName = "1.0.0"`.
2. [x] Створення релізного сховища ключів (Keystore) та скрипта/команди підпису:
   - підготовка інструкції для генерації приватного ключа підпису (Keystore) без публікації паролів у репозиторій;
   - збірка релізного Android App Bundle (`.aab` через `./gradlew bundleRelease`).
3. [x] Політика конфіденційності (Privacy Policy) для закритого тестування:
   - підготовлено файл `docs/release/PRIVACY_POLICY.md` з детальним описом використання геопозиції (виключно foreground за дією користувача для показу погоди, відсутність передачі 3-м особам, відсутність фонового відстеження);
   - підготовлено текст для форми Data Safety (Безпека даних) у Play Console.
4. [x] Покрокова інструкція для Власника з налаштування закритого тестування у Google Play Console:
   - акаунт: `appvsdeveloper@gmail.com`;
   - створення застосунку, заповнення декларацій (Data Safety, Ads, Location);
   - налаштування треку Closed Testing (закрите тестування) та залучення тестувальників;
   - завантаження підписаного `.aab`.
5. [x] Верифікація QA:
   - перевірка релізної збірки та відсутності крашів при мініфікації R8;
   - успішне виконання 55/55 юніт-тестів без помилок (`testDebugUnitTest`);
   - успішна збірка бандла `bundleRelease` (`app-release.aab`, розмір: 20 392 850 байт);
   - успішний запуск мініфікованого релізного APK на фізичному пристрої Samsung Galaxy S24 Ultra (Android 14) без крашів чи витоків даних у логах.

---

## Журнал верифікації QA (qa-privacy)
- **2026-11-20:** Незалежний аудит конфігурацій, коду та релізних артефактів проведено агентом `qa-privacy`.
  - Збірка бандла `bundleRelease` пройшла успішно, `app-release.aab` присутній (розмір 20.39 МБ).
  - Усі 55 unit-тестів (`testDebugUnitTest --rerun-tasks`) пройшли успішно (100% pass rate).
  - Релізний APK з R8 мініфікацією та стисненням ресурсів встановлено на фізичний пристрій Samsung Galaxy S24 Ultra (`RZCX326349A`). Додаток та віджет працюють стабільно, крашів і витоку конфіденційних даних немає.
  - Політика конфіденційності та посібник для закритого тестування відповідають вимогам Google Play.
  - Статус переведено в `QA_VERIFIED`.
