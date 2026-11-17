package com.equipo7.oftaapp

import com.equipo7.oftaapp.util.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertTrue(Validadores.correoValido("nombre.apellido+tag@dominio.com"))
        assertFalse(Validadores.correoValido("persona-ejemplo.cl"))
        assertFalse(Validadores.correoValido("@ejemplo.cl"))
        assertFalse(Validadores.correoValido("persona@.cl"))
        assertFalse(Validadores.correoValido(""))
    }

    @Test fun validaRutChilenoFormatoSinExigirDvReal() {
        // Formatos válidos (con/sin puntos, con k/K), incluyendo DV no "real" para datos de prueba
        assertTrue(Validadores.rutValido("11.111.111-1"))
        assertTrue(Validadores.rutValido("12.345.678-5"))
        assertTrue(Validadores.rutValido("12345678-5"))
        assertTrue(Validadores.rutValido("11.111.111-1".replace(".", "")))
        assertTrue(Validadores.rutValido("7.654.321-6"))
        assertTrue(Validadores.rutValido("15.790.867-2")) // formato válido, DV aleatorio permitido
        assertTrue(Validadores.rutValido("11.111.111-2"))
        assertTrue(Validadores.rutValido("12.345.678-K"))
        // Inválidos: formato/cuerpo incorrecto
        assertFalse(Validadores.rutValido("00.000.000-0"))
        assertFalse(Validadores.rutValido("0-0"))
        assertFalse(Validadores.rutValido("abc"))
        assertFalse(Validadores.rutValido(""))
        assertFalse(Validadores.rutValido("1.234-5")) // demasiado corto
        assertFalse(Validadores.rutValido("1.234.567.890-1")) // demasiado largo
    }

    @Test fun formateaRutMientrasSeEscribeYFormateaCompleto() {
        assertEquals("12.345.678-9", Validadores.formatearRutMientrasEscribe("123456789"))
        assertEquals("12.345.678-9", Validadores.formatearRutMientrasEscribe("12.345.678-9"))
        assertEquals("123.456.789-0", Validadores.formatearRutMientrasEscribe("1234567890"))
        assertEquals("12.345.678-K", Validadores.formatearRutMientrasEscribe("12345678k"))
        assertEquals("1", Validadores.formatearRutMientrasEscribe("1"))
        assertEquals("", Validadores.formatearRutMientrasEscribe("abc"))
        assertEquals("12.345.678-5", Validadores.rutFormateado("12345678-5"))
        assertEquals("12.345.678-5", Validadores.rutFormateado("12.345.678-5"))
    }

    @Test fun validaTelefonosChilenosComunes() {
        assertTrue(Validadores.telefonoValido("912345678"))
        assertTrue(Validadores.telefonoValido("+56912345678"))
        assertTrue(Validadores.telefonoValido("56912345678"))
        assertTrue(Validadores.telefonoValido("81234567")) // 8 dígitos -> +569...
        assertFalse(Validadores.telefonoValido("12345"))
        assertFalse(Validadores.telefonoValido("+56812345678"))
        assertFalse(Validadores.telefonoValido(""))
        assertTrue(Validadores.telefonoOpcionalValido(""))
        assertTrue(Validadores.telefonoOpcionalValido("912345678"))
        assertFalse(Validadores.telefonoOpcionalValido("123"))
    }

    @Test fun validaNombresYTextos() {
        assertTrue(Validadores.nombreValido("María José Pérez"))
        assertTrue(Validadores.nombreValido("Juan O'Brien"))
        assertFalse(Validadores.nombreValido("A"))
        assertFalse(Validadores.nombreValido("Nombre123"))
        assertFalse(Validadores.nombreValido("   "))
        assertTrue(Validadores.textoValido("hola"))
        assertFalse(Validadores.textoValido("  "))
        assertTrue(Validadores.previsionValida("Fonasa"))
        assertFalse(Validadores.previsionValida(" "))
    }

    @Test fun validaFechasYHoras() {
        assertTrue(Validadores.horaValida("09:30"))
        assertTrue(Validadores.horaValida("23:59"))
        assertFalse(Validadores.horaValida("25:00"))
        assertFalse(Validadores.horaValida("12:60"))
        assertFalse(Validadores.horaValida("abc"))
    }

    @Test fun validaUsuarioYClave() {
        assertTrue(Validadores.usuarioValido("admin.ofta"))
        assertTrue(Validadores.usuarioValido("tens_01"))
        assertFalse(Validadores.usuarioValido("ab"))
        assertFalse(Validadores.usuarioValido("usuario con espacio"))
        assertTrue(Validadores.claveValida("1234"))
        assertFalse(Validadores.claveValida("123"))
    }

    @Test fun observacionMaximoQuinientosCaracteres() {
        assertTrue(Validadores.observacionValida("a".repeat(500)))
        assertFalse(Validadores.observacionValida("a".repeat(501)))
    }

    @Test fun fechaVisibleSeConvierteUnaSolaVez() {
        // Contrato usado por FormularioExamenViewModel -> ExamenRepository:
        // la UI guarda el valor visible y el repositorio hace la única conversión a ISO.
        assertEquals("2026-11-16", com.equipo7.oftaapp.util.Fechas.aIso("16/11/2026"))
        assertNull(com.equipo7.oftaapp.util.Fechas.aIso("2026-11-16"))
    }
}
