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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weather.app.R
import com.weather.app.domain.model.DailyForecastItem
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.ui.components.AdBannerPlaceholder
import com.weather.app.ui.components.DayDetailsBottomSheet
import com.weather.app.ui.components.LanguageSelectionDialog
import com.weather.app.ui.components.SearchCityBottomSheet
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
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearchSheetOpen by remember { mutableStateOf(false) }
    var isLanguageDialogOpen by remember { mutableStateOf(false) }

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
                    snackbarHostState.showSnackbar(context.getString(R.string.gps_error))
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.location_service_error))
            }
        }
    }

    // Завантажуємо збережену локацію або визначаємо поточну за GPS
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadSelectedCities(context)
        val savedLoc = com.weather.app.data.local.WeatherLocalCache.loadActiveLocation(context)
        if (savedLoc != null) {
            viewModel.selectLocation(savedLoc, context = context, onLoaded = { forecast ->
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
                snackbarHostState.showSnackbar(context.getString(R.string.location_permission_denied))
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
    val selectedCities by viewModel.selectedCities.collectAsState()
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
            onLanguageClick = { isLanguageDialogOpen = true },
            selectedCities = selectedCities,
            onCitySelected = { city ->
                com.weather.app.data.local.WeatherLocalCache.saveActiveLocation(context, city)
                viewModel.selectLocation(city, context = context, onLoaded = { forecast ->
                    com.weather.app.widget.WeatherWidgetScheduler.updateWidgetNow(context, forecast)
                })
            },
            onCityRemoved = { city ->
                viewModel.removeSelectedCity(context, city)
            },
            modifier = modifier
        )

        // Модальне вікно вибору мови
        if (isLanguageDialogOpen) {
            LanguageSelectionDialog(
                onDismissRequest = { isLanguageDialogOpen = false }
            )
        }

        // Модальне вікно пошуку міст
        if (isSearchSheetOpen) {
            SearchCityBottomSheet(
                onDismissRequest = { isSearchSheetOpen = false },
                onCitySelected = { city ->
                    com.weather.app.data.local.WeatherLocalCache.saveActiveLocation(context, city)
                    viewModel.selectLocation(city, context = context, onLoaded = { forecast ->
                        com.weather.app.widget.WeatherWidgetScheduler.updateWidgetNow(context, forecast)
                    })
                },
                onRemoveCity = { city ->
                    viewModel.removeSelectedCity(context, city)
                },
                onSearchQuery = { query ->
                    viewModel.searchCities(query)
                },
                selectedCities = selectedCities
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
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedCities: List<LocationModel> = emptyList(),
    onCitySelected: (LocationModel) -> Unit = {},
    onCityRemoved: (LocationModel) -> Unit = {}
) {
    val palette = LocalWeatherPalette.current
    val reducedMotion = LocalReducedMotion.current
    var selectedTab by remember { mutableStateOf(WeatherNavTab.NOW) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var selectedDayForDetails by remember { mutableStateOf<DailyForecastItem?>(null) }

    val backgroundBrush = remember(palette.gradientColors) {
        Brush.verticalGradient(colors = palette.gradientColors)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        bottomBar = {
            if (!isSettingsOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Рекламний контейнер AdMob Anchored Adaptive Banner (Zero CLS)
                    AdBannerPlaceholder(bannerHeight = 60.dp)

                    // Нижня панель навігації на 3 вкладки
                    NavigationBar(
                        containerColor = palette.surfaceTint.copy(alpha = 0.95f),
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WeatherNavTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.contentDescription,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(tab.titleRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = palette.accentColor,
                                    selectedTextColor = palette.accentColor,
                                    indicatorColor = palette.surfaceTint,
                                    unselectedIconColor = palette.textColor.copy(alpha = 0.6f),
                                    unselectedTextColor = palette.textColor.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) { paddingValues ->
        if (isSettingsOpen) {
            SettingsScreen(
                onBackClick = { isSettingsOpen = false },
                onLanguageClick = onLanguageClick,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
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
                        WeatherTabContent(
                            forecast = state.forecast,
                            selectedTab = selectedTab,
                            isRefreshing = state.isRefreshing,
                            isOffline = state.isOffline,
                            selectedCities = selectedCities,
                            onRefresh = onRefresh,
                            onSearchClick = onSearchClick,
                            onLocationClick = onLocationClick,
                            onSettingsClick = { isSettingsOpen = true },
                            onCitySelected = onCitySelected,
                            onCityRemoved = onCityRemoved,
                            onDayClick = { selectedDayForDetails = it }
                        )
                    }
                    is WeatherUiState.Error -> {
                        if (state.lastCachedForecast != null) {
                            WeatherTabContent(
                                forecast = state.lastCachedForecast,
                                selectedTab = selectedTab,
                                isRefreshing = false,
                                isOffline = true,
                                selectedCities = selectedCities,
                                onRefresh = onRefresh,
                                onSearchClick = onSearchClick,
                                onLocationClick = onLocationClick,
                                onSettingsClick = { isSettingsOpen = true },
                                onCitySelected = onCitySelected,
                                onCityRemoved = onCityRemoved,
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
private fun WeatherTabContent(
    forecast: FullWeatherForecast,
    selectedTab: WeatherNavTab,
    isRefreshing: Boolean,
    isOffline: Boolean,
    selectedCities: List<LocationModel>,
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCitySelected: (LocationModel) -> Unit,
    onCityRemoved: (LocationModel) -> Unit,
    onDayClick: (DailyForecastItem) -> Unit
) {
    val palette = LocalWeatherPalette.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Верхня панель (місто, пошук, геолокація, шестерня налаштувань)
        WeatherTopBar(
            location = forecast.location,
            currentTime = forecast.current.time,
            onSearchClick = onSearchClick,
            onLocationClick = onLocationClick,
            onLanguageClick = onSettingsClick,
            onSettingsClick = onSettingsClick
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

        // Вміст активної вкладки
        Crossfade(
            targetState = selectedTab,
            label = "TabContentCrossfade",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                WeatherNavTab.NOW -> {
                    NowScreen(forecast = forecast)
                }
                WeatherNavTab.FORECAST -> {
                    ForecastScreen(
                        forecast = forecast,
                        onDayClick = onDayClick
                    )
                }
                WeatherNavTab.LOCATIONS -> {
                    LocationsScreen(
                        currentLocation = forecast.location,
                        savedCities = selectedCities,
                        onCitySelected = onCitySelected,
                        onCityRemoved = onCityRemoved,
                        onSearchClick = onSearchClick,
                        onGpsClick = onLocationClick
                    )
                }
            }
        }
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
                text = stringResource(R.string.offline_warning),
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
                contentDescription = stringResource(R.string.refresh_weather_desc),
                tint = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun WeatherLoadingSkeleton() {
    val palette = LocalWeatherPalette.current
    val loadingDesc = stringResource(R.string.loading_weather_desc)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = loadingDesc },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = palette.textColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading_weather),
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
                contentDescription = stringResource(R.string.loading_error),
                tint = palette.textColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.failed_to_load_weather),
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
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
