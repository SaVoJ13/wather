package com.weather.app.data.mock

import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Автономний мок-репозиторій погодних даних для автономної розробки UI та тестів без інтернету.
 * Містить реалістичні фікстури повної 14-денної погоди та 24-годинного погодинного прогнозу.
 */
class MockWeatherRepository : WeatherRepository {

    private val _currentForecast = MutableStateFlow<FullWeatherForecast?>(null)

    init {
        // За замовчуванням ініціалізуємо Києвом
        _currentForecast.value = createKyivForecast()
    }

    override suspend fun getFullForecast(
        location: LocationModel,
        forceRefresh: Boolean
    ): Result<FullWeatherForecast> {
        val forecast = when {
            location.name.contains("Warsaw", ignoreCase = true) || location.name.contains("Варшава", ignoreCase = true) -> {
                createWarsawForecast(location)
            }
            else -> {
                createKyivForecast(location)
            }
        }
        _currentForecast.value = forecast
        return Result.success(forecast)
    }

    override fun observeCurrentForecast(): Flow<FullWeatherForecast?> {
        return _currentForecast.asStateFlow()
    }

    companion object {
        fun createKyivLocation() = LocationModel(
            id = 1L,
            name = "Київ",
            country = "Україна",
            admin1 = "Київська область",
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv",
            isCurrentLocation = true
        )

        fun createWarsawLocation() = LocationModel(
            id = 2L,
            name = "Варшава",
            country = "Польща",
            admin1 = "Мазовецьке воєводство",
            latitude = 52.2297,
            longitude = 21.0122,
            timezone = "Europe/Warsaw",
            isCurrentLocation = false
        )

        /**
         * Фікстура для Києва: комфортна помірно-сонячна погода (Clear / Mainly clear / Partly cloudy).
         */
        fun createKyivForecast(customLocation: LocationModel? = null): FullWeatherForecast {
            val location = customLocation ?: createKyivLocation()
            val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)

            val current = CurrentWeather(
                temperatureCelsius = 21.5,
                apparentTemperatureCelsius = 22.0,
                condition = WeatherCondition.CLEAR_SKY,
                relativeHumidityPercent = 48,
                windSpeedKmh = 12.5,
                surfacePressureHpa = 1016.0,
                uvIndex = 4.2,
                time = now
            )

            // 24 години прогнозу
            val hourly = (0..23).map { hourOffset ->
                val time = now.plusHours(hourOffset.toLong())
                val temp = when (time.hour) {
                    in 0..5 -> 14.0 + (time.hour * 0.4)
                    in 6..11 -> 16.0 + ((time.hour - 6) * 1.2)
                    in 12..16 -> 22.0 + ((16 - time.hour) * 0.3)
                    else -> 20.0 - ((time.hour - 16) * 0.8)
                }
                val cond = when (time.hour) {
                    in 12..17 -> WeatherCondition.PARTLY_CLOUDY
                    else -> WeatherCondition.CLEAR_SKY
                }
                val pop = if (time.hour in 13..16) 15 else 0

                HourlyForecastItem(
                    time = time,
                    temperatureCelsius = Math.round(temp * 10.0) / 10.0,
                    condition = cond,
                    precipitationProbabilityPercent = pop
                )
            }

            // 14 днів прогнозу
            val dailyConditions = listOf(
                WeatherCondition.CLEAR_SKY,
                WeatherCondition.MAINLY_CLEAR,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.CLEAR_SKY,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.RAIN,
                WeatherCondition.MAINLY_CLEAR,
                WeatherCondition.CLEAR_SKY,
                WeatherCondition.CLEAR_SKY,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.OVERCAST,
                WeatherCondition.RAIN,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.CLEAR_SKY
            )

            val daily = (0..13).map { dayOffset ->
                val date = now.plusDays(dayOffset.toLong()).withHour(12)
                val cond = dailyConditions[dayOffset % dailyConditions.size]
                val maxT = 20.0 + (dayOffset % 5) - (if (cond == WeatherCondition.RAIN) 4.0 else 0.0)
                val minT = 11.0 + (dayOffset % 4)

                DailyForecastItem(
                    date = date,
                    minTemperatureCelsius = minT,
                    maxTemperatureCelsius = maxT,
                    condition = cond,
                    precipitationProbabilityPercent = if (cond == WeatherCondition.RAIN) 75 else 10,
                    precipitationSumMm = if (cond == WeatherCondition.RAIN) 6.4 else 0.0,
                    maxWindSpeedKmh = 14.0 + (dayOffset % 6),
                    sunrise = date.withHour(5).withMinute(45),
                    sunset = date.withHour(20).withMinute(30)
                )
            }

            return FullWeatherForecast(
                location = location,
                current = current,
                hourly24h = hourly,
                daily14Days = daily,
                updatedAt = now,
                isStale = false,
                dataSource = com.weather.app.domain.model.WeatherDataSource.MOCK
            )
        }

