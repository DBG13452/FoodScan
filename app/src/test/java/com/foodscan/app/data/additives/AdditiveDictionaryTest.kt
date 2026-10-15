package com.foodscan.app.data.additives

import org.junit.Assert.*
import org.junit.Test

class AdditiveDictionaryTest {
    @Test fun knownCodesSubgroupsAndUnknownCodesStayDistinct() {
        assertEquals("E322", AdditiveDictionary.find("en:e322i")?.code)
        assertEquals("E440", AdditiveDictionary.find("Е 440(ii)")?.code)
        assertEquals("E330", AdditiveDictionary.find("E330")?.code)
        assertNull(AdditiveDictionary.find("E9999"))
        assertNull(AdditiveDictionary.find("E330X"))
        assertNull(AdditiveDictionary.find("E150Z"))
        assertEquals("E150A", AdditiveDictionary.find("en:e150a")?.code)
        assertEquals("E407A", AdditiveDictionary.find("en:e407a")?.code)
        assertEquals("E472E", AdditiveDictionary.find("E472e")?.code)
    }

    @Test fun catalogueHasUniqueReviewedRecordsAndRussianSources() {
        val entries = AdditiveDictionary.entries
        assertTrue(entries.size >= 100)
        assertEquals(entries.size, entries.map { it.code }.toSet().size)
        val variants = entries.flatMap { it.variants }.map(AdditiveDictionary::normalize)
        assertEquals(variants.size, variants.toSet().size)
        assertTrue(variants.none { alias -> entries.any { it.code == alias } })
        entries.forEach { info ->
            assertTrue(info.code, Regex("E\\d{3,4}[A-Z]?").matches(info.code))
            assertTrue(info.code, info.description.length >= 80 && info.healthEffects.length >= 80)
            assertTrue(info.code, info.reason.isNotBlank() && info.sources.isNotEmpty())
            assertTrue(info.code, info.sources.all { it.language == "ru" && it.url.startsWith("https://") && it.title.isNotBlank() })
            if (info.concern != AdditiveConcern.LOW) assertTrue(info.code, info.sources.size >= 3)
        }
    }

    @Test fun concernCategoriesKeepConditionsAndJurisdictionsDistinct() {
        assertEquals(AdditiveConcern.HIGH, AdditiveDictionary.find("E128")?.concern)
        assertEquals(AdditiveConcern.ATTENTION, AdditiveDictionary.find("E171")?.concern)
        assertEquals(AdditiveConcern.ATTENTION, AdditiveDictionary.find("E250")?.concern)
        assertEquals(AdditiveConcern.ATTENTION, AdditiveDictionary.find("E951")?.concern)
        assertEquals(AdditiveConcern.LOW, AdditiveDictionary.find("E330")?.concern)
        assertTrue(AdditiveDictionary.find("E951")!!.healthEffects.contains("40 мг/кг"))
        assertTrue(AdditiveDictionary.find("E171")!!.healthEffects.contains("В ЕАЭС"))
        assertTrue(AdditiveDictionary.find("E339")!!.healthEffects.contains("почек"))
    }

    @Test fun searchSupportsRussianCodeSpacingNamesAndNoMatch() {
        assertEquals(listOf("E330"), AdditiveDictionary.search("Е 330").map { it.code })
        assertEquals(listOf("E330"), AdditiveDictionary.search("лимонная кислота").map { it.code })
        assertEquals(listOf("E322"), AdditiveDictionary.search("en:e322i").map { it.code })
        assertTrue(AdditiveDictionary.search("желтый").any { it.code == "E104" })
        assertTrue(AdditiveDictionary.search("E9999").isEmpty())
        assertEquals(AdditiveDictionary.entries, AdditiveDictionary.search("  "))
    }
}
