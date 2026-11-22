package com.weather.app.ui

import com.weather.app.ui.screens.WeatherNavTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тести навігаційного каркаса Phase 2 (A1.1, A2.1).
 */
class NavigationAndStateTest {

    @Test
    fun testAllThreeNavigationTabsAreDefined() {
        val tabs = WeatherNavTab.values()
        assertEquals("There must be exactly 3 navigation tabs according to UX specification", 3, tabs.size)
        assertEquals(WeatherNavTab.NOW, tabs[0])
        assertEquals(WeatherNavTab.FORECAST, tabs[1])
        assertEquals(WeatherNavTab.LOCATIONS, tabs[2])
    }

    @Test
    fun testNavigationTabsHaveValidResourcesAndDescriptions() {
        for (tab in WeatherNavTab.values()) {
            assertTrue("Tab titleRes must be positive", tab.titleRes > 0)
            assertNotNull("Tab icon must not be null", tab.icon)
            assertTrue("Tab content description must not be empty for TalkBack", tab.contentDescription.isNotEmpty())
        }
    }
}
