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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.theme.SunAccent

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

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val (icon, description) = weatherConditionIconData(condition)
    val finalTint = if (tint == Color.Unspecified) {
        when (condition) {
            WeatherCondition.CLEAR_SKY, WeatherCondition.MAINLY_CLEAR -> SunAccent
            WeatherCondition.SNOW -> Color(0xFFE1F5FE)
            WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> Color(0xFF4FC3F7)
            WeatherCondition.DRIZZLE -> Color(0xFF81D4FA)
            WeatherCondition.THUNDERSTORM -> Color(0xFFFFD54F)
            else -> Color.White
        }
    } else {
        tint
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = modifier,
        tint = finalTint
    )
}
