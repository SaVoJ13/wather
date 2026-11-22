package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Рекламний контейнер Google AdMob Anchored Adaptive Banner згідно з ADR-005, ADR-011 та WIDGET_AD_CONTRACTS.md.
 * 
 * Ключові властивості:
 * - Попереднє резервування слота динамічної висоти (50–90dp) для гарантії Zero CLS (нульового зсуву макета);
 * - Візуальне маркування тестового слота (AdMob Test Ad Unit);
 * - Сувора відсутність бойових рекламних ID на етапі Phase 2;
 * - 0% реклами у домашньому віджеті Glance.
 */
@Composable
fun AdBannerPlaceholder(
    modifier: Modifier = Modifier,
    bannerHeight: Dp = 60.dp
) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Обов'язкова мікроплашка Google AdMob
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ad_label),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Контейнер адаптивного банера із зарезервованою висотою (Zero CLS)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight.coerceIn(50.dp, 90.dp))
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topEnd = 12.dp))
                .background(palette.surfaceTint)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = palette.textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ad_slot_test),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
