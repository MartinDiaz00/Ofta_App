package com.equipo7.oftaapp.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado y avisos de sincronización FHIR.
 *
 * El guardado local (Room) es la fuente de verdad y nunca se revierte por un fallo de red.
 * Los repositorios publican aquí el estado "pendiente" cuando la sincronización best-effort
 * falla; la navegación lo muestra como snackbar y el banner global como estado persistente.
 */
object SincronizacionMonitor {
    enum class Estado { SINCRONIZADO, PENDIENTE }

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private val _estado = MutableStateFlow(Estado.SINCRONIZADO)
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    fun exito() {
        _estado.value = Estado.SINCRONIZADO
    }

    fun pendiente(mensaje: String) {
        _estado.value = Estado.PENDIENTE
        _mensajes.tryEmit(mensaje)
    }
}
