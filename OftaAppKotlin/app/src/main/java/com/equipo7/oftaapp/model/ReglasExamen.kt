package com.equipo7.oftaapp.model

object ReglasExamen {
    fun estadosPermitidos(rol: Rol): List<EstadoExamen> = when (rol) {
        Rol.TENS -> listOf(EstadoExamen.PENDIENTE)
        Rol.OFTALMOLOGO, Rol.ADMINISTRADOR -> EstadoExamen.entries
        Rol.PACIENTE -> emptyList()
    }

    fun puedeAsignarEstado(rol: Rol, estado: EstadoExamen): Boolean =
        estado in estadosPermitidos(rol)

    fun validarEstado(
        estado: EstadoExamen,
        rol: Rol,
        observaciones: String,
        tieneDocumento: Boolean
    ): String? {
        if (!puedeAsignarEstado(rol, estado)) {
            return "Tu rol no puede asignar el estado ${estado.etiqueta}."
        }
        if ((estado == EstadoExamen.VALIDADO || estado == EstadoExamen.ENTREGADO) && !tieneDocumento) {
            return "Debes adjuntar un documento antes de marcar el examen como ${estado.etiqueta.lowercase()}."
        }
        if (estado == EstadoExamen.OBSERVADO && observaciones.isBlank()) {
            return "Describe el motivo de la observación clínica."
        }
        return null
    }

    fun validarTransicion(
        rol: Rol,
        estadoActual: EstadoExamen,
        estadoNuevo: EstadoExamen
    ): String? = when {
        estadoActual == estadoNuevo -> null
        estadoActual == EstadoExamen.ENTREGADO ->
            "Un examen entregado no puede cambiar de estado."
        estadoNuevo == EstadoExamen.PENDIENTE ->
            if (rol == Rol.TENS && estadoActual == EstadoExamen.OBSERVADO) null
            else "Solo TENS puede devolver un examen Observado a Pendiente."
        estadoNuevo == EstadoExamen.ENTREGADO && estadoActual != EstadoExamen.VALIDADO ->
            "Solo un examen validado puede marcarse como entregado."
        else -> null
    }

    fun permiteEditar(rol: Rol, estado: EstadoExamen): Boolean =
        rol.puedeEditarExamen(estado)
}
