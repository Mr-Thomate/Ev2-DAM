package com.fabiancg.ejemplosfuncionesnativas

import com.fabiancg.ejemplosfuncionesnativas.util.formatearTamano
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoTest {

    @Test
    fun tamanoMenorA1KbSeMuestraEnBytes() {
        assertEquals("0 B", formatearTamano(0))
        assertEquals("500 B", formatearTamano(500))
    }

    @Test
    fun tamanoEnKilobytesConUnDecimal() {
        assertEquals("1,0 KB", formatearTamano(1024))
        assertEquals("1,5 KB", formatearTamano(1536))
    }

    @Test
    fun tamanoEnMegabytesConUnDecimal() {
        assertEquals("2,0 MB", formatearTamano(2L * 1024 * 1024))
        assertEquals("20,0 MB", formatearTamano(20L * 1024 * 1024))
    }
}
