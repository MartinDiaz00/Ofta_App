package com.equipo7.oftaapp.model

enum class Rol {
    PACIENTE,
    TENS,
    OFTALMOLOGO,
    ADMINISTRADOR;

    val nombreMostrar: String
        get() = when (this) {
            PACIENTE -> "Paciente"
            TENS -> "TENS"
            OFTALMOLOGO -> "Oftalmólogo"
            ADMINISTRADOR -> "Administrador"
        }

    fun puedeRegistrarExamen(): Boolean =
        this == TENS || this == ADMINISTRADOR

    fun puedeGestionarPacientes(): Boolean =
        this == TENS || this == OFTALMOLOGO || this == ADMINISTRADOR

    fun puedeGestionarUsuarios(): Boolean =
        this == ADMINISTRADOR

    fun puedeValidarExamen(): Boolean =
        this == OFTALMOLOGO || this == ADMINISTRADOR

    fun puedeEntregarExamen(): Boolean =
        this == OFTALMOLOGO || this == ADMINISTRADOR

    fun puedeEliminarDocumentos(): Boolean =
        this == TENS || this == ADMINISTRADOR

    fun puedeVerTodosLosPacientes(): Boolean =
        this != PACIENTE

    fun puedeEditarExamen(estado: EstadoExamen): Boolean = when (this) {
        ADMINISTRADOR -> true
        OFTALMOLOGO -> true
        TENS -> estado == EstadoExamen.PENDIENTE || estado == EstadoExamen.OBSERVADO
        PACIENTE -> false
    }

    companion object {
        fun desde(valor: String?): Rol = when (valor?.trim()?.uppercase()) {
            "TENS" -> TENS
            "OFTALMOLOGO", "OFTALMÓLOGO" -> OFTALMOLOGO
            "ADMINISTRADOR", "ADMIN" -> ADMINISTRADOR
            else -> PACIENTE
        }
    }
}
