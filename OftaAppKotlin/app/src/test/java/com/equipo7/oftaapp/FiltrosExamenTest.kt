package com.equipo7.oftaapp

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.FiltroExamenes
import com.equipo7.oftaapp.model.FiltrosExamen
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltrosExamenTest {
    private val examenes = listOf(
        examen(
            id = "1",
            paciente = "Camila Rojas",
            rut = "11.111.111-1",
            tipo = "Fondo de Ojo",
            sucursal = "Puerto Montt",
            fecha = "2026-10-10",
            estado = EstadoExamen.PENDIENTE
        ),
        examen(
            id = "2",
            paciente = "Matías González",
            rut = "22.222.222-2",
            tipo = "Campimetría Computarizada",
            sucursal = "Valdivia",
            fecha = "2026-09-15",
            estado = EstadoExamen.VALIDADO
        ),
        examen(
            id = "3",
            paciente = "Valentina Soto",
            rut = "33.333.333-3",
            tipo = "Fondo de Ojo",
            sucursal = "Puerto Montt",
            fecha = "2026-08-01",
            estado = EstadoExamen.OBSERVADO
        )
    )

    @Test fun filtraPorTextoEnPacienteTipoOSucursal() {
        assertEquals(
            listOf("2"),
            FiltroExamenes.aplicar(examenes, FiltrosExamen(texto = "campimetría")).map { it.id }
        )
        assertEquals(
            listOf("1", "3"),
            FiltroExamenes.aplicar(examenes, FiltrosExamen(texto = "Puerto")).map { it.id }
        )
    }

    @Test fun combinaEstadoTipoYSucursal() {
        val resultado = FiltroExamenes.aplicar(
            examenes,
            FiltrosExamen(
                estado = EstadoExamen.OBSERVADO,
                tipoExamen = "Fondo de Ojo",
                sucursal = "Puerto Montt"
            )
        )
        assertEquals(listOf("3"), resultado.map { it.id })
    }

    @Test fun filtraPorRangoDeFechas() {
        val resultado = FiltroExamenes.aplicar(
            examenes,
            FiltrosExamen(desde = "2026-09-01", hasta = "2026-10-31")
        )
        assertEquals(listOf("1", "2"), resultado.map { it.id })
    }

    @Test fun sinFiltrosDevuelveTodos() {
        assertEquals(examenes, FiltroExamenes.aplicar(examenes, FiltrosExamen()))
    }

    private fun examen(
        id: String,
        paciente: String,
        rut: String,
        tipo: String,
        sucursal: String,
        fecha: String,
        estado: EstadoExamen
    ) = ExamenOftalmologico(
        id = id,
        pacienteId = "pac-$id",
        pacienteNombre = paciente,
        pacienteRut = rut,
        tipoExamen = tipo,
        fecha = fecha,
        hora = "09:00",
        sucursal = sucursal,
        profesional = "Dra. Fernández",
        ojoEvaluado = "Derecho",
        estado = estado,
        observaciones = ""
    )
}
