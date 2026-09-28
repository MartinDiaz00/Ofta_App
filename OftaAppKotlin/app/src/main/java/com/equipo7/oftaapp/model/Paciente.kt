package com.equipo7.oftaapp.model

data class Paciente(
    val id: String,
    val rut: String,
    val nombreCompleto: String,
    val edad: Int,
    val prevision: String
)