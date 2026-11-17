package com.equipo7.oftaapp.network.fhir

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas puras de mapeo dominio <-> FHIR R4 (sin Android ni red). */
class FhirMappersTest {

    private fun paciente() = Paciente(
        id = "pac-local-1",
        rut = "15.790.867-2",
        nombreCompleto = "Camila Rojas",
        fechaNacimiento = "12/03/1995",
        correo = "camila@example.com",
        telefonoPrincipal = "+56912345678",
        prevision = "Fonasa"
    )

    private fun examen(estado: EstadoExamen = EstadoExamen.VALIDADO) = ExamenOftalmologico(
        id = "exa-local-1",
        pacienteId = "pac-local-1",
        pacienteNombre = "Camila Rojas",
        pacienteRut = "15.790.867-2",
        tipoExamen = "Fondo de ojo",
        fecha = "2026-05-10",
        hora = "10:30",
        sucursal = "Sucursal Centro",
        profesional = "Dra. Paz",
        ojoEvaluado = "Ambos",
        estado = estado,
        observaciones = "Sin hallazgos relevantes."
    )

    @Test
    fun pacienteToFhirUsaRutComoIdentificadorYFechaIso() {
        val fhir = FhirMappers.pacienteToFhir(paciente())

        assertEquals("Patient", fhir.resourceType)
        assertNull("El id local no debe enviarse como id FHIR", fhir.id)
        assertEquals(FhirMappers.identificadorRut("15.790.867-2"),
            "${fhir.identifier?.first()?.system}|${fhir.identifier?.first()?.value}")
        assertEquals("1995-03-12", fhir.birthDate)
        assertEquals("Camila Rojas", fhir.name?.first()?.text)
        assertEquals(2, fhir.telecom?.size)
    }

    @Test
    fun fhirToPacienteReconstruyeRutYFechaVisible() {
        val original = paciente()
        val reconstruido = FhirMappers.fhirToPaciente(FhirMappers.pacienteToFhir(original))

        assertEquals(original.rut, reconstruido.rut)
        assertEquals(original.nombreCompleto, reconstruido.nombreCompleto)
        assertEquals(original.fechaNacimiento, reconstruido.fechaNacimiento)
        assertEquals(original.correo, reconstruido.correo)
        assertEquals(original.telefonoPrincipal, reconstruido.telefonoPrincipal)
    }

    @Test
    fun examenToFhirIncluyeIdentificadorEstableYEstado() {
        val fhir = FhirMappers.examenToFhir(examen())

        assertEquals("DiagnosticReport", fhir.resourceType)
        assertNull(fhir.id)
        assertEquals(FhirMappers.identificadorExamen("exa-local-1"),
            "${fhir.identifier?.first()?.system}|${fhir.identifier?.first()?.value}")
        assertEquals("final", fhir.status)
        assertEquals("2026-05-10T10:30:00Z", fhir.effectiveDateTime)
        assertTrue(fhir.conclusion!!.contains("Sucursal: Sucursal Centro"))
        assertTrue(fhir.conclusion!!.contains("Sin hallazgos relevantes."))
    }

    @Test
    fun mapeoDeEstadosEsSimetrico() {
        val pares = listOf(
            EstadoExamen.PENDIENTE to "registered",
            EstadoExamen.VALIDADO to "final",
            EstadoExamen.OBSERVADO to "corrected",
            EstadoExamen.ENTREGADO to "amended"
        )
        pares.forEach { (estado, codigo) ->
            assertEquals(codigo, FhirMappers.examenToFhir(examen(estado)).status)
            val reconstruido = FhirMappers.fhirToExamen(
                FhirMappers.examenToFhir(examen(estado)).copy(subject = FhirReference("Patient/1001"))
            )
            assertEquals(estado, reconstruido.estado)
        }
    }

    @Test
    fun fhirToExamenReconstruyeDatosDeReporteDelServidor() {
        // Simula lo que devuelve HAPI: subject con id real del servidor y fecha con offset Z.
        val reporte = FhirDiagnosticReport(
            id = "2050",
            identifier = listOf(FhirIdentifier(FhirMappers.EXAM_ID_SYSTEM, "exa-local-1")),
            status = "amended",
            code = FhirCodeableConcept(text = "Fondo de ojo"),
            subject = FhirReference("Patient/1001", "Camila Rojas"),
            effectiveDateTime = "2026-05-10T10:30:00Z",
            conclusion = listOf(
                "Sin hallazgos relevantes.",
                "Sucursal: Sucursal Centro",
                "Ojo: Ambos",
                "Profesional: Dra. Paz"
            ).joinToString("\n")
        )

        val resultado = FhirMappers.fhirToExamen(reporte)

        assertEquals("exa-fhir-2050", resultado.id)
        assertEquals("pac-1001", resultado.pacienteId)
        assertEquals("Camila Rojas", resultado.pacienteNombre)
        assertEquals("2026-05-10", resultado.fecha)
        assertEquals("10:30", resultado.hora)
        assertEquals("Sucursal Centro", resultado.sucursal)
        assertEquals("Dra. Paz", resultado.profesional)
        assertEquals("Ambos", resultado.ojoEvaluado)
        assertEquals(EstadoExamen.ENTREGADO, resultado.estado)
        assertEquals("Sin hallazgos relevantes.", resultado.observaciones)
    }
}
