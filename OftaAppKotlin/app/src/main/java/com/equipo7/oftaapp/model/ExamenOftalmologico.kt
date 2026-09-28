package com.equipo7.oftaapp.model

data class ExamenOftalmologico(
    val id: String,
    val pacienteNombre: String,
    val tipoExamen: String,
    val fecha: String,
    val medicoSolicitante: String,
    val estado: String,
    val observaciones: String
)