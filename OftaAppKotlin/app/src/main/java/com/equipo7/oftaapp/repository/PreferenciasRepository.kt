package com.equipo7.oftaapp.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenciasRepository(context: Context) {
    private val preferencias = context.getSharedPreferences("ofta_preferencias", Context.MODE_PRIVATE)

    private val _textoGrande = MutableStateFlow(preferencias.getBoolean(CLAVE_TEXTO_GRANDE, false))
    val textoGrande: StateFlow<Boolean> = _textoGrande.asStateFlow()

    fun cambiarTextoGrande(activo: Boolean) {
        _textoGrande.value = activo
        preferencias.edit().putBoolean(CLAVE_TEXTO_GRANDE, activo).apply()
    }

    private companion object {
        const val CLAVE_TEXTO_GRANDE = "texto_grande"
    }
}
