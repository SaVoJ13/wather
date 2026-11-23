package com.weather.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.LocationModel
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Вкладка 3: «Місця» (LocationsScreen) згідно з USER_FLOWS.md та UI_STATE_CONTRACT.md.
 * - Управління списком збережених міст (до 10 міст);
 * - Швидкий вибір активної локації;
 * - Додавання нових міст через пошук;
 * - Оновлення поточної геопозиції за запитом GPS;
 * - Повна відсутність симуляції псевдорадара в MVP (No Fake Data Policy).
 */
@Composable
fun LocationsScreen(
    currentLocation: LocationModel,
    savedCities: List<LocationModel>,
    onCitySelected: (LocationModel) -> Unit,
    onCityRemoved: (LocationModel) -> Unit,
    onSearchClick: () -> Unit,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Панель дій: Пошук та GPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSearchClick,
                colors = ButtonDefaults.buttonColors(containerColor = palette.surfaceTint),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = palette.textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.search_city_action),
                    color = palette.textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Button(
                onClick = onGpsClick,
                colors = ButtonDefaults.buttonColors(containerColor = palette.surfaceTint),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .semantics { contentDescription = "Визначити поточне місцезнаходження за GPS" }
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = palette.textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок збережених міст
        Text(
            text = "${stringResource(R.string.selected_cities)} (${savedCities.size}/10)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (savedCities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_selected_cities),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textColor.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedCities, key = { "${it.id}_${it.latitude}_${it.longitude}" }) { city ->
                    val isSelected = city.name.equals(currentLocation.name, ignoreCase = true) ||
                            (city.id != 0L && city.id == currentLocation.id)

                    SavedCityItem(
                        city = city,
                        isSelected = isSelected,
                        onSelect = { onCitySelected(city) },
                        onRemove = { onCityRemoved(city) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Відступ під рекламний банер та NavigationBar
                }
            }
        }
    }
}

@Composable
private fun SavedCityItem(
    city: LocationModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val palette = LocalWeatherPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0x4D0F172A) else palette.surfaceTint)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (city.isCurrentLocation) Icons.Default.MyLocation else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelected) palette.accentColor else palette.textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = palette.textColor
                )
                val subtitle = buildString {
                    city.admin1?.takeIf { it.isNotBlank() }?.let { append(it).append(", ") }
                    append(city.country)
                }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Видалити ${city.name} зі збережених" }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = palette.textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
