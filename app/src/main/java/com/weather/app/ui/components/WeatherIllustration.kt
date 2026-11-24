package com.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.weather.app.domain.model.WeatherCondition
import kotlin.math.cos
import kotlin.math.sin

/**
 * Багатошарові об'ємні 3D-ілюстрації погодних явищ (Sapphire Atmospheric Glass v2).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 2 & 3):
 * - Жодної заміни на прості системні іконки;
 * - Повноцінне сферичне 3D-шейдування (дифузне освітлення, спекулярний блік, теплий рефлекс rim light);
 * - Сонце з подвійним радіальним ореолом та 12 конічними світловими променями;
 * - 5-часткові скульптурні хмари з падаючою тінню (ambient occlusion) і теплим/блакитним підсвічуванням;
 * - 3D-серп місяця з місячним сяйвом та 4-променевими іскристими зорями;
 * - Глянцеві краплі дощу зі шлейфом і білим бліком та грозова блискавка з внутрішнім світінням хмари.
 */
@Composable
fun WeatherIllustration(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    isDay: Boolean = true
) {
    val desc = stringResource(condition.stringRes)
    Canvas(
        modifier = modifier.semantics { contentDescription = desc }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = size.minDimension / 120f

        when (condition) {
            WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> {
                if (isDay) {
                    drawVolumetricSun(
                        cx = cx,
                        cy = cy,
                        r = 35f * scale,
                        withRays = true
                    )
                } else {
                    drawVolumetricMoon(
                        cx = cx,
                        cy = cy,
                        r = 34f * scale
                    )
                }
            }

            WeatherCondition.PARTLY_CLOUDY -> {
                if (isDay) {
                    drawVolumetricSun(
                        cx = cx - 22f * scale,
                        cy = cy - 17f * scale,
                        r = 31f * scale,
                        withRays = true
                    )
                    drawVolumetricCloud(
                        cx = cx + 9f * scale,
                        cy = cy + 9f * scale,
                        scale = scale * 0.96f,
                        variant = CloudVariant.DAY,
                        sunOnLeft = true
                    )
                } else {
                    drawVolumetricMoon(
                        cx = cx - 20f * scale,
                        cy = cy - 16f * scale,
                        r = 29f * scale
                    )
                    drawVolumetricCloud(
                        cx = cx + 9f * scale,
                        cy = cy + 9f * scale,
                        scale = scale * 0.96f,
                        variant = CloudVariant.NIGHT,
                        sunOnLeft = false
                    )
                }
            }

            WeatherCondition.OVERCAST, WeatherCondition.UNKNOWN -> {
                drawVolumetricCloud(
                    cx = cx - 14f * scale,
                    cy = cy - 11f * scale,
                    scale = scale * 0.76f,
                    variant = CloudVariant.NIGHT,
                    sunOnLeft = false
                )
                drawVolumetricCloud(
                    cx = cx + 7f * scale,
                    cy = cy + 5f * scale,
                    scale = scale * 0.95f,
                    variant = if (isDay) CloudVariant.DAY else CloudVariant.NIGHT,
                    sunOnLeft = false
                )
            }

            WeatherCondition.FOG -> {
                drawVolumetricCloud(
                    cx = cx,
                    cy = cy - 6f * scale,
                    scale = scale * 0.90f,
                    variant = CloudVariant.NIGHT,
                    sunOnLeft = false
                )
                drawFogBands(cx = cx, cy = cy + 25f * scale, scale = scale)
            }

            WeatherCondition.DRIZZLE -> {
                if (isDay) {
                    drawVolumetricSun(
                        cx = cx - 22f * scale,
                        cy = cy - 19f * scale,
                        r = 24f * scale,
                        withRays = true
                    )
                }
                drawVolumetricCloud(
                    cx = cx + 5f * scale,
                    cy = cy - 2f * scale,
                    scale = scale * 0.92f,
                    variant = CloudVariant.RAIN,
                    sunOnLeft = isDay
                )
                drawGlossyRaindrops(
                    cx = cx + 5f * scale,
                    cy = cy + 27f * scale,
                    scale = scale * 0.88f,
                    count = 3
                )
            }

            WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> {
                drawVolumetricCloud(
                    cx = cx - 12f * scale,
                    cy = cy - 13f * scale,
                    scale = scale * 0.74f,
                    variant = CloudVariant.STORM,
                    sunOnLeft = false
                )
                drawVolumetricCloud(
                    cx = cx + 4f * scale,
                    cy = cy - 3f * scale,
                    scale = scale * 0.94f,
                    variant = CloudVariant.RAIN,
                    sunOnLeft = false
                )
                drawGlossyRaindrops(
                    cx = cx + 2f * scale,
                    cy = cy + 26f * scale,
                    scale = scale * 0.92f,
                    count = 4
                )
            }

            WeatherCondition.THUNDERSTORM -> {
                drawVolumetricCloud(
                    cx = cx - 13f * scale,
                    cy = cy - 14f * scale,
                    scale = scale * 0.76f,
                    variant = CloudVariant.STORM,
                    sunOnLeft = false
                )
                drawVolumetricCloud(
                    cx = cx + 4f * scale,
                    cy = cy - 4f * scale,
                    scale = scale * 0.94f,
                    variant = CloudVariant.STORM,
                    sunOnLeft = false
                )
                drawElectricLightning(
                    cx = cx + 2f * scale,
                    cy = cy + 19f * scale,
                    scale = scale * 0.95f
                )
                drawGlossyRaindrops(
                    cx = cx,
                    cy = cy + 25f * scale,
                    scale = scale * 0.85f,
                    count = 3
                )
            }

            WeatherCondition.SNOW -> {
                drawVolumetricCloud(
                    cx = cx,
                    cy = cy - 6f * scale,
                    scale = scale * 0.92f,
                    variant = CloudVariant.DAY,
                    sunOnLeft = false
                )
                drawSnowCrystals(
                    cx = cx,
                    cy = cy + 26f * scale,
                    scale = scale * 0.90f
                )
            }
        }
    }
}

