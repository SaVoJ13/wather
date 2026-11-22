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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.ui.theme.LocalWeatherPalette

/**
 * Окремий екран налаштувань (SettingsScreen) згідно з UI_SPECIFICATION.md та USER_FLOWS.md.
 * Доступний через іконку шестерні у TopBar на всіх трьох вкладках.
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        // Шапка екрана налаштувань
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Повернутися назад" }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = palette.textColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Налаштування",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.textColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Категорія 1: Одиниці вимірювання
        SettingsCategoryHeader(title = "Одиниці вимірювання")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Thermostat,
                title = "Одиниця температури",
                value = "°C (Цельсій)"
            )
            SettingsItemRow(
                icon = Icons.Default.Speed,
                title = "Одиниця швидкості вітру",
                value = "км/год"
            )
            SettingsItemRow(
                icon = Icons.Default.Speed,
                title = "Одиниця тиску",
                value = "гПа (hPa)"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Категорія 2: Мова та локалізація
        SettingsCategoryHeader(title = "Мова інтерфейсу")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.select_language),
                value = "8 підтримуваних мов",
                onClick = onLanguageClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Категорія 3: Джерела даних та ліцензії
        SettingsCategoryHeader(title = "Дані, конфіденційність та правові умови")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "Основний постачальник погоди",
                value = "WeatherMetro Free (HatchSquare ToS)"
            )
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "Некомерційне dev-джерело",
                value = "Open-Meteo Free (CC BY 4.0)"
            )
            SettingsItemRow(
                icon = Icons.Default.Policy,
                title = "Політика приватності",
                value = "Zero Tracking • Фоновий GPS суворо вимкнено"
            )
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "Погодний радар",
                value = "Виключено з MVP (No Fake Data Policy)"
            )
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "Версія продукту",
                value = "1.0.0 (Phase 2 Foundation • $0 Budget)"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    val palette = LocalWeatherPalette.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        color = palette.textColor.copy(alpha = 0.8f),
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    val palette = LocalWeatherPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceTint)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = palette.textColor
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