        /**
         * Фікстура для Варшави: дощова / хмарна погода (Overcast / Rain / Thunderstorm).
         */
        fun createWarsawForecast(customLocation: LocationModel? = null): FullWeatherForecast {
            val location = customLocation ?: createWarsawLocation()
            val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)

            val current = CurrentWeather(
                temperatureCelsius = 15.2,
                apparentTemperatureCelsius = 14.5,
                condition = WeatherCondition.RAIN,
                relativeHumidityPercent = 86,
                windSpeedKmh = 22.0,
                surfacePressureHpa = 1008.0,
                uvIndex = 1.8,
                time = now
            )

            val hourly = (0..23).map { hourOffset ->
                val time = now.plusHours(hourOffset.toLong())
                val temp = 14.0 + (sinHour(time.hour) * 3.0)
                val cond = when {
                    time.hour in 14..16 -> WeatherCondition.THUNDERSTORM
                    time.hour % 2 == 0 -> WeatherCondition.RAIN
                    else -> WeatherCondition.OVERCAST
                }
                val pop = when (cond) {
                    WeatherCondition.THUNDERSTORM -> 90
                    WeatherCondition.RAIN -> 70
                    else -> 30
                }

                HourlyForecastItem(
                    time = time,
                    temperatureCelsius = Math.round(temp * 10.0) / 10.0,
                    condition = cond,
                    precipitationProbabilityPercent = pop
                )
            }

            val dailyConditions = listOf(
                WeatherCondition.RAIN,
                WeatherCondition.HEAVY_RAIN,
                WeatherCondition.OVERCAST,
                WeatherCondition.DRIZZLE,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.RAIN,
                WeatherCondition.THUNDERSTORM,
                WeatherCondition.OVERCAST,
                WeatherCondition.MAINLY_CLEAR,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.DRIZZLE,
                WeatherCondition.RAIN,
                WeatherCondition.OVERCAST,
                WeatherCondition.MAINLY_CLEAR
            )

            val daily = (0..13).map { dayOffset ->
                val date = now.plusDays(dayOffset.toLong()).withHour(12)
                val cond = dailyConditions[dayOffset % dailyConditions.size]
                val maxT = 16.0 + (dayOffset % 4) - (if (cond == WeatherCondition.HEAVY_RAIN) 3.0 else 0.0)
                val minT = 9.0 + (dayOffset % 3)

                DailyForecastItem(
                    date = date,
                    minTemperatureCelsius = minT,
                    maxTemperatureCelsius = maxT,
                    condition = cond,
                    precipitationProbabilityPercent = when (cond) {
                        WeatherCondition.HEAVY_RAIN, WeatherCondition.THUNDERSTORM -> 85
                        WeatherCondition.RAIN -> 65
                        WeatherCondition.DRIZZLE -> 45
                        else -> 15
                    },
                    precipitationSumMm = when (cond) {
                        WeatherCondition.HEAVY_RAIN -> 14.2
                        WeatherCondition.RAIN -> 5.5
                        WeatherCondition.THUNDERSTORM -> 18.0
                        else -> 0.0
                    },
                    maxWindSpeedKmh = 20.0 + (dayOffset % 10),
                    sunrise = date.withHour(5).withMinute(30),
                    sunset = date.withHour(20).withMinute(15)
                )
            }

            return FullWeatherForecast(
                location = location,
                current = current,
                hourly24h = hourly,
                daily14Days = daily,
                updatedAt = now,
                isStale = false,
                dataSource = com.weather.app.domain.model.WeatherDataSource.MOCK
            )
        }

        private fun sinHour(hour: Int): Double {
            return kotlin.math.sin((hour - 6) * Math.PI / 12.0)
        }
    }
}
