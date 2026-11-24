package com.weather.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Рекламний контейнер Google AdMob Anchored Adaptive Banner згідно з ADR-005, ADR-011, ADR-019 (v2).
 *
 * Відповідно до директиви Власника (Stage B — Requirement 5 & 12):
 * - Повністю вилучено будь-яку декоративну кнопку «Відкрити»;
 * - Стриманий напівпрозорий скляний резервований контейнер фіксованої висоти (50–60dp, Zero CLS);
 * - Сувора відсутність бойових рекламних ID на цьому етапі;
 * - 0% реклами у домашньому віджеті Glance.
 */
@Composable
fun AdBannerPlaceholder(
    modifier: Modifier = Modifier,
    bannerHeight: Dp = 56.dp
) {
    val palette = LocalWeatherPalette.current

    GlassCard(
        cornerRadius = 16.dp,
        topColor = Color(0xB3101E3E),
        bottomColor = Color(0xC70B162E),
        borderTopColor = Color(0x3894A3B8),
        borderBottomColor = Color(0x2038BDF8),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(bannerHeight.coerceIn(50.dp, 90.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            GlassPillBadge(
                cornerRadius = 7.dp,
                fillColor = Color(0xDC1E3A6C),
                borderColor = Color(0x607DD3FC),
                modifier = Modifier.padding(end = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.ad_label),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFBAE6FD),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ad_slot_test),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.textColor.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Adaptive 50–60dp • Zero CLS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
