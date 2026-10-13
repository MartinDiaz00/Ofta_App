package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario

class UsuarioRepository {
    fun autenticar(usuario: String, contrasena: String): Usuario? {
        val cuenta = usuarios.firstOrNull {
            it.usuario.equals(usuario.trim(), ignoreCase = true)
        }
        return if (cuenta != null && cuenta.contrasena == contrasena) cuenta else null
    }

    fun obtenerPorUsuario(usuario: String): Usuario? =
        usuarios.firstOrNull { it.usuario.equals(usuario, ignoreCase = true) }

    companion object {
        val usuarios = listOf(
            Usuario(
                usuario = "dra.fernandez",
                contrasena = "1234",
                nombreCompleto = "Dra. Fernández",
                correo = "m.fernandez@oftapp.cl",
                rol = Rol.OFTALMOLOGO
            ),
            Usuario(
                usuario = "dr.silva",
                contrasena = "1234",
                nombreCompleto = "Dr. Roberto Silva",
                correo = "r.silva@oftapp.cl",
                rol = Rol.OFTALMOLOGO
            ),
            Usuario(
                usuario = "tens.martinez",
                contrasena = "1234",
                nombreCompleto = "TENS Martinez",
                correo = "tens.martinez@oftapp.cl",
                rol = Rol.TENS
            ),
            Usuario(
                usuario = "sxtnix",
                contrasena = "1234",
                nombreCompleto = "Estudiante DuocUC",
                correo = "di.diazh@duocuc.cl",
                rol = Rol.PACIENTE
            )
        )
    }
}
