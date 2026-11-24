package com.weather.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.app.R
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.GlassCard
import com.weather.app.ui.components.GlassPillBadge
import com.weather.app.ui.components.WeatherIllustration
import com.weather.app.ui.theme.CityCardCloudyBottom
import com.weather.app.ui.theme.CityCardCloudyGlow
import com.weather.app.ui.theme.CityCardCloudyTop
import com.weather.app.ui.theme.CityCardNightBottom
import com.weather.app.ui.theme.CityCardNightGlow
import com.weather.app.ui.theme.CityCardNightTop
import com.weather.app.ui.theme.CityCardRainBottom
import com.weather.app.ui.theme.CityCardRainGlow
import com.weather.app.ui.theme.CityCardRainTop
import com.weather.app.ui.theme.CityCardStormBottom
import com.weather.app.ui.theme.CityCardStormGlow
import com.weather.app.ui.theme.CityCardStormTop
import com.weather.app.ui.theme.CityCardSunnyBottom
import com.weather.app.ui.theme.CityCardSunnyGlow
import com.weather.app.ui.theme.CityCardSunnyTop
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.LocalWeatherUnits
import com.weather.app.ui.viewmodel.WeatherViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Вкладка 3: «Місця» (LocationsScreen — Sapphire Atmospheric Glass v2, Макет 04).
 *
 * Відповідно до вимог Власника (Stage B — Requirement 2, 3, 6, 7, 9):
 * - Кожна картка міста має власну погодну атмосферу, радіальне світіння та акцентну рамку відповідно до погоди міста;
 * - Використовує реальні дані активного міста або власний локальний кеш міста з обмеженням кількості API-запитів;
 * - Жодних вигаданих чисел: якщо місто ще завантажується і не має кешу, чесно показується індикатор завантаження/натискання;
 * - Скляне поле пошуку міста та сапфірова кнопка визначення GPS за запитом.
 */
@Composable
fun LocationsScreen(
    currentLocation: LocationModel,
    savedCities: List<LocationModel>,
    onCitySelected: (LocationModel) -> Unit,
    onCityRemoved: (LocationModel) -> Unit,
    onSearchClick: () -> Unit,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeForecast: FullWeatherForecast? = null,
    cityForecasts: Map<String, FullWeatherForecast> = emptyMap(),
    onRequestPreloadCities: () -> Unit = {}
) {
    val palette = LocalWeatherPalette.current

    LaunchedEffect(savedCities.size) {
        onRequestPreloadCities()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // 1. Скляне поле пошуку міста, села або індексу
        GlassCard(
            cornerRadius = 22.dp,
            topColor = Color(0xA32856A2),
            bottomColor = Color(0xB5142E62),
            onClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF7DD3FC),
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Сапфірова кнопка визначення місцезнаходження (GPS виключно за запитом)
        GlassCard(
            cornerRadius = 20.dp,
            topColor = Color(0xC22563EB),
            bottomColor = Color(0xD91D4ED8),
            borderTopColor = Color(0xA67DD3FC),
            borderBottomColor = Color(0x6638BDF8),
            onClick = onGpsClick,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 50.dp)
                .semantics { contentDescription = "Визначити поточне місцезнаходження за GPS" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.location_permission_needed),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Заголовок збережених міст
        Text(
            text = "${stringResource(R.string.selected_cities).uppercase()} (${savedCities.size}/10)",
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF93C5FD),
            modifier = Modifier.padding(start = 4.dp)
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = savedCities,
                    key = { _, it -> "${it.id}_${it.latitude}_${it.longitude}" }
                ) { index, city ->
                    val isSelected = isSameLocation(city, currentLocation)
                    val cityKey = WeatherViewModel.cityCacheKey(city)
                    val cityForecast = when {
                        isSelected && activeForecast != null -> activeForecast
                        else -> cityForecasts[cityKey]
                    }

                    AtmosphericCityCard(
                        city = city,
                        isSelected = isSelected,
                        forecast = cityForecast,
                        cardIndex = index,
                        canRemove = savedCities.size > 1 || !isSelected,
                        onSelect = { onCitySelected(city) },
                        onRemove = { onCityRemoved(city) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(84.dp)) // Відступ під рекламний банер та NavigationBar
                }
            }
        }
    }
}

