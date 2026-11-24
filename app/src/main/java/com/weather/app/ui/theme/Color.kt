package com.weather.app.ui.theme

import androidx.compose.ui.graphics.Color

// Базові токени Material 3 - Світла тема
val MdLightBackground = Color(0xFF0E2A5C)
val MdLightOnBackground = Color(0xFFFFFFFF)
val MdLightSurface = Color(0xFF153878)
val MdLightOnSurface = Color(0xFFFFFFFF)
val MdLightSurfaceVariant = Color(0xFF1E4994)
val MdLightOnSurfaceVariant = Color(0xFFE2E8F0)
val MdLightPrimary = Color(0xFF38BDF8)
val MdLightOnPrimary = Color(0xFF0A1931)
val MdLightError = Color(0xFFFFB4AB)
val MdLightOutline = Color(0xFF64748B)

// Базові токени Material 3 - Темна тема
val MdDarkBackground = Color(0xFF070D1F)
val MdDarkOnBackground = Color(0xFFF8FAFC)
val MdDarkSurface = Color(0xFF0F1C3F)
val MdDarkOnSurface = Color(0xFFF8FAFC)
val MdDarkSurfaceVariant = Color(0xFF172B5C)
val MdDarkOnSurfaceVariant = Color(0xFFCBD5E1)
val MdDarkPrimary = Color(0xFF38BDF8)
val MdDarkOnPrimary = Color(0xFF071326)
val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOutline = Color(0xFF64748B)

// ============================================================================
// SAPPHIRE ATMOSPHERIC GLASS v2 — АТМОСФЕРНІ ПОГОДНІ ГРАДІЄНТИ НЕБА
// Жодних брудно-сірих фонів (#708090): кожна погодна атмосфера має глибоку
// багатошарову сапфірово-кобальтову або нічну індиго-палітру.
// ============================================================================

// 1. Денне ясне сапфірове небо (Deep Sapphire Day)
val ClearSkyDayTop = Color(0xFF0B2454)
val ClearSkyDayMid = Color(0xFF17489C)
val ClearSkyDayBottom = Color(0xFF2362C2)
val ClearSkyDaySurfaceTint = Color(0x2E2E62B6)
val SunAccent = Color(0xFFFFC426)

// 2. Нічне зоряне небо (Midnight Indigo Night)
val ClearSkyNightTop = Color(0xFF060B1C)
val ClearSkyNightMid = Color(0xFF0E1B40)
val ClearSkyNightBottom = Color(0xFF162B5E)
val ClearSkyNightSurfaceTint = Color(0x331A3066)
val MoonAccent = Color(0xFF7DD3FC)

// 3. Хмарно / Мінлива хмарність (Royal Cobalt Overcast — заміна сірого)
val OvercastLightTop = Color(0xFF0D285C)
val OvercastLightMid = Color(0xFF19428A)
val OvercastLightBottom = Color(0xFF2559B0)
val OvercastDarkTop = Color(0xFF08122B)
val OvercastDarkMid = Color(0xFF11224D)
val OvercastDarkBottom = Color(0xFF1B336E)
val OvercastSurfaceTint = Color(0x3322488C)

// 4. Дощ, мряка та злива (Deep Aqua-Cobalt Rain)
val RainTop = Color(0xFF0A2046)
val RainMid = Color(0xFF133A73)
val RainBottom = Color(0xFF1B4F94)
val RainSurfaceTint = Color(0x33164C88)
val RainAccent = Color(0xFF38BDF8)

// 5. Гроза (Electric Violet-Indigo Thunderstorm)
val ThunderstormTop = Color(0xFF090E26)
val ThunderstormMid = Color(0xFF191D52)
val ThunderstormBottom = Color(0xFF282B73)
val ThunderstormSurfaceTint = Color(0x38282468)
val LightningAccent = Color(0xFFFACC15)

// 6. Сніг (Winter Crystal Sapphire)
val SnowLightTop = Color(0xFF102E63)
val SnowLightMid = Color(0xFF1E4C96)
val SnowLightBottom = Color(0xFF306AC2)
val SnowDarkTop = Color(0xFF091633)
val SnowDarkMid = Color(0xFF132A59)
val SnowDarkBottom = Color(0xFF1D3E7D)
val SnowSurfaceTint = Color(0x3326549E)

