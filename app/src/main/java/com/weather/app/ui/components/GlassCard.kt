package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weather.app.ui.theme.GlassActiveBorderBottom
import com.weather.app.ui.theme.GlassActiveBorderTop
import com.weather.app.ui.theme.GlassPillBorder
import com.weather.app.ui.theme.GlassPillNavyFill
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Базовий контейнер скульптурного сапфірового скла (Sapphire Atmospheric Glass v2).
 *
 * Відтворює візуальну естетику затверджених PNG-макетів v2:
 * - діагональний скляний градієнт 135° (від освітленого сапфірового кута до глибини);
 * - опціональне внутрішнє радіальне свічення (glowColor) для Hero та атмосферних карток міст;
 * - тонка спекулярна контурна рамка 1dp (світліша на верхній/лівій грані та блакитна знизу).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    topColor: Color? = null,
    bottomColor: Color? = null,
    borderTopColor: Color? = null,
    borderBottomColor: Color? = null,
    glowColor: Color? = null,
    glowAlignmentFactorX: Float = 0.75f,
    glowAlignmentFactorY: Float = 0.44f,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalWeatherPalette.current
    val resolvedTop = topColor ?: palette.glassCardTopColor
    val resolvedBottom = bottomColor ?: palette.glassCardBottomColor
    val resolvedBorderTop = borderTopColor ?: if (isHighlighted) GlassActiveBorderTop else palette.glassBorderTopColor
    val resolvedBorderBottom = borderBottomColor ?: if (isHighlighted) GlassActiveBorderBottom else palette.glassBorderBottomColor

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                // 1. Основний діагональний скляний градієнт 135°
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(resolvedTop, resolvedBottom),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )

                // 2. Верхній лівий діагональний відблиск скла (specular sheen)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.11f),
                            Color.Transparent
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.65f, size.height * 0.55f)
                    )
                )

                // 3. Опціональне радіальне атмосферне світіння всередині картки
                if (glowColor != null) {
                    val radius = (size.minDimension * 0.92f).coerceAtLeast(10f)
                    val center = Offset(
                        x = size.width * glowAlignmentFactorX,
                        y = size.height * glowAlignmentFactorY
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.28f),
                                glowColor.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }
            .border(
                width = if (isHighlighted) 1.4.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(resolvedBorderTop, resolvedBorderBottom),
                    start = Offset.Zero,
                    end = Offset(400f, 400f)
                ),
                shape = shape
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

/**
 * Висококонтрастна темно-сапфірова капсула-пігулка для бейджів стану, чіпів Hero та вологості/вітру.
 */
@Composable
fun GlassPillBadge(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    fillColor: Color = GlassPillNavyFill,
    borderColor: Color = GlassPillBorder,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(fillColor)
            .border(width = 1.dp, color = borderColor, shape = shape),
        content = content
    )
}