private enum class CloudVariant {
    DAY, NIGHT, RAIN, STORM
}

/**
 * Малює 3D-шейдовану сферу з дифузним освітленням, спекулярним бліком у верхньому лівому квадранті
 * та м'яким рефлексом (rim light) знизу.
 */
private fun DrawScope.drawShaded3DSphere(
    cx: Float,
    cy: Float,
    radius: Float,
    baseColor: Color,
    shadowColor: Color,
    highlightColor: Color,
    rimColor: Color? = null
) {
    if (radius <= 0.5f) return

    // 1. Основне сферичне тіло (радіальний градієнт зміщений до джерела світла зверху-зліва)
    val lightCenter = Offset(cx - radius * 0.32f, cy - radius * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlightColor, baseColor, shadowColor),
            center = lightCenter,
            radius = radius * 1.48f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // 2. Підповерхневий рефлекс (rim light)
    if (rimColor != null) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    rimColor.copy(alpha = 0.38f)
                ),
                center = Offset(cx - radius * 0.15f, cy - radius * 0.18f),
                radius = radius * 1.05f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )
    }

    // 3. М'який спекулярний блік
    val specCenter = Offset(cx - radius * 0.36f, cy - radius * 0.38f)
    val specRadius = radius * 0.48f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = specCenter,
            radius = specRadius
        ),
        radius = specRadius,
        center = specCenter
    )
}

