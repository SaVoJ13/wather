package com.weather.app.ui

import com.weather.app.domain.model.WeatherDataSource
import com.weather.app.data.mock.MockWeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Тести для перевірки динамічної атрибуції джерел погодних даних.
 */
class DynamicAttributionTest {

    @Test
    fun testWeatherDataSourceEnumContainsAllRequiredProviders() {
        val providers = WeatherDataSource.values().map { it.name }
        assertTrue(providers.contains("OPEN_METEO"))
        assertTrue(providers.contains("WEATHER_METRO"))
        assertTrue(providers.contains("VISUAL_CROSSING"))
        assertTrue(providers.contains("MOCK"))
    }

    @Test
    fun testMockWeatherRepositoryProvidesMockDataSource() {
        val forecast = MockWeatherRepository.createKyivForecast()
        assertEquals(WeatherDataSource.MOCK, forecast.dataSource)
    }

    @Test
    fun testAllLanguagesContainAllAttributionStrings() {
        val resDir = File("src/main/res")
        val valuesDirs = resDir.listFiles { f -> f.isDirectory && f.name.startsWith("values") } ?: emptyArray()
        assertTrue("Values directories must be found", valuesDirs.isNotEmpty())

        val requiredKeys = listOf(
            "source_open_meteo",
            "source_weather_metro",
            "source_visual_crossing",
            "source_mock"
        )

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        for (dir in valuesDirs) {
            val stringsFile = File(dir, "strings.xml")
            if (!stringsFile.exists()) continue

            val doc = builder.parse(stringsFile)
            val stringNodes = doc.getElementsByTagName("string")
            val foundKeys = mutableSetOf<String>()

            for (i in 0 until stringNodes.length) {
                val node = stringNodes.item(i)
                val nameAttr = node.attributes?.getNamedItem("name")?.nodeValue
                if (nameAttr != null) {
                    foundKeys.add(nameAttr)
                }
            }

            for (key in requiredKeys) {
                assertTrue(
                    "File ${stringsFile.path} must contain key '$key'",
                    foundKeys.contains(key)
                )
            }
        }
    }
}