// 7. Туман і серпанок (Mystic Cobalt Fog)
val FogTop = Color(0xFF0F2652)
val FogMid = Color(0xFF1B3E7D)
val FogBottom = Color(0xFF2956A3)

// ============================================================================
// ТОКЕНИ СКЛЯНИХ КАРТОК (SCULPTED GLASSMORPHISM 135° & SPECULAR BORDERS)
// ============================================================================
val GlassCardDayTop = Color(0x9E265096)
val GlassCardDayBottom = Color(0xB5112654)
val GlassCardNightTop = Color(0xA61A3064)
val GlassCardNightBottom = Color(0xBF0D183A)

val GlassSpecularBorderTop = Color(0x78FFFFFF)
val GlassSpecularBorderBottom = Color(0x3838BDF8)
val GlassActiveBorderTop = Color(0xCC7DD3FC)
val GlassActiveBorderBottom = Color(0x8038BDF8)

val GlassPillNavyFill = Color(0xD90C1C40)
val GlassPillActiveFill = Color(0xE61C448E)
val GlassPillBorder = Color(0x8838BDF8)

// Палітри атмосферних карток міст (LocationsScreen v2)
val CityCardSunnyTop = Color(0xBF3472D2)
val CityCardSunnyBottom = Color(0xD4183C80)
val CityCardSunnyGlow = Color(0xFFFFC42D)

val CityCardRainTop = Color(0xBF165894)
val CityCardRainBottom = Color(0xD40E305C)
val CityCardRainGlow = Color(0xFF38BDF8)

val CityCardNightTop = Color(0xC41A2660)
val CityCardNightBottom = Color(0xDC0C1236)
val CityCardNightGlow = Color(0xFF818CF8)

val CityCardStormTop = Color(0xC4302A70)
val CityCardStormBottom = Color(0xDC161440)
val CityCardStormGlow = Color(0xFFFACC15)

val CityCardCloudyTop = Color(0xBF224A94)
val CityCardCloudyBottom = Color(0xD4122858)
val CityCardCloudyGlow = Color(0xFF60A5FA)

// Температурний тренд та шкали діапазонів (Max Coral / Min Cyan)
val TempTrendMaxCoral = Color(0xFFFB923C)
val TempTrendMinCyan = Color(0xFF38BDF8)
val TempBarCold = Color(0xFF38BDF8)
val TempBarMild = Color(0xFF2DD4BF)
val TempBarModerate = Color(0xFF4ADE80)
val TempBarWarm = Color(0xFFFBBF24)
val TempBarHot = Color(0xFFFB923C)
val TempBarTrack = Color(0xCC0A142C)

// Індикатор застарілості (Stale badge)
val StaleBadgeBackground = Color(0xFFFFB74D)
val StaleBadgeText = Color(0xFF3E2723)

// ============================================================================
// ЗАМОРОЖЕНІ ТОКЕНИ НИЖНЬОЇ ПАНЕЛІ НАВІГАЦІЇ (ADR-017 — НЕ ЗМІНЮВАТИ!)
// ============================================================================
val NavBarContainerSurface = Color(0xF50F172A)     // Темний графітово-синій фон (96% непрозорості, L ≈ 0.0085)
val NavBarTopBorder = Color(0x2EFFFFFF)            // Тонка верхня межа 18% білого для візуального відділення
val NavBarSelectedIcon = Color(0xFF38BDF8)         // Яскравий блакитний акцент (Sky-400, контраст ~9.2:1 на фоні та ~6.9:1 на індикаторі)
val NavBarSelectedText = Color(0xFFFFFFFF)         // Чистий білий текст активної вкладки (контраст ~17.9:1)
val NavBarUnselectedIcon = Color(0xFFCBD5E1)       // Чіткий сріблясто-сірий для неактивних іконок (Slate-300, контраст ~11.9:1)
val NavBarUnselectedText = Color(0xFFE2E8F0)       // Чіткий світло-сірий для неактивних підписів (Slate-200, контраст ~14.3:1)
val NavBarIndicatorPill = Color(0xFF1E3250)        // Контрастна графітово-сапфірова капсула активної вкладки
