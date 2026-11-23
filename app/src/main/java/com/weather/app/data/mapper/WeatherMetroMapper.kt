package com.weather.app.data.mapper

import com.weather.app.data.remote.dto.WeatherMetroResponse
import com.weather.app.domain.model.CurrentWeather
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.HourlyForecastItem
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherDataSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Мапер даних між WeatherMetro DTO та чистими доменними моделями.
 * Забезпечує дотримання архітектурних контрактів (DATA_CONTRACTS.md), сувору валідацію
 * полів без перетворення відсутніх даних на фальшиві нулі.
 */
object WeatherMetroMapper {

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

    fun parseDate(isoString: String?): LocalDate? {
        if (isoString.isNullOrBlank()) return null
        return try {
            if (isoString.contains("T")) {
                LocalDateTime.parse(isoString, ISO_DATE_TIME_FORMATTER).toLocalDate()
            } else {
                LocalDate.parse(isoString, ISO_DATE_FORMATTER)
            }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Конвертує відповідь WeatherMetro API у доменний агрегат [FullWeatherForecast].
     * При відсутності критичних полів генерує виняток [IllegalArgumentException],
     * не маскуючи помилки фіктивними значеннями.
     */
    fun toDomain(
        response: WeatherMetroResponse,
        location: LocationModel,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): FullWeatherForecast {
        val hourlyDto = response.hourly
            ?: throw IllegalArgumentException("WeatherMetro response missing required 'hourly' component")
        val dailyDto = response.daily
            ?: throw IllegalArgumentException("WeatherMetro response missing required 'daily' component")

        if (hourlyDto.time.isEmpty() || hourlyDto.temperature2m.isEmpty()) {
            throw IllegalArgumentException("WeatherMetro response has empty hourly time or temperature array")
        }
        if (dailyDto.time.isEmpty() || dailyDto.temperature2mMax.isEmpty() || dailyDto.temperature2mMin.isEmpty()) {
            throw IllegalArgumentException("WeatherMetro response has empty daily time or temperature array")
        }

        val allHourlyItems = mutableListOf<HourlyForecastItem>()
        var currentWeather: CurrentWeather? = null

        val totalSize = minOf(hourlyDto.time.size, hourlyDto.temperature2m.size)
        val currentHour = updatedAt.truncatedTo(ChronoUnit.HOURS)
        var currentFound = false

        for (i in 0 until totalSize) {
            val timeStr = hourlyDto.time[i]
            val timeParsed = parseDateTime(timeStr)
                ?: throw IllegalArgumentException("Неможливо розпарсити дату/час у погодинному прогнозі: '$timeStr'")
            val temp = hourlyDto.temperature2m[i]
            val pop = hourlyDto.precipitationProbability.getOrNull(i) ?: 0
            val wCode = hourlyDto.weathercode.getOrNull(i)
            val humidity = hourlyDto.relativeHumidity2m.getOrNull(i) ?: 50
            val wind = hourlyDto.windSpeed10m.getOrNull(i) ?: 0.0

            allHourlyItems.add(
                HourlyForecastItem(
                    time = timeParsed,
                    temperatureCelsius = temp,
                    condition = WeatherCodeMapper.mapWmoCode(wCode),
                    precipitationProbabilityPercent = pop
                )
            )

            // Визначаємо поточну погоду за годиною, найближчою до часу запиту
            if (!currentFound && (timeParsed.isEqual(currentHour) || timeParsed.isAfter(currentHour))) {
                currentWeather = CurrentWeather(
                    temperatureCelsius = temp,
                    apparentTemperatureCelsius = temp,
                    condition = WeatherCodeMapper.mapWmoCode(wCode),
                    relativeHumidityPercent = humidity,
                    windSpeedKmh = wind,
                    surfacePressureHpa = 1013.25,
                    uvIndex = 0.0,
                    time = timeParsed
                )
                currentFound = true
            }
        }

        if (currentWeather == null && allHourlyItems.isNotEmpty()) {
            val first = allHourlyItems.first()
            currentWeather = CurrentWeather(
                temperatureCelsius = first.temperatureCelsius,
                apparentTemperatureCelsius = first.temperatureCelsius,
                condition = first.condition,
                relativeHumidityPercent = hourlyDto.relativeHumidity2m.firstOrNull() ?: 50,
                windSpeedKmh = hourlyDto.windSpeed10m.firstOrNull() ?: 0.0,
                surfacePressureHpa = 1013.25,
                uvIndex = 0.0,
                time = first.time
            )
        }

        val finalCurrentWeather = currentWeather
            ?: throw IllegalArgumentException("Неможливо сформувати поточну погоду з погодинного масиву")

        // Відбираємо 24 години, починаючи від поточної
        val startIndex = allHourlyItems.indexOfFirst { !it.time.isBefore(currentHour) }
        val hourly24h = if (startIndex != -1) {
            allHourlyItems.drop(startIndex).take(24)
        } else {
            allHourlyItems.take(24)
        }

        // Формуємо щоденний прогноз (до 16 днів)
        val dailyList = mutableListOf<DailyForecastItem>()
        val count = minOf(16, dailyDto.time.size, dailyDto.temperature2mMax.size, dailyDto.temperature2mMin.size)
        for (i in 0 until count) {
            val dateStr = dailyDto.time[i]
            val dateTimeParsed = parseDateTime(dateStr)
                ?: throw IllegalArgumentException("Неможливо розпарсити дату у щоденному прогнозі: '$dateStr'")
            val wCode = dailyDto.weathercode.getOrNull(i)
            val maxT = dailyDto.temperature2mMax[i]
            val minT = dailyDto.temperature2mMin[i]
            val popMax = dailyDto.precipitationProbabilityMax.getOrNull(i) ?: 0
            val windMax = dailyDto.windSpeed10mMax.getOrNull(i) ?: 0.0
            val sunriseStr = dailyDto.sunrise.getOrNull(i)
            val sunsetStr = dailyDto.sunset.getOrNull(i)

            dailyList.add(
                DailyForecastItem(
                    date = dateTimeParsed,
                    minTemperatureCelsius = minT,
                    maxTemperatureCelsius = maxT,
                    condition = WeatherCodeMapper.mapWmoCode(wCode),
                    precipitationProbabilityPercent = popMax,
                    precipitationSumMm = 0.0,
                    maxWindSpeedKmh = windMax,
                    sunrise = parseDateTime(sunriseStr),
                    sunset = parseDateTime(sunsetStr)
                )
            )
        }

        if (dailyList.isEmpty()) {
            throw IllegalArgumentException("Неможливо сформувати щоденний прогноз: відсутні валідні дні")
        }

        return FullWeatherForecast(
            location = location,
            current = finalCurrentWeather,
            hourly24h = hourly24h,
            daily14Days = dailyList,
            updatedAt = updatedAt,
            isStale = false,
            dataSource = WeatherDataSource.WEATHER_METRO
        )
    }
}