private fun DrawScope.drawVolumetricSun(
    cx: Float,
    cy: Float,
    r: Float,
    withRays: Boolean
) {
    // 1. Зовнішня золота корона + внутрішнє гаряче бурштинове світіння
    val outerGlowR = (r * 2.55f).coerceAtLeast(4f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFA818).copy(alpha = 0.42f),
                Color(0xFFFFA818).copy(alpha = 0.14f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = outerGlowR
        ),
        radius = outerGlowR,
        center = Offset(cx, cy)
    )

    val innerGlowR = (r * 1.75f).coerceAtLeast(3f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFDA4B).copy(alpha = 0.58f),
                Color(0xFFFFC107).copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = innerGlowR
        ),
        radius = innerGlowR,
        center = Offset(cx, cy)
    )

    // 2. 12 конічних світлових променів
    if (withRays) {
        val numRays = 12
        for (i in 0 until numRays) {
            val ang = Math.toRadians((i * (360.0 / numRays) + 15.0))
            val perp = ang + Math.PI / 2.0
            val rIn = r * 1.05f
            val rOut = r * (if (i % 2 == 0) 1.56f else 1.36f)
            val hwIn = (r * 0.13f).coerceAtLeast(1.2f)
            val hwOut = (r * 0.03f).coerceAtLeast(0.4f)

            val rayPath = Path().apply {
                moveTo(
                    (cx + cos(ang) * rIn + cos(perp) * hwIn).toFloat(),
                    (cy + sin(ang) * rIn + sin(perp) * hwIn).toFloat()
                )
                lineTo(
                    (cx + cos(ang) * rIn - cos(perp) * hwIn).toFloat(),
                    (cy + sin(ang) * rIn - sin(perp) * hwIn).toFloat()
                )
                lineTo(
                    (cx + cos(ang) * rOut - cos(perp) * hwOut).toFloat(),
                    (cy + sin(ang) * rOut - sin(perp) * hwOut).toFloat()
                )
                lineTo(
                    (cx + cos(ang) * rOut + cos(perp) * hwOut).toFloat(),
                    (cy + sin(ang) * rOut + sin(perp) * hwOut).toFloat()
                )
                close()
            }
            drawPath(
                path = rayPath,
                color = Color(0xFFFFE066).copy(alpha = if (i % 2 == 0) 0.68f else 0.48f)
            )
        }
    }

    // 3. 3D-шейдована сонячна сфера
    drawShaded3DSphere(
        cx = cx,
        cy = cy,
        radius = r,
        baseColor = Color(0xFFFFC426),
        shadowColor = Color(0xFFEA6C0C),
        highlightColor = Color(0xFFFFFCDC),
        rimColor = Color(0xFFFFD75A)
    )
}

private fun DrawScope.drawVolumetricMoon(
    cx: Float,
    cy: Float,
    r: Float
) {
    // 1. Місячна блакитно-золотиста аура
    val outerR = (r * 2.4f).coerceAtLeast(4f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF60A5FA).copy(alpha = 0.38f),
                Color(0xFFFDE047).copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = outerR
        ),
        radius = outerR,
        center = Offset(cx, cy)
    )

    // 2. Іскристі 4-променеві зорі навколо місяця
    val stars = listOf(
        Triple(-r * 1.48f, -r * 0.74f, (r * 0.14f).coerceAtLeast(1.8f)),
        Triple(r * 1.42f, -r * 0.90f, (r * 0.17f).coerceAtLeast(2.2f)),
        Triple(r * 1.56f, r * 0.25f, (r * 0.11f).coerceAtLeast(1.4f)),
        Triple(-r * 1.25f, r * 0.62f, (r * 0.12f).coerceAtLeast(1.5f))
    )
    for ((sx, sy, sr) in stars) {
        drawSparkleStar(cx + sx, cy + sy, sr)
    }

    // 3. 3D-серп місяця через булеве віднімання PathOperation.Difference
    val outerMoon = Path().apply {
        addOval(Rect(center = Offset(cx, cy), radius = r))
    }
    val cutMoon = Path().apply {
        addOval(
            Rect(
                center = Offset(cx + r * 0.34f, cy - r * 0.26f),
                radius = r * 0.84f
            )
        )
    }
    val crescentPath = Path.combine(PathOperation.Difference, outerMoon, cutMoon)

    drawPath(
        path = crescentPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFBEB),
                Color(0xFFFADA5E),
                Color(0xFFCA8A04)
            ),
            center = Offset(cx - r * 0.35f, cy - r * 0.30f),
            radius = r * 1.45f
        )
    )
}

private fun DrawScope.drawSparkleStar(cx: Float, cy: Float, sr: Float) {
    drawCircle(
        color = Color(0xFFBAE6FD).copy(alpha = 0.30f),
        radius = sr * 2.2f,
        center = Offset(cx, cy)
    )
    val vPath = Path().apply {
        moveTo(cx, cy - sr * 2.4f)
        lineTo(cx + sr * 0.55f, cy)
        lineTo(cx, cy + sr * 2.4f)
        lineTo(cx - sr * 0.55f, cy)
        close()
    }
    val hPath = Path().apply {
        moveTo(cx - sr * 2.4f, cy)
        lineTo(cx, cy + sr * 0.55f)
        lineTo(cx + sr * 2.4f, cy)
        lineTo(cx, cy - sr * 0.55f)
        close()
    }
    drawPath(vPath, color = Color.White.copy(alpha = 0.92f))
    drawPath(hPath, color = Color.White.copy(alpha = 0.92f))
}

