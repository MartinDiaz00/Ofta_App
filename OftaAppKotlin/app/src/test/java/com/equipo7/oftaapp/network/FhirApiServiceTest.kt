package com.equipo7.oftaapp.network

import com.equipo7.oftaapp.network.api.FhirApiService
import com.equipo7.oftaapp.network.fhir.FhirCodeableConcept
import com.equipo7.oftaapp.network.fhir.FhirDiagnosticReport
import com.equipo7.oftaapp.network.fhir.FhirPatient
import com.equipo7.oftaapp.network.fhir.FhirReference
import com.google.gson.GsonBuilder
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Pruebas de comunicación HTTP (Retrofit) contra respuestas FHIR JSON.
 * Verifica solicitudes, serialización y procesamiento de respuestas/errores.
 */
class FhirApiServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var api: FhirApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val gson = GsonBuilder().setLenient().create()
        api = Retrofit.Builder()
            .baseUrl(server.url("/fhir/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FhirApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun createPatientEnviaFhirJsonYProcesaRespuesta() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"resourceType":"Patient","id":"101","name":[{"text":"Camila Rojas"}],"birthDate":"1995-03-12","active":true}"""
            ).addHeader("Content-Type", "application/fhir+json")
        )
        val response = api.createPatient(
            FhirPatient(
                name = listOf(com.equipo7.oftaapp.network.fhir.FhirHumanName(text = "Camila Rojas")),
                birthDate = "1995-03-12"
            )
        )
        assertTrue(response.isSuccessful)
        assertEquals("101", response.body()?.id)
        assertEquals("Patient", response.body()?.resourceType)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.startsWith("/fhir/Patient"))
        assertTrue(request.body.readUtf8().contains("\"resourceType\":\"Patient\""))
    }

    @Test
    fun searchDiagnosticReportsProcesaBundleFhir() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"resourceType":"Bundle","type":"searchset","total":1,"entry":[{"resource":{"resourceType":"DiagnosticReport","id":"201","status":"final","subject":{"reference":"Patient/101"}}}]}"""
            ).addHeader("Content-Type", "application/fhir+json")
        )
        val response = api.searchDiagnosticReports(subject = "Patient/101")
        assertTrue(response.isSuccessful)
        val body = response.body()
        assertEquals("Bundle", body?.resourceType)
        assertEquals(1, body?.total)
        assertEquals("201", body?.entry?.firstOrNull()?.resource?.id)
        assertEquals("final", body?.entry?.firstOrNull()?.resource?.status)
    }

    @Test
    fun createDiagnosticReportManejaErrorHttp() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"resourceType":"OperationOutcome","issue":[{"severity":"error","diagnostics":"DiagnosticReport.subject (paciente) es obligatorio."}]}"""
            ).addHeader("Content-Type", "application/fhir+json")
        )
        val response = api.createDiagnosticReport(
            FhirDiagnosticReport(
                status = "registered",
                code = FhirCodeableConcept(text = "Fondo de ojo"),
                subject = FhirReference(reference = "Patient/101")
            )
        )
        assertFalse(response.isSuccessful)
        assertEquals(400, response.code())
        assertTrue(response.errorBody()?.string()?.contains("OperationOutcome") == true)
    }

    @Test
    fun capabilityStatementConsultaMetadataFhir() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"resourceType":"CapabilityStatement","fhirVersion":"4.0.1","status":"active"}"""
            ).addHeader("Content-Type", "application/fhir+json")
        )
        val response = api.capabilityStatement()
        assertTrue(response.isSuccessful)
        assertEquals("GET", server.takeRequest().method)
    }
}
