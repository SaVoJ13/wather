package com.weather.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weather.app.data.local.WeatherLocalCache
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.model.WeatherDataSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Інструментальний тест для WeatherLocalCache з реальним Android Context та SharedPreferences.
 * 
 * Відповідно до вимог TASK 3 (Agent 5: Реальна перевірка кешу):
 * 1. Перевіряє запис та читання даних у реальному SharedPreferences на рівні Android ОС.
 * 2. Перевіряє ізоляцію кешу міст: кеш міста А (Київ) ніколи не повертається для міста Б (Варшава).
 * 3. Перевіряє коректність збереження та відновлення null-показників (достовірність метеоданих).
 */
@RunWith(AndroidJUnit4::class)
class WeatherLocalCacheAndroidTest {

    private lateinit var context: Context

    private val kyivLocation = LocationModel(
        id = 703448L,
        name = "Київ",
        country = "Україна",
        latitude = 50.4501,
        longitude = 30.5234,
        timezone = "Europe/Kyiv",
        isCurrentLocation = true
    )

    private val warsawLocation = LocationModel(
        id = 756135L,
        name = "Варшава",
        country = "Польща",
        latitude = 52.2297,
        longitude = 21.0122,
        timezone = "Europe/Warsaw",
        isCurrentLocation = false
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WeatherLocalCache.init(context)
        WeatherLocalCache.clearAll(context)
    }

    @After
    fun tearDown() {
        WeatherLocalCache.clearAll(context)
    }

    @Test
    fun testRealSharedPreferencesPersistenceAndIsolation() {
        val now = LocalDateTime.now()
        val forecast = FullWeatherForecast(
            location = kyivLocation,
            current = CurrentWeather(
                temperatureCelsius = 15.5,
                apparentTemperatureCelsius = null, // Відсутнє значення
                condition = WeatherCondition.PARTLY_CLOUDY,
                relativeHumidityPercent = null,     // Відсутнє значення
                windSpeedKmh = 14.0,
                surfacePressureHpa = null,          // Відсутнє значення (не 1013.25!)
                uvIndex = null,                     // Відсутнє значення (не 0.0!)
                time = now
            ),
            hourly24h = listOf(
                HourlyForecastItem(
                    time = now,
                    temperatureCelsius = 15.5,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    precipitationProbabilityPercent = null
                )
            ),
            daily14Days = listOf(
                DailyForecastItem(
                    date = now,
                    minTemperatureCelsius = 10.0,
                    maxTemperatureCelsius = 18.0,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    precipitationProbabilityPercent = null,
                    precipitationSumMm = null,
                    maxWindSpeedKmh = 18.0
                )
            ),
            updatedAt = now,
            isStale = false,
            dataSource = WeatherDataSource.WEATHER_METRO
        )

        // 1. Зберігаємо прогноз у реальний SharedPreferences
        WeatherLocalCache.saveForecast(context, forecast)

        // 2. Читаємо прогноз для Києва
        val loadedKyiv = WeatherLocalCache.loadCachedForecastForLocation(context, kyivLocation)
        assertNotNull("Кеш для Києва повинен успішно зчитуватися з SharedPreferences", loadedKyiv)
        assertEquals("Київ", loadedKyiv?.location?.name)
        assertEquals(15.5, loadedKyiv?.current?.temperatureCelsius ?: 0.0, 0.01)
        assertNull("Відсутній apparentTemperature повинен залишатися null", loadedKyiv?.current?.apparentTemperatureCelsius)
        assertNull("Відсутній pressure повинен залишатися null", loadedKyiv?.current?.surfacePressureHpa)
        assertNull("Відсутній uvIndex повинен залишатися null", loadedKyiv?.current?.uvIndex)
        assertNull("Відсутня humidity повинна залишатися null", loadedKyiv?.current?.relativeHumidityPercent)
        assertEquals(14.0, loadedKyiv?.current?.windSpeedKmh ?: 0.0, 0.01)

        // 3. Ізоляція міст: для Варшави кеш Києва повертатися НЕ ПОВИНЕН
        val loadedWarsaw = WeatherLocalCache.loadCachedForecastForLocation(context, warsawLocation)
        assertNull("Кеш одного міста (Києва) за жодних умов не повинен повертатися для іншого (Варшави)", loadedWarsaw)
    }

    @Test
    fun testGlanceWidgetOfflineCacheAndProviderRegistrationOnAndroid() {
        // 1. Коли кеш порожній, віджет повертає null (чесний стан "Немає даних", без MockWeatherRepository)
        WeatherLocalCache.clearAll(context)
        val emptyWidgetForecast = com.weather.app.widget.WeatherGlanceWidget.resolveWidgetForecast(
            cachedForecast = WeatherLocalCache.loadCachedForecast(context),
            lastKnownForecast = null
        )
        assertNull("Віджет не повинен повертати вигаданий прогноз, коли кеш порожній", emptyWidgetForecast)

        // 2. Після збереження реального прогнозу в кеш віджет отримує його для 2x2 і 4x2 режимів
        val now = LocalDateTime.now()
        val realForecast = FullWeatherForecast(
            location = warsawLocation,
            current = CurrentWeather(
                temperatureCelsius = 7.2,
                apparentTemperatureCelsius = 5.0,
                condition = WeatherCondition.RAIN,
                relativeHumidityPercent = 82,
                windSpeedKmh = 18.5,
                surfacePressureHpa = 1009.0,
                uvIndex = 1.0,
                time = now
            ),
            hourly24h = emptyList(),
            daily14Days = listOf(
                DailyForecastItem(
                    date = now,
                    minTemperatureCelsius = 3.0,
                    maxTemperatureCelsius = 9.0,
                    condition = WeatherCondition.RAIN,
                    precipitationProbabilityPercent = 70,
                    precipitationSumMm = 4.2,
                    maxWindSpeedKmh = 22.0
                )
            ),
            updatedAt = now,
            isStale = false,
            dataSource = WeatherDataSource.WEATHER_METRO
        )
        WeatherLocalCache.saveForecast(context, realForecast)

        val resolved = com.weather.app.widget.WeatherGlanceWidget.resolveWidgetForecast(
            cachedForecast = WeatherLocalCache.loadCachedForecast(context),
            lastKnownForecast = null
        )
        assertNotNull("Віджет повинен успішно зчитувати реальний прогноз з дискового кешу Android", resolved)
        assertEquals("Варшава", resolved?.location?.name)
        assertEquals("↑9° / ↓3°", resolved?.let { com.weather.app.widget.WeatherGlanceWidget.formatTodayRange(it) })

        // 3. Перевіряємо реєстрацію AppWidgetProvider у системному AppWidgetManager Android
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val installedProviders = appWidgetManager.getInstalledProvidersForPackage(context.packageName, null)
        val weatherProvider = installedProviders.firstOrNull {
            it.provider.className == com.weather.app.widget.WeatherWidgetReceiver::class.java.name
        }
        assertNotNull("WeatherWidgetReceiver повинен бути зареєстрований у системному AppWidgetManager", weatherProvider)
    }
}
