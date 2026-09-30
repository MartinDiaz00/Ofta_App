package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente

class ExamenRepository {
    fun obtenerExamenes(): List<ExamenOftalmologico> = examenesSimulados.toList()

    fun buscarPorTipo(filtro: String): List<ExamenOftalmologico> {
        if (filtro.isBlank()) return examenesSimulados.toList()
        return examenesSimulados.filter {
            it.tipoExamen.contains(filtro, ignoreCase = true) ||
                    it.pacienteNombre.contains(filtro, ignoreCase = true)
        }
    }

    fun obtenerPacientes(): List<Paciente> = pacientesSimulados.toList()

    fun obtenerPacientePorNombre(nombre: String): Paciente? =
        pacientesSimulados.firstOrNull { it.nombreCompleto == nombre }

    fun agregarExamen(examen: ExamenOftalmologico) {
        examenesSimulados.add(0, examen)
    }

    fun examenesDePaciente(nombrePaciente: String): List<ExamenOftalmologico> =
        examenesSimulados.filter { it.pacienteNombre == nombrePaciente }

    fun siguienteId(): String = "EX-%03d".format(examenesSimulados.size + 1)

    companion object {
        private val examenesSimulados = mutableListOf(
            ExamenOftalmologico(
                id = "EX-001",
                pacienteNombre = "Juan Pérez",
                tipoExamen = "Tonometría de Aplanación",
                fecha = "20/03/2026",
                medicoSolicitante = "Dr. Roberto Silva",
                estado = "Completado",
                observaciones = "Presión intraocular dentro de rango normal.",
                ojoEvaluado = "Derecho"
            ),
            ExamenOftalmologico(
                id = "EX-002",
                pacienteNombre = "María González",
                tipoExamen = "Campimetría Computarizada",
                fecha = "21/03/2026",
                medicoSolicitante = "Dra. Ana López",
                estado = "Pendiente",
                observaciones = "Requiere evaluación de campo visual perimétrico.",
                ojoEvaluado = "Izquierdo"
            ),
            ExamenOftalmologico(
                id = "EX-003",
                pacienteNombre = "Carlos Tapia",
                tipoExamen = "Tomografía de Coherencia Óptica (OCT)",
                fecha = "21/03/2026",
                medicoSolicitante = "Dr. Roberto Silva",
                estado = "Completado",
                observaciones = "Grosor macular normal en ambos ojos.",
                ojoEvaluado = "Ambos"
            )
        )

        private val pacientesSimulados = listOf(
            Paciente(
                id = "PAC-001",
                rut = "18.234.567-8",
                nombreCompleto = "Juan Pérez",
                edad = 45,
                prevision = "Fonasa"
            ),
            Paciente(
                id = "PAC-002",
                rut = "19.876.543-2",
                nombreCompleto = "María González",
                edad = 32,
                prevision = "Isapre"
            ),
            Paciente(
                id = "PAC-003",
                rut = "17.111.222-3",
                nombreCompleto = "Carlos Tapia",
                edad = 58,
                prevision = "Fonasa"
            )
        )
    }
}
