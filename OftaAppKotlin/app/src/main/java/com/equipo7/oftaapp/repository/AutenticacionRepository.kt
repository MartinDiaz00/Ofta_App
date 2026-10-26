package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.aDominio
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.util.Seguridad

data class ResultadoLogin(
    val usuario: Usuario? = null,
    val error: String? = null
)

class AutenticacionRepository(private val database: AppDatabase) {
    suspend fun autenticar(usuario: String, contrasena: String): ResultadoLogin {
        if (usuario.isBlank() || contrasena.isBlank()) {
            return ResultadoLogin(error = "Ingresa tu usuario y contraseña.")
        }
        val cuenta = database.usuarioDao().obtenerPorUsername(usuario.trim())
            ?: return ResultadoLogin(error = "Usuario o contraseña incorrectos.")
        val claveCorrecta = Seguridad.verificar(
            password = contrasena,
            salt = cuenta.passwordSalt,
            hashEsperado = cuenta.passwordHash
        )
        if (!claveCorrecta) {
            return ResultadoLogin(error = "Usuario o contraseña incorrectos.")
        }
        return ResultadoLogin(usuario = cuenta.aDominio())
    }
}
