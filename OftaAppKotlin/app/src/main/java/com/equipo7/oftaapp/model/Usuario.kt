package com.equipo7.oftaapp.model

enum class Rol {
    PACIENTE,
    TENS,
    OFTALMOLOGO,
    ADMINISTRADOR;

    fun puedeRegistrarExamen(): Boolean =
        this != PACIENTE

    fun puedeGestionarPacientes(): Boolean =
        this in setOf(TENS, OFTALMOLOGO, ADMINISTRADOR)

    fun puedeGestionarUsuarios(): Boolean =
        this == ADMINISTRADOR

    val nombreMostrar: String
        get() = when (this) {
            PACIENTE -> "Paciente"
            TENS -> "TENS"
            OFTALMOLOGO -> "Oftalmólogo"
            ADMINISTRADOR -> "Administrador"
        }
}

data class Usuario(
    val usuario: String,
    val contrasena: String,
    val nombreCompleto: String,
    val correo: String,
    val rol: Rol
)
