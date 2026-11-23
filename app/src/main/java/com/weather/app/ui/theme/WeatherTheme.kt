package com.weather.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.weather.app.domain.model.WeatherCondition

@Immutable
data class WeatherPalette(
    val gradientColors: List<Color>,
    val surfaceTint: Color,
    val textColor: Color,
    val accentColor: Color
)

val LocalWeatherPalette = staticCompositionLocalOf {
    WeatherPalette(
        gradientColors = listOf(ClearSkyDayTop, ClearSkyDayBottom),
        surfaceTint = ClearSkyDaySurfaceTint,
        textColor = Color.White,
        accentColor = SunAccent
    )
}

val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Централізована політика анімацій та руху (Phase 5 — Motion & Accessibility).
 * Забезпечує плавні переходи в нормальному режимі та миттєві або скорочені переходи,
 * коли в системі Android увімкнено зменшення руху (Reduced Motion).
 */
object WeatherMotionPolicy {
    fun skyGradientDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 600
    fun tabCrossfadeDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 220
    fun screenStateCrossfadeDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 80 else 250
    fun sunArcAnimationDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 1000
    fun shouldRunInfiniteAnimations(reducedMotion: Boolean): Boolean = !reducedMotion
}

private val LightColorScheme = lightColorScheme(
    primary = MdLightPrimary,
    onPrimary = MdLightOnPrimary,
    background = MdLightBackground,
    onBackground = MdLightOnBackground,
    surface = MdLightSurface,
    onSurface = MdLightOnSurface,
    surfaceVariant = MdLightSurfaceVariant,
    onSurfaceVariant = MdLightOnSurfaceVariant,
    error = MdLightError,
    outline = MdLightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = MdDarkPrimary,
    onPrimary = MdDarkOnPrimary,
    background = MdDarkBackground,
    onBackground = MdDarkOnBackground,
    surface = MdDarkSurface,
    onSurface = MdDarkOnSurface,
    surfaceVariant = MdDarkSurfaceVariant,
    onSurfaceVariant = MdDarkOnSurfaceVariant,
    error = MdDarkError,
    outline = MdDarkOutline
)

fun weatherPaletteFor(
    condition: WeatherCondition,
    isDark: Boolean,
    isDay: Boolean = true
): WeatherPalette {
    return when (condition) {
        WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> {
            if (isDay) {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyDayTop, ClearSkyDayBottom),
                    surfaceTint = ClearSkyDaySurfaceTint,
                    textColor = Color.White,
                    accentColor = SunAccent
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyNightTop, ClearSkyNightBottom),
                    surfaceTint = ClearSkyNightSurfaceTint,
                    textColor = Color(0xFFE0E6ED),
                    accentColor = MoonAccent
                )
            }
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            if (isDark || !isDay) {
                WeatherPalette(
                    gradientColors = listOf(OvercastDarkTop, OvercastDarkBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color(0xFFF1F2F6),
                    accentColor = MoonAccent
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyDayTop, OvercastLightBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color.White,
                    accentColor = SunAccent
                )
            }
        }
        WeatherCondition.OVERCAST -> {
            if (isDark) {
                WeatherPalette(
                    gradientColors = listOf(OvercastDarkTop, OvercastDarkBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color(0xFFF1F2F6),
                    accentColor = Color(0xFFCFD8DC)
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(OvercastLightTop, OvercastLightBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color.White,
                    accentColor = Color(0xFFCFD8DC)
                )
            }
        }
        WeatherCondition.FOG -> {
            WeatherPalette(
                gradientColors = listOf(FogTop, FogBottom),
                surfaceTint = Color(0x1FFFFFFF),
                textColor = Color.White,
                accentColor = Color(0xFFB0BEC5)
            )
        }
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> {
            WeatherPalette(
                gradientColors = listOf(RainTop, RainBottom),
                surfaceTint = RainSurfaceTint,
                textColor = Color.White,
                accentColor = RainAccent
            )
        }
        WeatherCondition.THUNDERSTORM -> {
            WeatherPalette(
                gradientColors = listOf(ThunderstormTop, ThunderstormBottom),
                surfaceTint = ThunderstormSurfaceTint,
                textColor = Color(0xFFF8F9FA),
                accentColor = LightningAccent
            )
        }
        WeatherCondition.SNOW -> {
            if (isDark) {
                WeatherPalette(
                    gradientColors = listOf(SnowDarkTop, SnowDarkBottom),
                    surfaceTint = SnowSurfaceTint,
                    textColor = Color.White,
                    accentColor = Color(0xFFE1F5FE)
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(SnowLightTop, SnowLightBottom),
                    surfaceTint = SnowSurfaceTint,
                    textColor = Color(0xFF102027),
                    accentColor = Color(0xFFE1F5FE)
                )
            }
        }
        WeatherCondition.UNKNOWN -> {
            WeatherPalette(
                gradientColors = listOf(OvercastLightTop, OvercastLightBottom),
                surfaceTint = OvercastSurfaceTint,
                textColor = Color.White,
                accentColor = Color(0xFFCFD8DC)
            )
        }
    }
}

@Composable
fun WeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    condition: WeatherCondition = WeatherCondition.CLEAR_SKY,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val weatherPalette = weatherPaletteFor(condition, isDark = darkTheme)

    CompositionLocalProvider(
        LocalWeatherPalette provides weatherPalette,
        LocalReducedMotion provides reducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
