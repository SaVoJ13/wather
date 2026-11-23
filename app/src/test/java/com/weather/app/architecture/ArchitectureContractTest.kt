package com.weather.app.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Архітектурні тести (Agent 5 / A5.2) згідно з MASTER PROMPT Phase 2:
 * 1. Чистота Domain Layer: відсутність імпортів Android SDK у доменних моделях та репозиторіях.
 * 2. Ізоляція DTO: Presentation Layer та Domain Layer не повинні напряму залежати від DTO пакетів.
 * 3. Контроль відсутності бойових рекламних ID та секретів у коді.
 */
class ArchitectureContractTest {

    @Test
    fun domainLayer_hasZeroAndroidImports() {
        val domainDir = File("src/main/java/com/weather/app/domain")
        assertTrue("Domain directory must exist", domainDir.exists())

        val files = domainDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("Domain files must be found", files.isNotEmpty())

        val violations = mutableListOf<String>()

        for (file in files) {
            val lines = file.readLines()
            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("import android.")) {
                    violations.add("${file.name}:${index + 1} -> $trimmed")
                }
            }
        }

        assertTrue(
            "Domain layer must have 0 Android Framework imports. Violations found: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun presentationLayer_hasNoDirectDtoImports() {
        val uiDir = File("src/main/java/com/weather/app/ui")
        assertTrue("UI directory must exist", uiDir.exists())

        val files = uiDir.walkTopDown().filter { it.extension == "kt" }.toList()
        val violations = mutableListOf<String>()

        for (file in files) {
            val lines = file.readLines()
            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.contains(".data.remote.dto.")) {
                    violations.add("${file.name}:${index + 1} -> $trimmed")
                }
            }
        }

        assertTrue(
            "Presentation layer must NEVER import DTOs directly. Violations: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun codebase_hasNoHardcodedApiKeysOrSecrets() {
        val srcDir = File("src/main/java/com/weather/app")
        val files = srcDir.walkTopDown().filter { it.extension == "kt" }.toList()
        val suspiciousPatterns = listOf(
            "AIzaSy",               // Google API key prefix
            "WEATHER_API_KEY =",    // hardcoded assignments
            "apiKey = \"",
            "secret = \""
        )

        val violations = mutableListOf<String>()

        for (file in files) {
            val content = file.readText()
            for (pattern in suspiciousPatterns) {
                if (content.contains(pattern)) {
                    violations.add("${file.name} contains '$pattern'")
                }
            }
        }

        assertTrue(
            "Source code must not contain hardcoded secrets or API keys. Violations: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun manifest_doesNotDeclareBackgroundLocation() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifestFile.exists())

        val content = manifestFile.readText()
        assertFalse(
            "ACCESS_BACKGROUND_LOCATION is strictly forbidden by Constitution and Profile",
            content.contains("ACCESS_BACKGROUND_LOCATION")
        )
    }

    @Test
    fun widgetLayer_hasZeroAdsZeroGpsZeroMockFallbackAndZeroWakeupAlarms() {
        val widgetDir = File("src/main/java/com/weather/app/widget")
        assertTrue("Widget directory must exist", widgetDir.exists())

        val files = widgetDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("Widget files must exist", files.isNotEmpty())

        val forbiddenPatterns = listOf(
            "import com.weather.app.data.mock.MockWeatherRepository",
            "MockWeatherRepository.",
            "AdBannerPlaceholder",
            "com.google.android.gms.ads",
            "AdView",
            "LocationManager",
            "FusedLocationProviderClient",
            "LocationClient",
            "ACCESS_BACKGROUND_LOCATION",
            "AlarmManager.ELAPSED_REALTIME_WAKEUP",
            "AlarmManager.RTC_WAKEUP"
        )

        val violations = mutableListOf<String>()

        for (file in files) {
            val lines = file.readLines()
            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue
                for (pattern in forbiddenPatterns) {
                    if (trimmed.contains(pattern)) {
                        violations.add("${file.name}:${index + 1} contains forbidden '$pattern'")
                    }
                }
            }
        }

        assertTrue(
            "Widget package must have 0 ads, 0 GPS calls, 0 MockWeatherRepository fallbacks, and 0 CPU wakeup alarms. Violations: $violations",
            violations.isEmpty()
        )
    }
}
