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
import androidx.compose.ui.graphics.luminance
import com.weather.app.domain.model.WeatherCondition
import kotlin.math.max
import kotlin.math.min

@Immutable
data class WeatherPalette(
    val gradientColors: List<Color>,
    val surfaceTint: Color,
    val textColor: Color,
    val accentColor: Color,
    val isDay: Boolean = true,
    val glassCardTopColor: Color = GlassCardDayTop,
    val glassCardBottomColor: Color = GlassCardDayBottom,
    val glassBorderTopColor: Color = GlassSpecularBorderTop,
    val glassBorderBottomColor: Color = GlassSpecularBorderBottom,
    val heroGlowColor: Color = SunAccent,
    val navBarContainerColor: Color = NavBarContainerSurface,
    val navBarTopBorderColor: Color = NavBarTopBorder,
    val navBarSelectedIconColor: Color = NavBarSelectedIcon,
    val navBarSelectedTextColor: Color = NavBarSelectedText,
    val navBarUnselectedIconColor: Color = NavBarUnselectedIcon,
    val navBarUnselectedTextColor: Color = NavBarUnselectedText,
    val navBarIndicatorColor: Color = NavBarIndicatorPill
)

val LocalWeatherPalette = staticCompositionLocalOf {
    WeatherPalette(
        gradientColors = listOf(ClearSkyDayTop, ClearSkyDayMid, ClearSkyDayBottom),
        surfaceTint = ClearSkyDaySurfaceTint,
        textColor = Color.White,
        accentColor = SunAccent,
        isDay = true
    )
}

val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Централізована політика анімацій та руху (Phase 5 & Visual Design Recovery v2).
 * Забезпечує плавні переходи в нормальному режимі та миттєві або скорочені переходи,
 * коли в системі Android увімкнено зменшення руху (Reduced Motion).
 */
object WeatherMotionPolicy {
    fun skyGradientDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 600
    fun tabCrossfadeDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 220
    fun screenStateCrossfadeDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 80 else 250
    fun sunArcAnimationDurationMillis(reducedMotion: Boolean): Int = if (reducedMotion) 0 else 1000
    fun shouldRunInfiniteAnimations(reducedMotion: Boolean): Boolean = !reducedMotion

