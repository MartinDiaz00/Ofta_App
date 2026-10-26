package com.equipo7.oftaapp

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
}
