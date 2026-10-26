package com.equipo7.oftaapp.model

import com.equipo7.oftaapp.util.Fechas

object FiltroExamenes {
    fun aplicar(
        examenes: List<ExamenOftalmologico>,
        filtros: FiltrosExamen
    ): List<ExamenOftalmologico> {
        val texto = filtros.texto.trim()
        return examenes.filter { examen ->
            val coincideTexto = texto.isBlank() ||
                examen.pacienteNombre.contains(texto, ignoreCase = true) ||
                examen.pacienteRut.contains(texto, ignoreCase = true) ||
                examen.tipoExamen.contains(texto, ignoreCase = true) ||
                examen.sucursal.contains(texto, ignoreCase = true) ||
                examen.profesional.contains(texto, ignoreCase = true) ||
                examen.estado.etiqueta.contains(texto, ignoreCase = true)
            val coincideEstado = filtros.estado == null || examen.estado == filtros.estado
            val coincideTipo = filtros.tipoExamen == null || examen.tipoExamen == filtros.tipoExamen
            val coincideSucursal = filtros.sucursal == null || examen.sucursal == filtros.sucursal
            val coincideFecha = Fechas.dentroDeRango(examen.fecha, filtros.desde, filtros.hasta)
            coincideTexto && coincideEstado && coincideTipo && coincideSucursal && coincideFecha
        }
    }
}
