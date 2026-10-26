package com.equipo7.oftaapp

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Rol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasRolTest {
    @Test fun permisosDeRegistro() {
        assertTrue(Rol.TENS.puedeRegistrarExamen())
        assertTrue(Rol.ADMINISTRADOR.puedeRegistrarExamen())
        assertFalse(Rol.PACIENTE.puedeRegistrarExamen())
        assertFalse(Rol.OFTALMOLOGO.puedeRegistrarExamen())
    }

    @Test fun oftalmologoPuedeGestionarPacientes() {
        assertTrue(Rol.TENS.puedeGestionarPacientes())
        assertTrue(Rol.OFTALMOLOGO.puedeGestionarPacientes())
        assertTrue(Rol.ADMINISTRADOR.puedeGestionarPacientes())
        assertFalse(Rol.PACIENTE.puedeGestionarPacientes())
    }

    @Test fun soloTensYAdminEliminanDocumentos() {
        assertTrue(Rol.TENS.puedeEliminarDocumentos())
        assertTrue(Rol.ADMINISTRADOR.puedeEliminarDocumentos())
        assertFalse(Rol.OFTALMOLOGO.puedeEliminarDocumentos())
        assertFalse(Rol.PACIENTE.puedeEliminarDocumentos())
    }

    @Test fun tensEditaPendienteYObservado() {
        assertTrue(Rol.TENS.puedeEditarExamen(EstadoExamen.PENDIENTE))
        assertTrue(Rol.TENS.puedeEditarExamen(EstadoExamen.OBSERVADO))
        assertFalse(Rol.TENS.puedeEditarExamen(EstadoExamen.VALIDADO))
        assertFalse(Rol.TENS.puedeEditarExamen(EstadoExamen.ENTREGADO))
    }
}
