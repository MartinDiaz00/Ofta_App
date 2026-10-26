package com.equipo7.oftaapp.model

import com.equipo7.oftaapp.util.Fechas

data class Paciente(
    val id: String,
    val rut: String,
    val nombreCompleto: String,
    val fechaNacimiento: String,
    val correo: String,
    val telefonoPrincipal: String,
    val telefonoSecundario: String? = null,
    val prevision: String,
    val activo: Boolean = true
) {
    val edad: Int
        get() = Fechas.edad(fechaNacimiento)
}
