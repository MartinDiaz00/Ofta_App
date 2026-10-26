package com.equipo7.oftaapp.model

data class Usuario(
    val id: String,
    val usuario: String,
    val nombreCompleto: String,
    val correo: String,
    val rol: Rol,
    val pacienteId: String? = null
)