private data class CityCardVisualTheme(
    val topColor: Color,
    val bottomColor: Color,
    val glowColor: Color,
    val borderTopColor: Color,
    val borderBottomColor: Color,
    val isDayIllustration: Boolean
)

private fun resolveCityCardTheme(
    forecast: FullWeatherForecast?,
    isSelected: Boolean,
    cardIndex: Int
): CityCardVisualTheme {
    if (forecast != null) {
        val currentHour = forecast.current.time.hour
        val today = forecast.daily14Days.firstOrNull()
        val isDay = if (today?.sunrise != null && today.sunset != null) {
            !forecast.current.time.isBefore(today.sunrise) && !forecast.current.time.isAfter(today.sunset)
        } else {
            currentHour in 6..20
        }

        return when (forecast.current.condition) {
            WeatherCondition.THUNDERSTORM -> CityCardVisualTheme(
                topColor = CityCardStormTop,
                bottomColor = CityCardStormBottom,
                glowColor = CityCardStormGlow,
                borderTopColor = Color(0x9EFDE047),
                borderBottomColor = Color(0x60A855F7),
                isDayIllustration = isDay
            )
            WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> CityCardVisualTheme(
                topColor = CityCardRainTop,
                bottomColor = CityCardRainBottom,
                glowColor = CityCardRainGlow,
                borderTopColor = Color(0x997DD3FC),
                borderBottomColor = Color(0x5E2DD4BF),
                isDayIllustration = isDay
            )
            WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> {
                if (isDay) {
                    CityCardVisualTheme(
                        topColor = CityCardSunnyTop,
                        bottomColor = CityCardSunnyBottom,
                        glowColor = CityCardSunnyGlow,
                        borderTopColor = Color(0xBAFFE082),
                        borderBottomColor = Color(0x7338BDF8),
                        isDayIllustration = true
                    )
                } else {
                    CityCardVisualTheme(
                        topColor = CityCardNightTop,
                        bottomColor = CityCardNightBottom,
                        glowColor = CityCardNightGlow,
                        borderTopColor = Color(0x94A5B4FC),
                        borderBottomColor = Color(0x596366F1),
                        isDayIllustration = false
                    )
                }
            }
            else -> {
                if (!isDay) {
                    CityCardVisualTheme(
                        topColor = CityCardNightTop,
                        bottomColor = CityCardNightBottom,
                        glowColor = CityCardNightGlow,
                        borderTopColor = Color(0x94A5B4FC),
                        borderBottomColor = Color(0x596366F1),
                        isDayIllustration = false
                    )
                } else if (isSelected) {
                    CityCardVisualTheme(
                        topColor = CityCardSunnyTop,
                        bottomColor = CityCardSunnyBottom,
                        glowColor = CityCardSunnyGlow,
                        borderTopColor = Color(0xBAFFE082),
                        borderBottomColor = Color(0x7338BDF8),
                        isDayIllustration = true
                    )
                } else {
                    CityCardVisualTheme(
                        topColor = CityCardCloudyTop,
                        bottomColor = CityCardCloudyBottom,
                        glowColor = CityCardCloudyGlow,
                        borderTopColor = Color(0x8893C5FD),
                        borderBottomColor = Color(0x4D38BDF8),
                        isDayIllustration = true
                    )
                }
            }
        }
    }

    // Коли кеш міста ще завантажується, надаємо картці індивідуальний сапфіровий відтінок за індексом
    return when (cardIndex % 4) {
        0 -> CityCardVisualTheme(CityCardSunnyTop, CityCardSunnyBottom, CityCardSunnyGlow, Color(0x997DD3FC), Color(0x5538BDF8), true)
        1 -> CityCardVisualTheme(CityCardRainTop, CityCardRainBottom, CityCardRainGlow, Color(0x887DD3FC), Color(0x4D2DD4BF), true)
        2 -> CityCardVisualTheme(CityCardNightTop, CityCardNightBottom, CityCardNightGlow, Color(0x88A5B4FC), Color(0x4D6366F1), false)
        else -> CityCardVisualTheme(CityCardStormTop, CityCardStormBottom, CityCardStormGlow, Color(0x88FDE047), Color(0x4DA855F7), true)
    }
}

