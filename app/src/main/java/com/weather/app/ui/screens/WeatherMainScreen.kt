package com.weather.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.AdBannerPlaceholder
import com.weather.app.ui.components.CurrentWeatherHero
import com.weather.app.ui.components.DailyForecastList
import com.weather.app.ui.components.DayDetailsBottomSheet
import com.weather.app.ui.components.HourlyForecastRow
import com.weather.app.ui.components.SearchCityBottomSheet
import com.weather.app.ui.components.WeatherMetricsGrid
import com.weather.app.ui.components.WeatherTopBar
import com.weather.app.ui.state.WeatherUiState
import com.weather.app.ui.theme.LocalReducedMotion
import com.weather.app.ui.theme.LocalWeatherPalette
import com.weather.app.ui.theme.WeatherTheme
import com.weather.app.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearchSheetOpen by remember { mutableStateOf(false) }

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

    // Функція визначення локації
    val determineGpsLocation = {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf("fused", LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var bestLocation: android.location.Location? = null

            for (provider in providers) {
                try {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                } catch (e: SecurityException) {
                    // ігноруємо відсутність дозволу для окремого провайдера
                } catch (e: IllegalArgumentException) {
                    // провайдер не підтримується на цьому пристрої
                }
            }

            val isFresh = bestLocation != null && (System.currentTimeMillis() - bestLocation.time) < 15 * 60 * 1000

            if (bestLocation != null) {
                viewModel.onGpsLocationAcquired(context, bestLocation.latitude, bestLocation.longitude)
                scope.launch {
                    snackbarHostState.showSnackbar("Визначаємо населений пункт за GPS...")
                }
            }

            // Якщо кешованої локації немає або вона застаріла, запитуємо свіжі координати
            if (!isFresh && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    val activeProvider = when {
                        locationManager.isProviderEnabled("fused") -> "fused"
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        else -> LocationManager.NETWORK_PROVIDER
                    }
                    locationManager.getCurrentLocation(
                        activeProvider,
                        null,
                        ContextCompat.getMainExecutor(context)
                    ) { freshLoc ->
                        if (freshLoc != null && bestLocation == null) {
                            viewModel.onGpsLocationAcquired(context, freshLoc.latitude, freshLoc.longitude)
                        }
                    }
                } catch (e: Exception) {
                    // безпечний фолбек
                }
            }

            if (bestLocation == null && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                scope.launch {
                    snackbarHostState.showSnackbar("Не вдалося отримати координати GPS. Спробуйте на вулиці або виберіть місто зі списку.")
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Помилка доступу до сервісу геопозиції")
            }
        }
    }

    // Завантажуємо збережену локацію або визначаємо поточну за GPS
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val savedLoc = com.weather.app.data.local.WeatherLocalCache.loadActiveLocation(context)
        if (savedLoc != null) {
            viewModel.selectLocation(savedLoc, onLoaded = { forecast ->
                com.weather.app.widget.WeatherWidgetScheduler.updateWidgetNow(context, forecast)
            })
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            determineGpsLocation()
        }
    }

    // Системний лаунчер запиту дозволів на геолокацію
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            determineGpsLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Дозвіл на доступ до геопозиції відхилено. Ви можете вибрати місто вручну через пошук.")
            }
        }
    }

    val onLocationClick: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            determineGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            snackbarHostState = snackbarHostState,
            onRefresh = {
                viewModel.refresh(onLoaded = { forecast ->
                    com.weather.app.widget.WeatherWidgetScheduler.updateWidgetNow(context, forecast)
                })
            },
            onSearchClick = { isSearchSheetOpen = true },
            onLocationClick = onLocationClick,
            modifier = modifier
        )

        // Модальне вікно пошуку міст
        if (isSearchSheetOpen) {
            SearchCityBottomSheet(
                onDismissRequest = { isSearchSheetOpen = false },
                onCitySelected = { city ->
                    com.weather.app.data.local.WeatherLocalCache.saveActiveLocation(context, city)
                    viewModel.selectLocation(city, onLoaded = { forecast ->
                        com.weather.app.widget.WeatherWidgetScheduler.updateWidgetNow(context, forecast)
                    })
                },
                onSearchQuery = { query ->
                    viewModel.searchCities(query)
                },
                popularCities = viewModel.popularCities
            )
        }
    }
}

@Composable
fun WeatherMainScreenContent(
    uiState: WeatherUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val reducedMotion = LocalReducedMotion.current
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
                        onLocationClick = onLocationClick,
                        onDayClick = { selectedDayForDetails = it }
                    )
                }
                is WeatherUiState.Error -> {
                    if (state.lastCachedForecast != null) {
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
    onDayClick: (DailyForecastItem) -> Unit
) {
    val palette = LocalWeatherPalette.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Верхня панель (місто, пошук, геолокація)
        WeatherTopBar(
            location = forecast.location,
            currentTime = forecast.current.time,
            onSearchClick = onSearchClick,
            onLocationClick = onLocationClick
        )

        // Попередження про офлайн / старі дані
        if (isOffline) {
            OfflineWarningBanner(onRetry = onRefresh)
        }

        // Індикатор фонового оновлення
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = palette.textColor,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Головний блок поточної погоди
        CurrentWeatherHero(
            current = forecast.current,
            todayForecast = forecast.daily14Days.firstOrNull(),
            updatedAt = forecast.updatedAt,
            isStale = forecast.isStale,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Сітка метрик 2x2
        WeatherMetricsGrid(
            current = forecast.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Погодинний прогноз (24 год)
        HourlyForecastRow(
            hourlyItems = forecast.hourly24h,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Прогноз на 14 днів
        DailyForecastList(
            dailyItems = forecast.daily14Days,
            onDayClick = onDayClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Рекламний блок
        AdBannerPlaceholder(
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun OfflineWarningBanner(
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3E0))
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
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Офлайн-режим. Показано збережені дані.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100)
            )
        }

        IconButton(
            onClick = onRetry,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Оновити дані погоди",
                tint = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun WeatherLoadingSkeleton() {
    val palette = LocalWeatherPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Завантаження погодних даних" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = palette.textColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Завантаження погоди...",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textColor
            )
        }
    }
}

@Composable
private fun WeatherErrorScreen(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Помилка",
                tint = palette.textColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Не вдалося отримати погоду",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            if (canRetry) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.textColor,
                        contentColor = palette.surfaceTint
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Спробувати знову")
                }
            }
        }
    }
}
