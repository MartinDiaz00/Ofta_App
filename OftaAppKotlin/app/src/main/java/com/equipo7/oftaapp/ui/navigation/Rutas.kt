package com.equipo7.oftaapp.ui.navigation

import com.equipo7.oftaapp.model.EstadoExamen

object Rutas {
    const val INICIO = "inicio"
    const val EXAMENES = "examenes"
    const val PACIENTES = "pacientes"
    const val PERFIL = "perfil"
    const val NUEVO_EXAMEN = "examen/nuevo"
    const val DETALLE_EXAMEN = "examen/detalle/{id}"
    const val EDITAR_EXAMEN = "examen/editar/{id}"
    const val DETALLE_PACIENTE = "paciente/{pacienteId}"

    fun examenes(estado: EstadoExamen? = null): String =
        if (estado == null) EXAMENES else "$EXAMENES?estado=${estado.name}"

    fun detalleExamen(id: String): String = "examen/detalle/$id"

    fun editarExamen(id: String): String = "examen/editar/$id"

    fun detallePaciente(id: String): String = "paciente/$id"
}