private fun DrawScope.drawVolumetricCloud(
    cx: Float,
    cy: Float,
    scale: Float,
    variant: CloudVariant,
    sunOnLeft: Boolean
) {
    val w = 114f * scale
    val h = 66f * scale

    // 1. Падаюча атмосферна тінь під хмарою
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF040C20).copy(alpha = if (variant == CloudVariant.STORM) 0.56f else 0.42f),
                Color.Transparent
            ),
            center = Offset(cx, cy + h * 0.36f),
            radius = (w * 0.54f).coerceAtLeast(4f)
        ),
        topLeft = Offset(cx - w * 0.50f, cy + h * 0.12f),
        size = Size(w, h * 0.45f)
    )

    val baseColor: Color
    val shadowColor: Color
    val hiColor: Color
    val rimColor: Color

    when (variant) {
        CloudVariant.DAY -> {
            baseColor = Color(0xFFE0EEFF)
            shadowColor = Color(0xFF8AAEDE)
            hiColor = Color(0xFFFFFFFF)
            rimColor = if (sunOnLeft) Color(0xFFFFE8A8) else Color(0xFF7DD3FC)
        }
        CloudVariant.NIGHT -> {
            baseColor = Color(0xFFB0CAEE)
            shadowColor = Color(0xFF5878AF)
            hiColor = Color(0xFFF0F8FF)
            rimColor = Color(0xFF7DD3FC)
        }
        CloudVariant.RAIN -> {
            baseColor = Color(0xFFA5C2E8)
            shadowColor = Color(0xFF5272A6)
            hiColor = Color(0xFFE8F4FF)
            rimColor = Color(0xFF38BDF8)
        }
        CloudVariant.STORM -> {
            baseColor = Color(0xFF7A96C6)
            shadowColor = Color(0xFF344A7A)
            hiColor = Color(0xFFCDE0FA)
            rimColor = Color(0xFFFACC15)
        }
    }

    // 2. Основа хмари (3D-капсула)
    val bellyW = 98f * scale
    val bellyH = 35f * scale
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(hiColor, baseColor, shadowColor),
            startY = cy - 4f * scale,
            endY = cy - 4f * scale + bellyH
        ),
        topLeft = Offset(cx - bellyW / 2f, cy - 4f * scale),
        size = Size(bellyW, bellyH),
        cornerRadius = CornerRadius(18f * scale, 18f * scale)
    )

    // 3. 5 скульптурних 3D-часток хмари (від заднього плану до переднього)
    val lobes = listOf(
        Triple(26f, 3f, 21.5f),   // Права частка
        Triple(-32f, 1f, 24.5f),  // Ліва частка (теплий рефлекс від сонця)
        Triple(11f, -8f, 25.5f),  // Верхня права середня частка
        Triple(-7f, -14f, 32f),   // Головна центральна корона хмари
        Triple(-2f, 6.5f, 22.5f)  // Передня нижня пухка частка
    )

    for ((ox, oy, lr) in lobes) {
        val lobeRim = if (sunOnLeft && ox < 0f && variant == CloudVariant.DAY) {
            Color(0xFFFFDE82)
        } else {
            rimColor
        }
        drawShaded3DSphere(
            cx = cx + ox * scale,
            cy = cy + oy * scale,
            radius = lr * scale,
            baseColor = baseColor,
            shadowColor = shadowColor,
            highlightColor = hiColor,
            rimColor = lobeRim
        )
    }

    // 4. Внутрішній бурштиновий спалах для грозової хмари
    if (variant == CloudVariant.STORM) {
        val glowR = (35f * scale).coerceAtLeast(3f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD740).copy(alpha = 0.42f),
                    Color.Transparent
                ),
                center = Offset(cx + 2f * scale, cy + 8f * scale),
                radius = glowR
            ),
            radius = glowR,
            center = Offset(cx + 2f * scale, cy + 8f * scale)
        )
    }
}

