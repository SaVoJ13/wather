package com.weather.app.ui.screens

import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.AdBannerPlaceholder
import com.weather.app.ui.components.CurrentWeatherHero
import com.weather.app.ui.components.DailyForecastList
import com.weather.app.ui.components.DayDetailsBottomSheet
import com.weather.app.ui.components.HourlyForecastRow
import com.weather.app.ui.components.WeatherMetricsGrid
import com.weather.app.ui.components.WeatherTopBar
import com.weather.app.ui.state.WeatherUiState
import com.weather.app.ui.theme.LocalReducedMotion
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.WeatherTheme
import com.weather.app.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel = viewModel(),
    onSearchClick: () -> Unit = {},
    onLocationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isSystemReducedMotion = remember {
        try {
            val animScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            val transScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            animScale == 0f || transScale == 0f
        } catch (e: Exception) {
            false
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val condition = when (val state = uiState) {
        is WeatherUiState.Success -> state.forecast.current.condition
        is WeatherUiState.Error -> state.lastCachedForecast?.current?.condition ?: WeatherCondition.CLEAR_SKY
        is WeatherUiState.Loading -> WeatherCondition.CLEAR_SKY
    }

    WeatherTheme(
        condition = condition,
        reducedMotion = isSystemReducedMotion
    ) {
        WeatherMainScreenContent(
            uiState = uiState,
            onRefresh = { viewModel.refresh() },
            onSearchClick = onSearchClick,
            onLocationClick = onLocationClick
        )
    }
}

@Composable
fun WeatherMainScreenContent(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val reducedMotion = LocalReducedMotion.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedDayForDetails by remember { mutableStateOf<DailyForecastItem?>(null) }

    val backgroundBrush = remember(palette.gradientColors) {
        Brush.verticalGradient(colors = palette.gradientColors)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) { paddingValues ->
        Crossfade(
            targetState = uiState,
            animationSpec = tween(durationMillis = if (reducedMotion) 100 else 250),
            label = "ScreenStateCrossfade",
            modifier = Modifier.padding(paddingValues)
        ) { state ->
            when (state) {
                is WeatherUiState.Loading -> {
                    WeatherLoadingSkeleton()
                }
                is WeatherUiState.Success -> {
                    WeatherSuccessContent(
                        forecast = state.forecast,
                        isRefreshing = state.isRefreshing,
                        isOffline = state.isOffline,
                        onRefresh = onRefresh,
                        onSearchClick = onSearchClick,
                        onLocationClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Запит геолокації (ADR-004)")
                            }
                            onLocationClick()
                        },
                        onDayClick = { selectedDayForDetails = it }
                    )
                }
                is WeatherUiState.Error -> {
                    if (state.lastCachedForecast != null) {
                        // Якщо є старий кеш, показуємо його з попередженням про помилку оновлення
                        WeatherSuccessContent(
                            forecast = state.lastCachedForecast,
                            isRefreshing = false,
                            isOffline = true,
                            onRefresh = onRefresh,
                            onSearchClick = onSearchClick,
                            onLocationClick = onLocationClick,
                            onDayClick = { selectedDayForDetails = it }
                        )
                    } else {
                        WeatherErrorScreen(
                            message = state.message,
                            canRetry = state.canRetry,
                            onRetry = onRefresh
                        )
                    }
                }
            }
        }
    }

    // Деталі обраного дня у Bottom Sheet
    if (selectedDayForDetails != null) {
        DayDetailsBottomSheet(
            item = selectedDayForDetails,
            onDismissRequest = { selectedDayForDetails = null }
        )
    }
}

@Composable
private fun WeatherSuccessContent(
    forecast: FullWeatherForecast,
    isRefreshing: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onDayClick: (DailyForecastItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val scrollState = rememberScrollState()
    val todayForecast = forecast.daily14Days.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .semantics {
                contentDescription = "Погода в місті : " +
                        "°C, "
            }
    ) {
        // Верхня панель дій
        WeatherTopBar(
            location = forecast.location,
            currentTime = forecast.current.time,
            onSearchClick = onSearchClick,
            onLocationClick = onLocationClick
        )

        // Офлайн-банер / індикатор застарілих даних
        if (isOffline || forecast.isStale) {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val staleTime = forecast.updatedAt.format(timeFormatter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Офлайн-режим",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Офлайн-режим. Показано прогноз від ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Спробувати оновити",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Головна картка поточної погоди
        CurrentWeatherHero(
            current = forecast.current,
            todayForecast = todayForecast,
            updatedAt = forecast.updatedAt,
            isStale = forecast.isStale,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Погодинна стрічка (24 години)
        HourlyForecastRow(
            hourlyItems = forecast.hourly24h,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Сітка з 4 параметрів погоди (вітер, вологість, тиск, UV)
        WeatherMetricsGrid(
            current = forecast.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список прогнозу на 14 днів
        DailyForecastList(
            dailyItems = forecast.daily14Days,
            onDayClick = onDayClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Рекламний банерний слот (AdMob Slot)
        AdBannerPlaceholder()

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun WeatherLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { contentDescription = "Завантаження погодних даних" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Скелетон заголовка
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.surfaceTint)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Скелетон Hero-картки
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceTint)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Скелетон погодинного ряду
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(palette.surfaceTint)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Скелетон карток метрик
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surfaceTint)
        )
    }
}

@Composable
private fun WeatherErrorScreen(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = palette.textColor,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Немає з'єднання",
            style = MaterialTheme.typography.headlineSmall,
            color = palette.textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        if (canRetry) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.textColor,
                    contentColor = palette.gradientColors.first()
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.sizeIn(minWidth = 160.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Спробувати знову",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
