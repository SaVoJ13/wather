package com.weather.app.ui

import com.weather.app.data.mock.MockWeatherRepository
import com.weather.app.domain.model.FullWeatherForecast
import com.weather.app.domain.model.LocationModel
import com.weather.app.domain.model.WeatherCondition
import com.weather.app.domain.repository.WeatherRepository
import com.weather.app.ui.state.WeatherUiState
import com.weather.app.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsKyivForecastSuccessfully() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, got $state", state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success
        assertEquals("Київ", success.forecast.location.name)
        assertEquals(24, success.forecast.hourly24h.size)
        assertEquals(14, success.forecast.daily14Days.size)
    }

    @Test
    fun testSelectLocationUpdatesForecastToSelectedCity() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)
        advanceUntilIdle()

        viewModel.selectLocation(MockWeatherRepository.createWarsawLocation())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        val success = state as WeatherUiState.Success
        assertEquals("Варшава", success.forecast.location.name)
        assertEquals(WeatherCondition.RAIN, success.forecast.current.condition)
    }

    @Test
    fun testRefreshTriggersReloadAndMaintainsSuccessState() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
    }

    @Test
    fun testErrorFromRepositoryEmitsErrorStateWithRetryFlag() = runTest(testDispatcher) {
        val failingRepo = object : WeatherRepository {
            override suspend fun getFullForecast(location: LocationModel, forceRefresh: Boolean): Result<FullWeatherForecast> {
                return Result.failure(Exception("Мережевий таймаут"))
            }
            override fun observeCurrentForecast(): Flow<FullWeatherForecast?> = flowOf(null)
        }

        val viewModel = WeatherViewModel(failingRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
        val error = state as WeatherUiState.Error
        assertEquals("Мережевий таймаут", error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun testErrorRetainsLastCachedForecastWhenRefreshFails() = runTest(testDispatcher) {
        var shouldFail = false
        val mockRepo = MockWeatherRepository()
        val conditionalRepo = object : WeatherRepository {
            override suspend fun getFullForecast(location: LocationModel, forceRefresh: Boolean): Result<FullWeatherForecast> {
                return if (shouldFail) {
                    Result.failure(Exception("Помилка з'єднання"))
                } else {
                    mockRepo.getFullForecast(location, forceRefresh)
                }
            }
            override fun observeCurrentForecast(): Flow<FullWeatherForecast?> = mockRepo.observeCurrentForecast()
        }

        val viewModel = WeatherViewModel(conditionalRepo)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)

        shouldFail = true
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
        val error = state as WeatherUiState.Error
        assertNotNull("Last cached forecast must be preserved", error.lastCachedForecast)
        assertEquals("Київ", error.lastCachedForecast?.location?.name)
    }

    @Test
    fun testSelectLocationUpdatesSelectedCities() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)
        advanceUntilIdle()

        val warsaw = MockWeatherRepository.createWarsawLocation()
        viewModel.selectLocation(warsaw)
        advanceUntilIdle()

        val cities = viewModel.selectedCities.value
        assertTrue("Selected cities should contain Warsaw", cities.any { it.name == "Варшава" })
        assertEquals("Варшава", cities.first().name)
    }

    @Test
    fun testRemoveSelectedCityRemovesCityFromList() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)
        advanceUntilIdle()

        val warsaw = MockWeatherRepository.createWarsawLocation()
        viewModel.selectLocation(warsaw)
        advanceUntilIdle()
        assertTrue(viewModel.selectedCities.value.any { it.name == "Варшава" })

        viewModel.removeSelectedCity(null, warsaw)
        advanceUntilIdle()
        assertTrue(viewModel.selectedCities.value.none { it.name == "Варшава" })
    }

    @Test
    fun testCityUiStateIsolationKyivToWarsawHttp500() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val warsaw = MockWeatherRepository.createWarsawLocation()
        var warsawCallFailed = false

        val testRepo = object : WeatherRepository {
            override suspend fun getFullForecast(location: LocationModel, forceRefresh: Boolean): Result<FullWeatherForecast> {
                return if (location.name.contains("Варшава", ignoreCase = true)) {
                    warsawCallFailed = true
                    Result.failure(Exception("HTTP 500: Internal Server Error"))
                } else {
                    mockRepo.getFullForecast(location, forceRefresh)
                }
            }

            override fun observeCurrentForecast(): Flow<FullWeatherForecast?> = mockRepo.observeCurrentForecast()
        }

        val viewModel = WeatherViewModel(testRepo)
        advanceUntilIdle()

        // Початковий стан: Київ
        val initialState = viewModel.uiState.value
        assertTrue("Initial state must be Success", initialState is WeatherUiState.Success)
        assertEquals("Київ", (initialState as WeatherUiState.Success).forecast.location.name)

        // Перемикаємось на Варшаву, яка падає з HTTP 500 і не має кешу
        viewModel.selectLocation(warsaw)
        advanceUntilIdle()

        assertTrue("Warsaw call should have been triggered", warsawCallFailed)
        val finalState = viewModel.uiState.value
        assertTrue("State must be Error, not Success with Kyiv data: got $finalState", finalState is WeatherUiState.Error)
        val errorState = finalState as WeatherUiState.Error
        assertEquals("HTTP 500: Internal Server Error", errorState.message)
        // КРИТИЧНО: прогноз Києва НЕ повинен підставлятися як кеш Варшави!
        org.junit.Assert.assertNull("Kyiv's forecast must NOT leak as Warsaw's cached forecast", errorState.lastCachedForecast)
    }

    @Test
    fun testCitySearchIsIndependentOfGps() = runTest(testDispatcher) {
        val mockRepo = MockWeatherRepository()
        val viewModel = WeatherViewModel(mockRepo)
        advanceUntilIdle()

        val cities = viewModel.searchCities("Київ")
        assertNotNull(cities)
    }
}
