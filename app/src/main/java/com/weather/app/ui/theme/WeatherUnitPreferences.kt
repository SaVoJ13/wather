package com.weather.app.ui.theme

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.weather.app.data.local.WeatherLocalCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.roundToInt

enum class TemperatureUnit(val label: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}

enum class WindSpeedUnit(val labelUk: String) {
    KMH("км/год"),
    MS("м/с")
}

enum class PressureUnit(val labelUk: String) {
    HPA("гПа"),
    MMHG("мм рт.ст.")
}

@Immutable
data class WeatherUnitState(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA
) {
    fun convertTemperature(celsius: Double): Int {
        return when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> celsius.roundToInt()
            TemperatureUnit.FAHRENHEIT -> ((celsius * 9.0 / 5.0) + 32.0).roundToInt()
        }
    }

    fun formatTemperature(celsius: Double, includePlus: Boolean = true, includeUnitSymbol: Boolean = false): String {
        val value = convertTemperature(celsius)
        val prefix = if (includePlus && value > 0 && temperatureUnit == TemperatureUnit.CELSIUS) "+" else if (includePlus && value > 0) "+" else ""
        val suffix = if (includeUnitSymbol) temperatureUnit.label else "°"
        return "$prefix$value$suffix"
    }

    fun convertWindSpeed(kmh: Double): Double {
        return when (windSpeedUnit) {
            WindSpeedUnit.KMH -> kmh
            WindSpeedUnit.MS -> kmh / 3.6
        }
    }

    fun formatWindSpeed(kmh: Double, unitLabelKmh: String = "км/год", unitLabelMs: String = "м/с"): String {
        return when (windSpeedUnit) {
            WindSpeedUnit.KMH -> "${kmh.roundToInt()} $unitLabelKmh"
            WindSpeedUnit.MS -> "${String.format(Locale.US, "%.1f", kmh / 3.6)} $unitLabelMs"
        }
    }

    fun formatPressurePrimary(hpa: Double, unitLabelHpa: String = "гПа", unitLabelMmHg: String = "мм рт.ст."): String {
        return when (pressureUnit) {
            PressureUnit.HPA -> "${hpa.roundToInt()} $unitLabelHpa"
            PressureUnit.MMHG -> "${(hpa * 0.750062).roundToInt()} $unitLabelMmHg"
        }
    }

    fun formatPressureSecondary(hpa: Double, unitLabelHpa: String = "гПа", unitLabelMmHg: String = "мм рт.ст."): String {
        return when (pressureUnit) {
            PressureUnit.HPA -> "${(hpa * 0.750062).roundToInt()} $unitLabelMmHg"
            PressureUnit.MMHG -> "${hpa.roundToInt()} $unitLabelHpa"
        }
    }
}

val LocalWeatherUnits = staticCompositionLocalOf { WeatherUnitState() }

/**
 * Централізоване збереження та реактивне спостереження за одиницями вимірювання (Stage B — Requirement 7).
 */
object WeatherUnitPreferences {
    private const val PREFS_NAME = "weather_unit_preferences"
    private const val KEY_TEMP_UNIT = "unit_temperature"
    private const val KEY_WIND_UNIT = "unit_wind_speed"
    private const val KEY_PRESSURE_UNIT = "unit_pressure"

    private val _unitState = MutableStateFlow(WeatherUnitState())
    val unitState: StateFlow<WeatherUnitState> = _unitState.asStateFlow()

    fun load(context: Context? = null): WeatherUnitState {
        val ctx = context?.applicationContext ?: WeatherLocalCache.appContext
        if (ctx == null) {
            return _unitState.value
        }
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val temp = runCatching {
            TemperatureUnit.valueOf(prefs.getString(KEY_TEMP_UNIT, TemperatureUnit.CELSIUS.name)!!)
        }.getOrDefault(TemperatureUnit.CELSIUS)

        val wind = runCatching {
            WindSpeedUnit.valueOf(prefs.getString(KEY_WIND_UNIT, WindSpeedUnit.KMH.name)!!)
        }.getOrDefault(WindSpeedUnit.KMH)

        val pressure = runCatching {
            PressureUnit.valueOf(prefs.getString(KEY_PRESSURE_UNIT, PressureUnit.HPA.name)!!)
        }.getOrDefault(PressureUnit.HPA)

        val loaded = WeatherUnitState(temp, wind, pressure)
        _unitState.value = loaded
        return loaded
    }

    fun setTemperatureUnit(context: Context? = null, unit: TemperatureUnit) {
        val updated = _unitState.value.copy(temperatureUnit = unit)
        _unitState.value = updated
        saveToPrefs(context, updated)
    }

    fun setWindSpeedUnit(context: Context? = null, unit: WindSpeedUnit) {
        val updated = _unitState.value.copy(windSpeedUnit = unit)
        _unitState.value = updated
        saveToPrefs(context, updated)
    }

    fun setPressureUnit(context: Context? = null, unit: PressureUnit) {
        val updated = _unitState.value.copy(pressureUnit = unit)
        _unitState.value = updated
        saveToPrefs(context, updated)
    }

    fun resetForTests() {
        _unitState.value = WeatherUnitState()
    }

    private fun saveToPrefs(context: Context?, state: WeatherUnitState) {
        val ctx = context?.applicationContext ?: WeatherLocalCache.appContext ?: return
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TEMP_UNIT, state.temperatureUnit.name)
            putString(KEY_WIND_UNIT, state.windSpeedUnit.name)
            putString(KEY_PRESSURE_UNIT, state.pressureUnit.name)
            apply()
        }
    }
}