    /**
     * Обчислює коефіцієнт контрастності WCAG 2.1 між кольором тексту/іконки та фоном (від 1.0 до 21.0).
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val lum1 = foreground.luminance().toDouble()
        val lum2 = background.luminance().toDouble()
        val brightest = max(lum1, lum2)
        val darkest = min(lum1, lum2)
        return (brightest + 0.05) / (darkest + 0.05)
    }
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
    val cardTop = if (isDay) GlassCardDayTop else GlassCardNightTop
    val cardBot = if (isDay) GlassCardDayBottom else GlassCardNightBottom

    return when (condition) {
        WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> {
            if (isDay) {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyDayTop, ClearSkyDayMid, ClearSkyDayBottom),
                    surfaceTint = ClearSkyDaySurfaceTint,
                    textColor = Color.White,
                    accentColor = SunAccent,
                    isDay = true,
                    glassCardTopColor = cardTop,
                    glassCardBottomColor = cardBot,
                    heroGlowColor = SunAccent
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyNightTop, ClearSkyNightMid, ClearSkyNightBottom),
                    surfaceTint = ClearSkyNightSurfaceTint,
                    textColor = Color(0xFFF8FAFC),
                    accentColor = MoonAccent,
                    isDay = false,
                    glassCardTopColor = cardTop,
                    glassCardBottomColor = cardBot,
                    heroGlowColor = Color(0xFF6366F1)
                )
            }
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            if (!isDay || isDark) {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyNightTop, OvercastDarkMid, OvercastDarkBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color(0xFFF8FAFC),
                    accentColor = MoonAccent,
                    isDay = isDay,
                    glassCardTopColor = GlassCardNightTop,
                    glassCardBottomColor = GlassCardNightBottom,
                    heroGlowColor = MoonAccent
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(ClearSkyDayTop, ClearSkyDayMid, OvercastLightBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color.White,
                    accentColor = SunAccent,
                    isDay = true,
                    glassCardTopColor = cardTop,
                    glassCardBottomColor = cardBot,
                    heroGlowColor = SunAccent
                )
            }
        }
        WeatherCondition.OVERCAST -> {
            if (!isDay || isDark) {
                WeatherPalette(
                    gradientColors = listOf(OvercastDarkTop, OvercastDarkMid, OvercastDarkBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color(0xFFF8FAFC),
                    accentColor = Color(0xFF7DD3FC),
                    isDay = isDay,
                    glassCardTopColor = GlassCardNightTop,
                    glassCardBottomColor = GlassCardNightBottom,
                    heroGlowColor = Color(0xFF38BDF8)
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(OvercastLightTop, OvercastLightMid, OvercastLightBottom),
                    surfaceTint = OvercastSurfaceTint,
                    textColor = Color.White,
                    accentColor = Color(0xFF7DD3FC),
                    isDay = true,
                    glassCardTopColor = cardTop,
                    glassCardBottomColor = cardBot,
                    heroGlowColor = Color(0xFF38BDF8)
                )
            }
        }
        WeatherCondition.FOG -> {
            WeatherPalette(
                gradientColors = listOf(FogTop, FogMid, FogBottom),
                surfaceTint = OvercastSurfaceTint,
                textColor = Color.White,
                accentColor = Color(0xFF93C5FD),
                isDay = isDay,
                glassCardTopColor = cardTop,
                glassCardBottomColor = cardBot,
                heroGlowColor = Color(0xFF60A5FA)
            )
        }
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> {
            WeatherPalette(
                gradientColors = listOf(RainTop, RainMid, RainBottom),
                surfaceTint = RainSurfaceTint,
                textColor = Color.White,
                accentColor = RainAccent,
                isDay = isDay,
                glassCardTopColor = Color(0xA616508C),
                glassCardBottomColor = Color(0xBF0E2C58),
                heroGlowColor = RainAccent
            )
        }
        WeatherCondition.THUNDERSTORM -> {
            WeatherPalette(
                gradientColors = listOf(ThunderstormTop, ThunderstormMid, ThunderstormBottom),
                surfaceTint = ThunderstormSurfaceTint,
                textColor = Color(0xFFF8FAFC),
                accentColor = LightningAccent,
                isDay = isDay,
                glassCardTopColor = Color(0xAD2E286E),
                glassCardBottomColor = Color(0xC4161442),
                heroGlowColor = LightningAccent
            )
        }
        WeatherCondition.SNOW -> {
            if (isDark || !isDay) {
                WeatherPalette(
                    gradientColors = listOf(SnowDarkTop, SnowDarkMid, SnowDarkBottom),
                    surfaceTint = SnowSurfaceTint,
                    textColor = Color.White,
                    accentColor = Color(0xFFE0F2FE),
                    isDay = isDay,
                    glassCardTopColor = GlassCardNightTop,
                    glassCardBottomColor = GlassCardNightBottom,
                    heroGlowColor = Color(0xFF7DD3FC)
                )
            } else {
                WeatherPalette(
                    gradientColors = listOf(SnowLightTop, SnowLightMid, SnowLightBottom),
                    surfaceTint = SnowSurfaceTint,
                    textColor = Color.White,
                    accentColor = Color(0xFFE0F2FE),
                    isDay = true,
                    glassCardTopColor = cardTop,
                    glassCardBottomColor = cardBot,
                    heroGlowColor = Color(0xFF7DD3FC)
                )
            }
        }
        WeatherCondition.UNKNOWN -> {
            WeatherPalette(
                gradientColors = if (isDay) {
                    listOf(OvercastLightTop, OvercastLightMid, OvercastLightBottom)
                } else {
                    listOf(OvercastDarkTop, OvercastDarkMid, OvercastDarkBottom)
                },
                surfaceTint = OvercastSurfaceTint,
                textColor = Color.White,
                accentColor = Color(0xFF7DD3FC),
                isDay = isDay,
                glassCardTopColor = cardTop,
                glassCardBottomColor = cardBot,
                heroGlowColor = Color(0xFF38BDF8)
            )
        }
    }
}

@Composable
fun WeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    condition: WeatherCondition = WeatherCondition.CLEAR_SKY,
    isDay: Boolean = true,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme || !isDay) DarkColorScheme else LightColorScheme
    val weatherPalette = weatherPaletteFor(condition, isDark = darkTheme, isDay = isDay)

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
