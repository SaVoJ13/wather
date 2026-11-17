package com.weather.app.data.location

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тести для [ReverseGeocoder]:
 * Перевіряє коректність форматування назв населених пунктів (сіл, міст)
 * та відсутність захардкодzone-помилок.
 */
class ReverseGeocoderTest {

    @Test
    fun formatSettlementName_przeclaw_formatsCorrectly() {
        assertEquals("Пшецлав (Przecław)", ReverseGeocoder.formatSettlementName("Przecław"))
        assertEquals("Пшецлав (Przecław)", ReverseGeocoder.formatSettlementName("przecław"))
        assertEquals("Пшецлав (Przecław)", ReverseGeocoder.formatSettlementName("Пшецлав"))
    }

    @Test
    fun formatSettlementName_szczecin_formatsCorrectly() {
        assertEquals("Щецин (Szczecin)", ReverseGeocoder.formatSettlementName("Szczecin"))
        assertEquals("Щецин (Szczecin)", ReverseGeocoder.formatSettlementName("Щецин"))
    }

    @Test
    fun formatSettlementName_otherCities_retainsOriginalTrimmedName() {
        assertEquals("Львів", ReverseGeocoder.formatSettlementName(" Львів "))
        assertEquals("Kyiv", ReverseGeocoder.formatSettlementName("Kyiv"))
        assertEquals("Berlin", ReverseGeocoder.formatSettlementName("Berlin"))
    }
}
