package com.weather.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.GlassCard
import com.weather.app.ui.components.GlassPillBadge
import com.weather.app.ui.components.WeatherIllustration
import com.weather.app.ui.theme.LocalReducedMotion
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.theme.PressureUnit
import com.weather.app.ui.theme.TemperatureUnit
import com.weather.app.ui.theme.WeatherUnitPreferences
import com.weather.app.ui.theme.WindSpeedUnit
import java.util.Locale

/**
 * Окремий екран налаштувань (SettingsScreen — Sapphire Atmospheric Glass v2, Макет 05).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 4, 7, 9):
 * - Робочі інтерактивні сегментовані перемикачі одиниць (`°C | °F`, `км/год | м/с`, `гПа | мм рт.ст.`)
 *   зі збереженням у SharedPreferences та миттєвим оновленням усіх екранів;
 * - Повністю прибрано внутрішню технічну інформацію та розробницький жаргон (`API v2 / 384 год`,
 *   `ACCESS_BACKGROUND_LOCATION`, `THIRD_PARTY.md`, `$0 Budget`, `Phase 2 Foundation`);
 * - Чисті користувацькі секції у єдиному сапфіровому скляному стилі.
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalWeatherPalette.current
    val units = LocalWeatherUnits.current
    val isReducedMotion = LocalReducedMotion.current
    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val currentLocale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val currentLanguageLabel = when (currentLocale.language) {
        "uk" -> stringResource(R.string.lang_uk)
        "pl" -> stringResource(R.string.lang_pl)
        "de" -> stringResource(R.string.lang_de)
        "es" -> stringResource(R.string.lang_es)
        "it" -> stringResource(R.string.lang_it)
        "ru" -> stringResource(R.string.lang_ru)
        "en" -> stringResource(R.string.lang_en)
        else -> stringResource(R.string.lang_uk)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Шапка екрана налаштувань
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassCard(
                cornerRadius = 24.dp,
                topColor = Color(0x9E2D5CA8),
                bottomColor = Color(0xB5163062),
                onClick = onBackClick,
                modifier = Modifier
                    .size(46.dp)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics {
                        contentDescription = "Повернутися назад"
                        role = Role.Button
                    }
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Налаштування",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = palette.textColor
                )
                Text(
                    text = "Одиниці вимірювання, мова та вигляд",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color(0xFFBAD6F8)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Секція 1: ОДИНИЦІ ВИМІРЮВАННЯ
        SettingsSectionHeader(title = "ОДИНИЦІ ВИМІРЮВАННЯ")
        GlassCard(
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                SegmentedUnitSettingRow(
                    title = "Температура",
                    subtitle = "Відображення градусів у прогнозі та віджетах",
                    option1Label = "°C",
                    option2Label = "°F",
                    isOption1Selected = units.temperatureUnit == TemperatureUnit.CELSIUS,
                    onSelectOption1 = {
                        WeatherUnitPreferences.setTemperatureUnit(context, TemperatureUnit.CELSIUS)
                    },
                    onSelectOption2 = {
                        WeatherUnitPreferences.setTemperatureUnit(context, TemperatureUnit.FAHRENHEIT)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                )

                SegmentedUnitSettingRow(
                    title = "Швидкість вітру",
                    subtitle = "Одиниці вимірювання вітру та поривів",
                    option1Label = stringResource(R.string.wind_unit_kmh),
                    option2Label = stringResource(R.string.wind_unit_ms),
                    isOption1Selected = units.windSpeedUnit == WindSpeedUnit.KMH,
                    onSelectOption1 = {
                        WeatherUnitPreferences.setWindSpeedUnit(context, WindSpeedUnit.KMH)
                    },
                    onSelectOption2 = {
                        WeatherUnitPreferences.setWindSpeedUnit(context, WindSpeedUnit.MS)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                )

                SegmentedUnitSettingRow(
                    title = "Атмосферний тиск",
                    subtitle = "Шкала барометричного тиску",
                    option1Label = stringResource(R.string.pressure_unit_hpa),
                    option2Label = stringResource(R.string.pressure_unit_mmhg),
                    isOption1Selected = units.pressureUnit == PressureUnit.HPA,
                    onSelectOption1 = {
                        WeatherUnitPreferences.setPressureUnit(context, PressureUnit.HPA)
                    },
                    onSelectOption2 = {
                        WeatherUnitPreferences.setPressureUnit(context, PressureUnit.MMHG)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Секція 2: ІНТЕРФЕЙС ТА ОФОРМЛЕННЯ
        SettingsSectionHeader(title = "ІНТЕРФЕЙС ТА ОФОРМЛЕННЯ")
        GlassCard(
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLanguageClick)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.select_language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Українська, Polski, English, Deutsch та інші",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = Color(0xFFBAD6F8)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2563EB))
                            .border(1.dp, Color(0xA67DD3FC), RoundedCornerShape(16.dp))
                            .clickable(onClick = onLanguageClick)
                            .sizeIn(minHeight = 42.dp)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentLanguageLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Плавні погодні анімації",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Автоматично враховує системне зменшення руху",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = Color(0xFFBAD6F8)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    GlassPillBadge(
                        cornerRadius = 14.dp,
                        fillColor = Color(0xE00E3A76),
                        borderColor = Color(0xA638BDF8)
                    ) {
                        Text(
                            text = if (isReducedMotion) "Зменшено" else "Увімкнено",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFBAE6FD),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Секція 3: ВІДЖЕТИ ТА ГЕОЛОКАЦІЯ
        SettingsSectionHeader(title = "ВІДЖЕТИ ТА ГЕОЛОКАЦІЯ")
        GlassCard(
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Домашні віджети погоди (2×2 та 4×2)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Енергоощадне оновлення прогнозу для обраного міста",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = Color(0xFFBAD6F8)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                )

                Text(
                    text = "Доступ до місцезнаходження",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Лише за вашим натисканням кнопки «Моє місце»",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = Color(0xFFBAE6FD)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Секція 4: ПРО ЗАСТОСУНОК (Чистий користувацький блок без технічного жаргону)
        SettingsSectionHeader(title = "ПРО ЗАСТОСУНОК")
        GlassCard(
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherIllustration(
                        condition = WeatherCondition.PARTLY_CLOUDY,
                        isDay = true,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${stringResource(R.string.app_name)} — Smart Forecast",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Версія 1.0 • Метеодані: WeatherMetro",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = Color(0xFFBAD6F8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CleanAboutLinkRow(title = "Джерело даних: WeatherMetro (weathermetro.com)")
                CleanAboutLinkRow(title = "Політика конфіденційності")
                CleanAboutLinkRow(title = "Ліцензії відкритого програмного забезпечення")
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        color = Color(0xFF7DD3FC),
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SegmentedUnitSettingRow(
    title: String,
    subtitle: String,
    option1Label: String,
    option2Label: String,
    isOption1Selected: Boolean,
    onSelectOption1: () -> Unit,
    onSelectOption2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = Color(0xFFBAD6F8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Інтерактивний двосегментний перемикач-капсула
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xF50A1632))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentPillOption(
                label = option1Label,
                isSelected = isOption1Selected,
                onClick = onSelectOption1
            )
            SegmentPillOption(
                label = option2Label,
                isSelected = !isOption1Selected,
                onClick = onSelectOption2
            )
        }
    }
}

@Composable
private fun SegmentPillOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(Color(0xFF2563EB))
                        .border(1.dp, Color(0xB37DD3FC), shape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .sizeIn(minWidth = 76.dp, minHeight = 38.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (isSelected) Color.White else Color(0xFFBAD6F8),
            maxLines = 1
        )
    }
}

@Composable
private fun CleanAboutLinkRow(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.11f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFFE0F2FE),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF7DD3FC),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
