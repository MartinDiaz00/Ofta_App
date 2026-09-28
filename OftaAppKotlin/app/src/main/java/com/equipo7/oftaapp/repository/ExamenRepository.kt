package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.model.ExamenOftalmologico

class ExamenRepository {

    private val examenesSimulados = mutableListOf(
        ExamenOftalmologico(
            id = "EX-001",
            pacienteNombre = "Juan Pérez",
            tipoExamen = "Tonometría de Aplanación",
            fecha = "2026-03-20",
            medicoSolicitante = "Dr. Roberto Silva",
            estado = "Completado",
            observaciones = "Presión intraocular dentro de rango normal."
        ),
        ExamenOftalmologico(
            id = "EX-002",
            pacienteNombre = "María González",
            tipoExamen = "Campimetría Computarizada",
            fecha = "2026-03-21",
            medicoSolicitante = "Dra. Ana López",
            estado = "Pendiente",
            observaciones = "Requiere evaluación de campo visual perimétrico."
        ),
        ExamenOftalmologico(
            id = "EX-003",
            pacienteNombre = "Carlos Tapia",
            tipoExamen = "Tomografía de Coherencia Óptica (OCT)",
            fecha = "2026-03-21",
            medicoSolicitante = "Dr. Roberto Silva",
            estado = "Completado",
            observaciones = "Grosor macular normal en ambos ojos."
        )
    )

    fun obtenerExamenes(): List<ExamenOftalmologico> {
        return examenesSimulados.toList()
    }

    fun buscarPorTipo(filtro: String): List<ExamenOftalmologico> {
        if (filtro.isBlank()) return examenesSimulados
        return examenesSimulados.filter {
            it.tipoExamen.contains(filtro, ignoreCase = true) ||
                    it.pacienteNombre.contains(filtro, ignoreCase = true)
        }
    }
}