private fun DrawScope.drawGlossyRaindrops(
    cx: Float,
    cy: Float,
    scale: Float,
    count: Int
) {
    val offsets = if (count == 3) {
        listOf(-24f to 0f, 0f to 6.5f, 24f to -1f)
    } else {
        listOf(-31f to -1f, -10f to 6.5f, 11f to -1f, 32f to 5.5f)
    }

    for ((ox, oy) in offsets) {
        val xTop = cx + ox * scale
        val yTop = cy + oy * scale
        val xBot = xTop - 6.5f * scale
        val yBot = yTop + 15.5f * scale

        // Світловий шлейф краплі
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.36f),
            start = Offset(xTop + 2.5f * scale, yTop - 4.5f * scale),
            end = Offset(xBot, yBot),
            strokeWidth = (5.5f * scale).coerceAtLeast(1.5f),
            cap = StrokeCap.Round
        )

        // Тіло краплі
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC)),
                startY = yTop,
                endY = yBot
            ),
            start = Offset(xTop, yTop),
            end = Offset(xBot, yBot),
            strokeWidth = (4.2f * scale).coerceAtLeast(1.2f),
            cap = StrokeCap.Round
        )

        // Глянцевий білий блік на голівці краплі
        drawCircle(
            color = Color.White.copy(alpha = 0.90f),
            radius = (1.3f * scale).coerceAtLeast(0.6f),
            center = Offset(xBot - 0.8f * scale, yBot - 0.8f * scale)
        )
    }
}

private fun DrawScope.drawElectricLightning(
    cx: Float,
    cy: Float,
    scale: Float
) {
    val glowR = (38f * scale).coerceAtLeast(4f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFACC15).copy(alpha = 0.56f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = glowR
        ),
        radius = glowR,
        center = Offset(cx, cy)
    )

    val bolt = Path().apply {
        moveTo(cx + 4.5f * scale, cy - 15f * scale)
        lineTo(cx - 10.5f * scale, cy + 3f * scale)
        lineTo(cx - 1f * scale, cy + 3f * scale)
        lineTo(cx - 7.5f * scale, cy + 23f * scale)
        lineTo(cx + 13f * scale, cy + 0f * scale)
        lineTo(cx + 2.5f * scale, cy + 0f * scale)
        close()
    }
    drawPath(
        path = bolt,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFC107), Color(0xFFFF8F00)),
            startY = cy - 15f * scale,
            endY = cy + 23f * scale
        )
    )
}

private fun DrawScope.drawSnowCrystals(
    cx: Float,
    cy: Float,
    scale: Float
) {
    val positions = listOf(-24f to 2f, 0f to 8f, 24f to 2f)
    for ((ox, oy) in positions) {
        val sx = cx + ox * scale
        val sy = cy + oy * scale
        val r = (6.5f * scale).coerceAtLeast(1.8f)
        for (a in listOf(0.0, 60.0, 120.0)) {
            val rad = Math.toRadians(a)
            drawLine(
                color = Color(0xFFE0F2FE),
                start = Offset((sx - cos(rad) * r).toFloat(), (sy - sin(rad) * r).toFloat()),
                end = Offset((sx + cos(rad) * r).toFloat(), (sy + sin(rad) * r).toFloat()),
                strokeWidth = (2.2f * scale).coerceAtLeast(1f),
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = Color.White,
            radius = (2.0f * scale).coerceAtLeast(0.8f),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawFogBands(
    cx: Float,
    cy: Float,
    scale: Float
) {
    val bands = listOf(-6f to 68f, 3f to 52f, 11f to 38f)
    for ((oy, w) in bands) {
        val halfW = (w * scale) / 2f
        val y = cy + oy * scale
        drawLine(
            color = Color(0xFFBAE6FD).copy(alpha = 0.78f),
            start = Offset(cx - halfW, y),
            end = Offset(cx + halfW, y),
            strokeWidth = (4.5f * scale).coerceAtLeast(1.4f),
            cap = StrokeCap.Round
        )
    }
}
