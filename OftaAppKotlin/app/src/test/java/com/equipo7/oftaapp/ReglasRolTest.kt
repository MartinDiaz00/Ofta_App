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

    @Test fun soloOftalmologiaYAdminValidanYEntregan() {
        assertTrue(Rol.OFTALMOLOGO.puedeValidarExamen())
        assertTrue(Rol.ADMINISTRADOR.puedeValidarExamen())
        assertTrue(Rol.OFTALMOLOGO.puedeEntregarExamen())
        assertTrue(Rol.ADMINISTRADOR.puedeEntregarExamen())
        assertFalse(Rol.TENS.puedeValidarExamen())
        assertFalse(Rol.TENS.puedeEntregarExamen())
        assertFalse(Rol.PACIENTE.puedeEntregarExamen())
    }

    @Test fun visibilidadYGestionDeUsuarios() {
        assertTrue(Rol.TENS.puedeVerTodosLosPacientes())
        assertTrue(Rol.OFTALMOLOGO.puedeVerTodosLosPacientes())
        assertFalse(Rol.PACIENTE.puedeVerTodosLosPacientes())
        assertTrue(Rol.ADMINISTRADOR.puedeGestionarUsuarios())
        assertFalse(Rol.OFTALMOLOGO.puedeGestionarUsuarios())
        assertFalse(Rol.TENS.puedeGestionarUsuarios())
    }

    @Test fun rolDesdeTextoDemo() {
        assertTrue(Rol.desde("admin") == Rol.ADMINISTRADOR)
        assertTrue(Rol.desde("OFTALMÓLOGO") == Rol.OFTALMOLOGO)
        assertTrue(Rol.desde("TENS") == Rol.TENS)
        assertTrue(Rol.desde(null) == Rol.PACIENTE)
        assertTrue(Rol.desde("desconocido") == Rol.PACIENTE)
    }
}
