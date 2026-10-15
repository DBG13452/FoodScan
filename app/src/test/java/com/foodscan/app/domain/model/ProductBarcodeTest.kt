package com.foodscan.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductBarcodeTest {
    @Test fun acceptsSupportedCodesAndWhitespace() {
        listOf("3017620422003", "96385074", "036000291452").forEach {
            assertEquals(it, ProductBarcode.normalize(it))
        }
        assertEquals("3017620422003", ProductBarcode.normalize(" 3017 6204-22003 "))
    }

    @Test fun rejectsWrongChecksumAndNonAsciiInput() {
        listOf("", "123", "3017620422004", "00000000", "https://example.com", "３０１７６２０４２２００３").forEach {
            assertNull(ProductBarcode.normalize(it))
        }
    }

    @Test fun expandsUpceToUpcaBeforeLookup() {
        assertEquals("042100005264", ProductBarcode.fromCamera("04252614", isUpce = true))
        assertEquals("04252614", ProductBarcode.normalize("04252614"))
        assertEquals("042100005264", ProductBarcode.forLookup("04252614"))
        assertNull(ProductBarcode.fromCamera("04252615", isUpce = true))
    }
}
