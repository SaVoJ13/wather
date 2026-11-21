package com.weather.app.ui

import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.supportedLanguages
import com.weather.app.widget.WeatherGlanceWidget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.DayOfWeek
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class LocalizationTest {

    @Test
    fun allWeatherConditionsHaveStringResources() {
        for (condition in WeatherCondition.entries) {
            assertTrue(
                "Condition $condition must have a positive string resource ID",
                condition.stringRes > 0
            )
            assertFalse(
                "Legacy descriptionUk must not be empty for backward compatibility",
                condition.descriptionUk.isBlank()
            )
        }
    }

    @Test
    fun supportedLanguagesContainsAllRequiredLocales() {
        val codes = supportedLanguages.map { it.code }
        val required = listOf("", "uk", "pl", "en", "de", "ru", "es", "it")
        for (req in required) {
            assertTrue("Supported languages must include code '$req'", codes.contains(req))
        }
    }

    @Test
    fun glanceWidgetFormatsDaysForDifferentLocales() {
        val days = DayOfWeek.values()
        val locales = listOf(
            Locale("uk", "UA"),
            Locale("pl", "PL"),
            Locale.ENGLISH,
            Locale.GERMAN,
            Locale("es", "ES"),
            Locale.ITALIAN,
            Locale("ru", "RU")
        )

        for (locale in locales) {
            for (day in days) {
                val formatted = WeatherGlanceWidget.formatDayOfWeek(day, locale)
                assertNotNull("Day formatting should not be null", formatted)
                assertTrue("Day format for $day in $locale should not be empty", formatted.isNotEmpty())
            }
        }

        // Test explicit Polish short names
        assertEquals("pn", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.MONDAY, Locale("pl", "PL")))
        assertEquals("wt", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.TUESDAY, Locale("pl", "PL")))
        assertEquals("śr", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.WEDNESDAY, Locale("pl", "PL")))
        assertEquals("cz", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.THURSDAY, Locale("pl", "PL")))
        assertEquals("pt", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.FRIDAY, Locale("pl", "PL")))
        assertEquals("sb", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.SATURDAY, Locale("pl", "PL")))
        assertEquals("nd", WeatherGlanceWidget.formatDayOfWeek(DayOfWeek.SUNDAY, Locale("pl", "PL")))
    }

    @Test
    fun stringsXmlFilesAreWellFormedAndContainCoreKeys() {
        val resDir = File("src/main/res")
        val targetDirs = listOf(
            "values",
            "values-uk",
            "values-pl",
            "values-en",
            "values-de",
            "values-ru",
            "values-es",
            "values-it"
        )

        val coreKeys = listOf(
            "app_name",
            "now",
            "today",
            "tomorrow",
            "feels_like",
            "updated_at",
            "hourly_forecast_title",
            "daily_forecast_title",
            "metric_humidity",
            "metric_wind",
            "metric_pressure",
            "metric_uv",
            "sunrise_sunset_title",
            "radar_title",
            "select_language",
            "lang_pl",
            "condition_clear_sky",
            "condition_rain",
            "condition_snow",
            "gps_error",
            "location_service_error",
            "location_permission_denied",
            "refresh_weather_desc",
            "loading_weather",
            "loading_weather_desc",
            "failed_to_load_weather",
            "radar_play",
            "radar_pause",
            "compass_north",
            "compass_south",
            "compass_west",
            "compass_east",
            "distance_km",
            "unit_mm",
            "remove_city_desc"
        )

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()

        for (dirName in targetDirs) {
            val file = File(resDir, "$dirName/strings.xml")
            assertTrue("Resource file must exist: ${file.path}", file.exists())

            val doc = dBuilder.parse(file)
            doc.documentElement.normalize()
            assertEquals("Root element must be <resources>", "resources", doc.documentElement.nodeName)

            val stringNodes = doc.getElementsByTagName("string")
            assertTrue("File $dirName/strings.xml must have string entries", stringNodes.length > 20)

            val foundKeys = mutableSetOf<String>()
            for (i in 0 until stringNodes.length) {
                val node = stringNodes.item(i)
                val key = node.attributes.getNamedItem("name")?.nodeValue
                if (key != null) {
                    foundKeys.add(key)
                }
            }

            for (key in coreKeys) {
                assertTrue("Language $dirName must contain string key '$key'", foundKeys.contains(key))
            }
        }
    }
}