@Composable
private fun AtmosphericCityCard(
    city: LocationModel,
    isSelected: Boolean,
    forecast: FullWeatherForecast?,
    cardIndex: Int,
    canRemove: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val units = LocalWeatherUnits.current
    val noDataStr = stringResource(R.string.no_data)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val cardTheme = resolveCityCardTheme(forecast, isSelected, cardIndex)

    val subtitleText = buildString {
        if (city.country.isNotBlank()) {
            append(city.country)
        }
        city.admin1?.takeIf { it.isNotBlank() && !it.equals(city.name, ignoreCase = true) }?.let {
            if (isNotEmpty()) append(", ")
            append(it)
        }
        if (forecast != null) {
            if (isNotEmpty()) append(" • ")
            append(forecast.current.time.format(timeFormatter))
        }
    }

    GlassCard(
        cornerRadius = 28.dp,
        topColor = cardTheme.topColor,
        bottomColor = cardTheme.bottomColor,
        borderTopColor = if (isSelected) Color(0xD9FFE082) else cardTheme.borderTopColor,
        borderBottomColor = if (isSelected) Color(0x9938BDF8) else cardTheme.borderBottomColor,
        glowColor = cardTheme.glowColor,
        glowAlignmentFactorX = 0.72f,
        glowAlignmentFactorY = 0.45f,
        isHighlighted = isSelected,
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Багатошарова 3D-ілюстрація погоди міста праворуч-угорі
            if (forecast != null) {
                WeatherIllustration(
                    condition = forecast.current.condition,
                    isDay = cardTheme.isDayIllustration,
                    modifier = Modifier
                        .size(92.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-18).dp, y = 4.dp)
                )
            }

            // Кнопка видалення міста у верхньому правому куті
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .semantics { contentDescription = "Видалити ${city.name} зі збережених" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Рядок 1: Назва міста + бейдж АКТИВНЕ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2563EB))
                                .border(1.dp, Color(0xCCFFE082), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.active_city_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                if (subtitleText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = Color(0xFFBAD6F8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.70f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (forecast != null) {
                    val today = forecast.daily14Days.firstOrNull()
                    Text(
                        text = stringResource(forecast.current.condition.stringRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    if (today != null) {
                        val maxStr = units.formatTemperature(today.maxTemperatureCelsius)
                        val minStr = units.formatTemperature(today.minTemperatureCelsius)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stringResource(R.string.max_temperature)}: $maxStr   ${stringResource(R.string.min_temperature)}: $minStr",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color(0xFFE0F2FE)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Нижній рядок картки міста: зліва пігулка Вологість + Вітер, справа велика температура
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val humStr = forecast.current.relativeHumidityPercent?.let { "$it%" } ?: noDataStr
                        val windStr = forecast.current.windSpeedKmh?.let {
                            units.formatWindSpeed(
                                kmh = it,
                                unitLabelKmh = stringResource(R.string.wind_unit_kmh),
                                unitLabelMs = stringResource(R.string.wind_unit_ms)
                            )
                        } ?: noDataStr

                        GlassPillBadge(
                            cornerRadius = 12.dp,
                            fillColor = Color(0xD90A1838),
                            borderColor = cardTheme.borderTopColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stringResource(R.string.metric_humidity)} $humStr",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFE0F2FE)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = windStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFE0F2FE)
                                )
                            }
                        }

                        Text(
                            text = units.formatTemperature(forecast.current.temperatureCelsius),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 44.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                } else {
                    // Чесний стан, якщо кеш для цього міста ще не завантажено (No Fake Data Policy)
                    Text(
                        text = stringResource(R.string.loading_weather),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFE0F2FE)
                    )
                }
            }
        }
    }
}

private fun isSameLocation(a: LocationModel, b: LocationModel): Boolean {
    if (a.id != 0L && b.id != 0L && a.id == b.id) return true
    if (a.name.equals(b.name, ignoreCase = true)) return true
    return abs(a.latitude - b.latitude) < 0.05 && abs(a.longitude - b.longitude) < 0.05
}
