package com.weather.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.theme.LocalWeatherPalette

fun weatherConditionIconData(condition: WeatherCondition): Pair<ImageVector, String> {
    return when (condition) {
        WeatherCondition.CLEAR_SKY -> Icons.Default.WbSunny to "Ясно, сонячно"
        WeatherCondition.MAINLY_CLEAR -> Icons.Default.WbSunny to "Переважно ясно"
        WeatherCondition.PARTLY_CLOUDY -> Icons.Default.CloudQueue to "Мінлива хмарність"
        WeatherCondition.OVERCAST -> Icons.Default.Cloud to "Похмуро, суцільна хмарність"
        WeatherCondition.FOG -> Icons.Default.WbCloudy to "Туман, видимість знижена"
        WeatherCondition.DRIZZLE -> Icons.Default.Grain to "Дрібна мряка"
        WeatherCondition.RAIN -> Icons.Default.WaterDrop to "Дощ"
        WeatherCondition.HEAVY_RAIN -> Icons.Default.WaterDrop to "Сильний зливовий дощ"
        WeatherCondition.SNOW -> Icons.Default.AcUnit to "Снігопад"
        WeatherCondition.THUNDERSTORM -> Icons.Default.Thunderstorm to "Гроза з блискавками"
        WeatherCondition.UNKNOWN -> Icons.Default.Cloud to "Невідомо"
    }
}

/**
 * Усі погодні іконки в застосунку тепер рендерять повноцінну багатошарову 3D-ілюстрацію [WeatherIllustration]
 * відповідно до директиви Власника (Stage B — Requirement 2 & 3: не замінювати складні ілюстрації простими системними іконками).
 */
@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    isDay: Boolean = LocalWeatherPalette.current.isDay
) {
    WeatherIllustration(
        condition = condition,
        modifier = modifier,
        isDay = isDay
    )
}
