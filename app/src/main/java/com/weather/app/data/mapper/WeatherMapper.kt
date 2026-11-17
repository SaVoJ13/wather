package com.weather.app.data.mapper

import com.weather.app.data.remote.dto.OpenMeteoForecastResponse
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Мапер даних між Open-Meteo DTO та чистими доменними моделями.
 */
object WeatherMapper {

    private val ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseDateTime(isoString: String?): LocalDateTime? {
        if (isoString.isNullOrBlank()) return null
        return try {
            if (isoString.contains("T")) {
                LocalDateTime.parse(isoString, ISO_DATE_TIME_FORMATTER)
            } else {
                LocalDate.parse(isoString, ISO_DATE_FORMATTER).atStartOfDay()
            }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun toDomain(
        response: OpenMeteoForecastResponse,
        location: LocationModel,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): FullWeatherForecast {
        val currentDto = response.current
        val current = if (currentDto != null) {
            CurrentWeather(
                temperatureCelsius = currentDto.temperature_2m,
                apparentTemperatureCelsius = currentDto.apparent_temperature ?: currentDto.temperature_2m,
                condition = WeatherCodeMapper.mapWmoCode(currentDto.weather_code),
                relativeHumidityPercent = currentDto.relative_humidity_2m ?: 0,
                windSpeedKmh = currentDto.wind_speed_10m ?: 0.0,
                surfacePressureHpa = currentDto.surface_pressure ?: 1013.25,
                uvIndex = currentDto.uv_index ?: 0.0,
                time = parseDateTime(currentDto.time) ?: updatedAt
            )
        } else {
            CurrentWeather(
                temperatureCelsius = 0.0,
                apparentTemperatureCelsius = 0.0,
                condition = WeatherCodeMapper.mapWmoCode(null),
                relativeHumidityPercent = 0,
                windSpeedKmh = 0.0,
                surfacePressureHpa = 1013.25,
                uvIndex = 0.0,
                time = updatedAt
            )
        }

        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourlyDto = response.hourly
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val count = minOf(24, hourlyDto.time.size)
            for (i in 0 until count) {
                val timeParsed = parseDateTime(hourlyDto.time.getOrNull(i)) ?: updatedAt.plusHours(i.toLong())
                val temp = hourlyDto.temperature_2m.getOrNull(i) ?: 0.0
                val wCode = hourlyDto.weather_code.getOrNull(i)
                val pop = hourlyDto.precipitation_probability?.getOrNull(i) ?: 0
                hourlyList.add(
                    HourlyForecastItem(
                        time = timeParsed,
                        temperatureCelsius = temp,
                        condition = WeatherCodeMapper.mapWmoCode(wCode),
                        precipitationProbabilityPercent = pop
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyDto = response.daily
        if (dailyDto != null && dailyDto.time.isNotEmpty()) {
            val count = minOf(14, dailyDto.time.size)
            for (i in 0 until count) {
                val dateParsed = parseDateTime(dailyDto.time.getOrNull(i)) ?: updatedAt.plusDays(i.toLong())
                val maxTemp = dailyDto.temperature_2m_max.getOrNull(i) ?: 0.0
                val minTemp = dailyDto.temperature_2m_min.getOrNull(i) ?: 0.0
                val wCode = dailyDto.weather_code.getOrNull(i)
                val popMax = dailyDto.precipitation_probability_max?.getOrNull(i) ?: 0
                val precipSum = dailyDto.precipitation_sum?.getOrNull(i) ?: 0.0
                val windSpeed = dailyDto.wind_speed_10m_max?.getOrNull(i) ?: 0.0
                val sunrise = parseDateTime(dailyDto.sunrise?.getOrNull(i))
                val sunset = parseDateTime(dailyDto.sunset?.getOrNull(i))

                dailyList.add(
                    DailyForecastItem(
                        date = dateParsed,
                        minTemperatureCelsius = minTemp,
                        maxTemperatureCelsius = maxTemp,
                        condition = WeatherCodeMapper.mapWmoCode(wCode),
                        precipitationProbabilityPercent = popMax,
                        precipitationSumMm = precipSum,
                        maxWindSpeedKmh = windSpeed,
                        sunrise = sunrise,
                        sunset = sunset
                    )
                )
            }
        }

        return FullWeatherForecast(
            location = location,
            current = current,
            hourly24h = hourlyList,
            daily14Days = dailyList,
            updatedAt = updatedAt,
            isStale = false
        )
    }
}
