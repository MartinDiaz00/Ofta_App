package com.equipo7.oftaapp

import com.equipo7.oftaapp.util.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {
    @Test fun aceptaFechaRealConFormatoCorrecto() {
        assertTrue(Validadores.fechaValida("28/09/2026"))
    }

    @Test fun rechazaFechaImposible() {
        assertFalse(Validadores.fechaValida("31/02/2026"))
    }

    @Test fun rechazaFechaConFormatoIncorrecto() {
        assertFalse(Validadores.fechaValida("2026-09-28"))
    }

    @Test fun validaCorreoBasico() {
        assertTrue(Validadores.correoValido("persona@ejemplo.cl"))
        assertFalse(Validadores.correoValido("persona-ejemplo.cl"))
    }

    @Test fun validaRutChileno() {
        assertTrue(Validadores.rutValido("11.111.111-1"))
        assertFalse(Validadores.rutValido("11.111.111-2"))
    }

    @Test fun formateaRutMientrasSeEscribe() {
        assertEquals("12.345.678-9", Validadores.formatearRutMientrasEscribe("123456789"))
        assertEquals("12.345.678-9", Validadores.formatearRutMientrasEscribe("12.345.678-9"))
        assertEquals("1", Validadores.formatearRutMientrasEscribe("1"))
        assertEquals("", Validadores.formatearRutMientrasEscribe("abc"))
    }

    @Test fun observacionMaximoQuinientosCaracteres() {
        assertTrue(Validadores.observacionValida("a".repeat(500)))
        assertFalse(Validadores.observacionValida("a".repeat(501)))
    }
}
