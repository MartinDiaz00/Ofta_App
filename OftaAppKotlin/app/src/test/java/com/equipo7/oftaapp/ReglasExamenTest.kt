package com.equipo7.oftaapp

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ReglasExamen
import com.equipo7.oftaapp.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasExamenTest {
    @Test fun tensSoloPuedeAsignarPendiente() {
        assertEquals(listOf(EstadoExamen.PENDIENTE), ReglasExamen.estadosPermitidos(Rol.TENS))
        assertTrue(ReglasExamen.puedeAsignarEstado(Rol.TENS, EstadoExamen.PENDIENTE))
        assertFalse(ReglasExamen.puedeAsignarEstado(Rol.TENS, EstadoExamen.VALIDADO))
    }

    @Test fun pacienteNoPuedeAsignarEstados() {
        assertTrue(ReglasExamen.estadosPermitidos(Rol.PACIENTE).isEmpty())
        assertFalse(ReglasExamen.puedeAsignarEstado(Rol.PACIENTE, EstadoExamen.PENDIENTE))
    }

    @Test fun validarExigeDocumento() {
        assertNotNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.VALIDADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = "",
                tieneDocumento = false
            )
        )
        assertNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.VALIDADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = "",
                tieneDocumento = true
            )
        )
    }

    @Test fun observarExigeJustificacion() {
        assertNotNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.OBSERVADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = " ",
                tieneDocumento = false
            )
        )
        assertNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.OBSERVADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = "Repetir por calidad de imagen",
                tieneDocumento = false
            )
        )
    }

    @Test fun entregadoNoPuedeCambiarDeEstado() {
        assertNotNull(
            ReglasExamen.validarTransicion(
                Rol.ADMINISTRADOR,
                EstadoExamen.ENTREGADO,
                EstadoExamen.VALIDADO
            )
        )
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO,
                EstadoExamen.VALIDADO,
                EstadoExamen.ENTREGADO
            )
        )
    }

    @Test fun soloTensDevuelveObservadoAPendiente() {
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.TENS,
                EstadoExamen.OBSERVADO,
                EstadoExamen.PENDIENTE
            )
        )
        assertNotNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO,
                EstadoExamen.OBSERVADO,
                EstadoExamen.PENDIENTE
            )
        )
    }

    @Test fun noSePuedeVolverAPendiente() {
        assertNotNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO,
                EstadoExamen.VALIDADO,
                EstadoExamen.PENDIENTE
            )
        )
        assertNotNull(
            ReglasExamen.validarTransicion(
                Rol.ADMINISTRADOR,
                EstadoExamen.VALIDADO,
                EstadoExamen.PENDIENTE
            )
        )
    }

    @Test fun transicionesPermitidasPorRol() {
        // Oftalmólogo/A admin pueden observar y validar, y corregir un observado.
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO, EstadoExamen.PENDIENTE, EstadoExamen.OBSERVADO
            )
        )
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO, EstadoExamen.OBSERVADO, EstadoExamen.VALIDADO
            )
        )
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.ADMINISTRADOR, EstadoExamen.PENDIENTE, EstadoExamen.VALIDADO
            )
        )
        // TENS no puede promover a validado: la restricción de rol vive en validarEstado
        // (defensa en profundidad junto al repositorio).
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.TENS, EstadoExamen.PENDIENTE, EstadoExamen.VALIDADO
            )
        )
        assertNotNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.VALIDADO,
                rol = Rol.TENS,
                observaciones = "",
                tieneDocumento = true
            )
        )
        // Mantener el estado nunca es un error de transición.
        assertNull(
            ReglasExamen.validarTransicion(
                Rol.TENS, EstadoExamen.PENDIENTE, EstadoExamen.PENDIENTE
            )
        )
    }

    @Test fun entregadoExigeDocumentoYSoloDesdeValidado() {
        assertNotNull(
            ReglasExamen.validarTransicion(
                Rol.OFTALMOLOGO, EstadoExamen.PENDIENTE, EstadoExamen.ENTREGADO
            )
        )
        assertNotNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.ENTREGADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = "",
                tieneDocumento = false
            )
        )
        assertNull(
            ReglasExamen.validarEstado(
                estado = EstadoExamen.ENTREGADO,
                rol = Rol.OFTALMOLOGO,
                observaciones = "",
                tieneDocumento = true
            )
        )
    }

    @Test fun rolesConEstadosDisponiblesCubiertos() {
        assertEquals(4, ReglasExamen.estadosPermitidos(Rol.OFTALMOLOGO).size)
        assertEquals(4, ReglasExamen.estadosPermitidos(Rol.ADMINISTRADOR).size)
        assertTrue(ReglasExamen.permiteEditar(Rol.ADMINISTRADOR, EstadoExamen.ENTREGADO))
        assertFalse(ReglasExamen.permiteEditar(Rol.PACIENTE, EstadoExamen.PENDIENTE))
    }
}
