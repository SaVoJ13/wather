package com.weather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val currentLocale = Locale.getDefault()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", currentLocale)
    val formattedDate = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase(currentLocale) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_city_action),
                    tint = palette.textColor
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSearchClick() }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (location.isCurrentLocation) Icons.Default.NearMe else Icons.Default.LocationOn,
                        contentDescription = if (location.isCurrentLocation) {
                            stringResource(R.string.location_permission_needed)
                        } else {
                            stringResource(R.string.city_selection)
                        },
                        tint = palette.textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textColor.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLanguageClick,
                    modifier = Modifier.sizeIn(minWidth = 44.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = stringResource(R.string.select_language),
                        tint = palette.textColor
                    )
                }

                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier.sizeIn(minWidth = 44.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.location_permission_needed),
                        tint = palette.textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Бейдж джерела даних згідно з вимогами ліцензії CC BY 4.0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.surfaceTint)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.source_open_meteo),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = palette.textColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}
