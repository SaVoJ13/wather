package com.weather.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.weather.app.R
import com.weather.app.domain.model.LocationModel
import com.weather.app.ui.theme.LocalWeatherPalette
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeatherTopBar(
    location: LocationModel,
    currentTime: LocalDateTime,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null
) {
    val palette = LocalWeatherPalette.current
    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM • HH:mm", currentLocale)
    val formattedDate = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase(currentLocale) }

    val cityTitle = if (location.country.isNotBlank() && !location.name.contains(location.country, ignoreCase = true)) {
        "${location.name}, ${location.country}"
    } else {
        location.name
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ліва скляна кнопка пошуку (48x48dp)
        GlassIconButton(
            icon = Icons.Default.Search,
            contentDescription = stringResource(R.string.search_city_action),
            onClick = onSearchClick
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Назва міста та поточна дата/час локації
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onSearchClick)
                .padding(vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (location.isCurrentLocation) Icons.Default.NearMe else Icons.Default.LocationOn,
                    contentDescription = if (location.isCurrentLocation) {
                        stringResource(R.string.location_permission_needed)
                    } else {
                        stringResource(R.string.city_selection)
                    },
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cityTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = palette.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Color(0xFFBAD6F8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, top = 1.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Праві скляні кнопки: GPS за запитом та Налаштування
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = stringResource(R.string.location_permission_needed),
                tint = Color(0xFF38BDF8),
                onClick = onLocationClick
            )

            if (onSettingsClick != null) {
                GlassIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Відкрити екран налаштувань",
                    onClick = onSettingsClick
                )
            } else {
                GlassIconButton(
                    icon = Icons.Default.Language,
                    contentDescription = stringResource(R.string.select_language),
                    onClick = onLanguageClick
                )
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = Color(0xFFE2E8F0)
) {
    GlassCard(
        cornerRadius = 24.dp,
        topColor = Color(0x9E2D5CA8),
        bottomColor = Color(0xB5163062),
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
    ) {
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}
