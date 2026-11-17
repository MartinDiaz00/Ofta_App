package com.equipo7.oftaapp.network

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.network.api.FhirApiService
import com.equipo7.oftaapp.network.fhir.FhirDiagnosticReport
import com.equipo7.oftaapp.network.fhir.FhirDiagnosticReportBundle
import com.equipo7.oftaapp.network.fhir.FhirDiagnosticReportEntry
import com.equipo7.oftaapp.network.fhir.FhirMappers
import com.equipo7.oftaapp.network.fhir.FhirPatient
import com.equipo7.oftaapp.network.fhir.FhirPatientBundle
import com.equipo7.oftaapp.network.fhir.FhirPatientEntry
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas de la lógica de sincronización con un API falso (sin Android ni red):
 * reutilización de recursos, referencias al Patient real y propagación de errores HTTP.
 */
class FhirRemoteServiceTest {

    private class ApiFalso : FhirApiService {
        var pacientes: FhirPatientBundle = FhirPatientBundle()
        var reportes: FhirDiagnosticReportBundle = FhirDiagnosticReportBundle()
        var pacienteCreado: Response<FhirPatient> = Response.success(FhirPatient(id = "1002"))
        var pacienteActualizado: Response<FhirPatient> = Response.success(FhirPatient(id = "1001"))
        var reporteCreado: Response<FhirDiagnosticReport> = Response.success(FhirDiagnosticReport(id = "2002"))
        var reporteActualizado: Response<FhirDiagnosticReport> = Response.success(FhirDiagnosticReport(id = "2001"))

        val eventos = mutableListOf<String>()
        var ultimoPaciente: FhirPatient? = null
        var ultimoReporte: FhirDiagnosticReport? = null

        override suspend fun searchPatients(name: String?, identifier: String?): Response<FhirPatientBundle> {
            eventos += "searchPatients:$identifier"
            return Response.success(pacientes)
        }

        override suspend fun getPatient(id: String): Response<FhirPatient> = Response.success(FhirPatient(id = id))

        override suspend fun createPatient(patient: FhirPatient): Response<FhirPatient> {
            eventos += "createPatient"
            ultimoPaciente = patient
            return pacienteCreado
        }

        override suspend fun updatePatient(id: String, patient: FhirPatient): Response<FhirPatient> {
            eventos += "updatePatient:$id"
            ultimoPaciente = patient
            return pacienteActualizado
        }

        override suspend fun searchDiagnosticReports(
            subject: String?,
            status: String?,
            identifier: String?
        ): Response<FhirDiagnosticReportBundle> {
            eventos += "searchReports:$identifier"
            return Response.success(reportes)
        }

        override suspend fun getDiagnosticReport(id: String): Response<FhirDiagnosticReport> =
            Response.success(FhirDiagnosticReport(id = id))

        override suspend fun createDiagnosticReport(report: FhirDiagnosticReport): Response<FhirDiagnosticReport> {
            eventos += "createReport"
            ultimoReporte = report
            return reporteCreado
        }

        override suspend fun updateDiagnosticReport(
            id: String,
            report: FhirDiagnosticReport
        ): Response<FhirDiagnosticReport> {
            eventos += "updateReport:$id"
            ultimoReporte = report
            return reporteActualizado
        }

        override suspend fun capabilityStatement(): Response<Any> = Response.success(Any())
    }

    private fun paciente() = Paciente(
        id = "pac-local",
        rut = "15.790.867-2",
        nombreCompleto = "Camila Rojas",
        fechaNacimiento = "12/03/1995",
        correo = "camila@example.com",
        telefonoPrincipal = "+56912345678",
        prevision = "Fonasa"
    )

    private fun examen() = ExamenOftalmologico(
        id = "exa-local",
        pacienteId = "pac-local",
        pacienteNombre = "Camila Rojas",
        pacienteRut = "15.790.867-2",
        tipoExamen = "Fondo de ojo",
        fecha = "2026-05-10",
        hora = "10:30",
        sucursal = "Centro",
        profesional = "Dra. Paz",
        ojoEvaluado = "Ambos",
        estado = EstadoExamen.VALIDADO,
        observaciones = "Sin hallazgos."
    )

    @Test
    fun pacienteSeActualizaSiYaExisteEnFhir() = runTest {
        val api = ApiFalso().apply {
            pacientes = FhirPatientBundle(entry = listOf(FhirPatientEntry(resource = FhirPatient(id = "1001"))))
        }
        val resultado = FhirRemoteService(api).sincronizarPaciente(paciente())

        assertTrue(resultado.isSuccess)
        assertEquals("1001", resultado.getOrNull())
        assertTrue(api.eventos.contains("updatePatient:1001"))
        assertFalse(api.eventos.contains("createPatient"))
        assertEquals("1001", api.ultimoPaciente?.id)
    }

    @Test
    fun pacienteSeCreaSiNoExisteEnFhir() = runTest {
        val api = ApiFalso()
        val resultado = FhirRemoteService(api).sincronizarPaciente(paciente())

        assertTrue(resultado.isSuccess)
        assertEquals("1002", resultado.getOrNull())
        assertTrue(api.eventos.contains("createPatient"))
        assertNull(api.ultimoPaciente?.id)
        assertTrue(api.eventos.first().contains(FhirMappers.identificadorRut("15.790.867-2")))
    }

    @Test
    fun examenActualizaElReporteYApuntaAlPatientRemoto() = runTest {
        val api = ApiFalso().apply {
            pacientes = FhirPatientBundle(entry = listOf(FhirPatientEntry(resource = FhirPatient(id = "1001"))))
            reportes = FhirDiagnosticReportBundle(
                entry = listOf(FhirDiagnosticReportEntry(resource = FhirDiagnosticReport(id = "2001")))
            )
        }
        val resultado = FhirRemoteService(api).sincronizarExamen(examen())

        assertTrue(resultado.isSuccess)
        assertEquals("2001", resultado.getOrNull())
        assertTrue(api.eventos.contains("updateReport:2001"))
        assertFalse(api.eventos.contains("createReport"))
        assertEquals("2001", api.ultimoReporte?.id)
        assertEquals("Patient/1001", api.ultimoReporte?.subject?.reference)
        assertEquals(
            FhirMappers.identificadorExamen("exa-local"),
            "${api.ultimoReporte?.identifier?.first()?.system}|${api.ultimoReporte?.identifier?.first()?.value}"
        )
        assertTrue(api.eventos.any { it.contains(FhirMappers.identificadorExamen("exa-local")) })
    }

    @Test
    fun examenCreaElPatientReferenciadoSiNoExiste() = runTest {
        val api = ApiFalso()
        val resultado = FhirRemoteService(api).sincronizarExamen(examen())

        assertTrue(resultado.isSuccess)
        assertEquals("2002", resultado.getOrNull())
        assertTrue(api.eventos.contains("createPatient"))
        assertTrue(api.eventos.contains("createReport"))
        assertEquals("Patient/1002", api.ultimoReporte?.subject?.reference)
    }

    @Test
    fun errorHttpSePropagaComoFailure() = runTest {
        val api = ApiFalso().apply {
            pacienteCreado = Response.error(500, "{}".toResponseBody("application/json".toMediaType()))
        }
        val resultado = FhirRemoteService(api).sincronizarPaciente(paciente())

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message?.contains("500") == true)
    }
}
