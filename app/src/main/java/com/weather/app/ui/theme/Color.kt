package com.weather.app.ui.theme

import androidx.compose.ui.graphics.Color

// Базові токени Material 3 - Світла тема
val MdLightBackground = Color(0xFFF8F9FA)
val MdLightOnBackground = Color(0xFF1A1C1E)
val MdLightSurface = Color(0xFFFFFFFF)
val MdLightOnSurface = Color(0xFF1A1C1E)
val MdLightSurfaceVariant = Color(0xFFE1E2EC)
val MdLightOnSurfaceVariant = Color(0xFF44474F)
val MdLightPrimary = Color(0xFF0061A4)
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightError = Color(0xFFBA1A1A)
val MdLightOutline = Color(0xFF74777F)

// Базові токени Material 3 - Темна тема
val MdDarkBackground = Color(0xFF101317)
val MdDarkOnBackground = Color(0xFFE2E2E6)
val MdDarkSurface = Color(0xFF1A1D23)
val MdDarkOnSurface = Color(0xFFE2E2E6)
val MdDarkSurfaceVariant = Color(0xFF2B2F38)
val MdDarkOnSurfaceVariant = Color(0xFFC4C6D0)
val MdDarkPrimary = Color(0xFF9ECAFF)
val MdDarkOnPrimary = Color(0xFF003258)
val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOutline = Color(0xFF8E9099)

// Атмосферні погодні градієнти та палітри
// 1. Денне ясне небо
val ClearSkyDayTop = Color(0xFF4A90E2)
val ClearSkyDayBottom = Color(0xFF87CEEB)
val ClearSkyDaySurfaceTint = Color(0x33FFFFFF) // 20% білого
val SunAccent = Color(0xFFFFB300)

// 2. Нічне ясне небо
val ClearSkyNightTop = Color(0xFF0B132B)
val ClearSkyNightBottom = Color(0xFF1C2541)
val ClearSkyNightSurfaceTint = Color(0x14FFFFFF) // 8% білого
val MoonAccent = Color(0xFFA0C4E2)

// 3. Хмарно / Переважно хмарно
val OvercastLightTop = Color(0xFF708090)
val OvercastLightBottom = Color(0xFFA4B0BE)
val OvercastDarkTop = Color(0xFF1E272E)
val OvercastDarkBottom = Color(0xFF2F3542)
val OvercastSurfaceTint = Color(0x1FFFFFFF) // 12% білого

// 4. Дощ та злива
val RainTop = Color(0xFF37474F)
val RainBottom = Color(0xFF546E7A)
val RainSurfaceTint = Color(0x1AFFFFFF) // 10% білого
val RainAccent = Color(0xFF4FC3F7)

// 5. Гроза
val ThunderstormTop = Color(0xFF1A1A2E)
val ThunderstormBottom = Color(0xFF16213E)
val ThunderstormSurfaceTint = Color(0x14FFFFFF)
val LightningAccent = Color(0xFFFFD54F)

// 6. Сніг
val SnowLightTop = Color(0xFF78909C)
val SnowLightBottom = Color(0xFFB0BEC5)
val SnowDarkTop = Color(0xFF263238)
val SnowDarkBottom = Color(0xFF37474F)
val SnowSurfaceTint = Color(0x26FFFFFF) // 15% білого

// 7. Туман
val FogTop = Color(0xFF5A6B7C)
val FogBottom = Color(0xFF8898AA)

// Температурний температурний бар
val TempBarCold = Color(0xFF42A5F5)
val TempBarMild = Color(0xFF26A69A)
val TempBarModerate = Color(0xFF66BB6A)
val TempBarWarm = Color(0xFFFFA726)
val TempBarHot = Color(0xFFFF7043)
val TempBarTrack = Color(0x33FFFFFF)

// Індикатор застарілості (Stale badge)
val StaleBadgeBackground = Color(0xFFFFB74D)
val StaleBadgeText = Color(0xFF3E2723)
