package com.equipo7.oftaapp.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias y marcador de sesión simulada.
 * Usa EncryptedSharedPreferences respaldado por Android Keystore cuando está disponible;
 * si el dispositivo no lo soporta, degrada a SharedPreferences normal (demo académica).
 */
class PreferenciasRepository(context: Context) {
    private val preferencias = runCatching {
        val llaveMaestra = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "ofta_seguro",
            llaveMaestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { context.getSharedPreferences("ofta_preferencias", Context.MODE_PRIVATE) }

    val almacenamientoCifrado: Boolean =
        preferencias is EncryptedSharedPreferences

    private val _textoGrande = MutableStateFlow(preferencias.getBoolean(CLAVE_TEXTO_GRANDE, false))
    val textoGrande: StateFlow<Boolean> = _textoGrande.asStateFlow()

    fun cambiarTextoGrande(activo: Boolean) {
        _textoGrande.value = activo
        preferencias.edit().putBoolean(CLAVE_TEXTO_GRANDE, activo).apply()
    }

    /** Token simulado de sesión: solo el id de usuario, nunca credenciales reales. */
    fun guardarSesion(usuarioId: String) {
        preferencias.edit().putString(CLAVE_SESION, usuarioId).apply()
    }

    fun leerSesion(): String? = preferencias.getString(CLAVE_SESION, null)

    fun cerrarSesion() {
        preferencias.edit().remove(CLAVE_SESION).apply()
    }

    private companion object {
        const val CLAVE_TEXTO_GRANDE = "texto_grande"
        const val CLAVE_SESION = "sesion_usuario_id"
    }
}
