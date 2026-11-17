package com.equipo7.oftaapp.network.api

import com.equipo7.oftaapp.network.fhir.FhirBundle
import com.equipo7.oftaapp.network.fhir.FhirDiagnosticReport
import com.equipo7.oftaapp.network.fhir.FhirPatient
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz Retrofit: comunicación HTTP desde Android hacia el microservicio HAPI FHIR (R4).
 * Base URL configurada en BuildConfig.FHIR_BASE_URL (sin localhost en dispositivos externos).
 */
interface FhirApiService {
    @GET("Patient")
    suspend fun searchPatients(
        @Query("name") name: String? = null,
        @Query("identifier") identifier: String? = null
    ): Response<com.equipo7.oftaapp.network.fhir.FhirPatientBundle>

    @GET("Patient/{id}")
    suspend fun getPatient(@Path("id") id: String): Response<FhirPatient>

    @POST("Patient")
    @Headers("Content-Type: application/fhir+json", "Accept: application/fhir+json")
    suspend fun createPatient(@Body patient: FhirPatient): Response<FhirPatient>

    @PUT("Patient/{id}")
    @Headers("Content-Type: application/fhir+json", "Accept: application/fhir+json")
    suspend fun updatePatient(@Path("id") id: String, @Body patient: FhirPatient): Response<FhirPatient>

    @GET("DiagnosticReport")
    suspend fun searchDiagnosticReports(
        @Query("subject") subject: String? = null,
        @Query("status") status: String? = null,
        @Query("identifier") identifier: String? = null
    ): Response<com.equipo7.oftaapp.network.fhir.FhirDiagnosticReportBundle>

    @GET("DiagnosticReport/{id}")
    suspend fun getDiagnosticReport(@Path("id") id: String): Response<FhirDiagnosticReport>

    @POST("DiagnosticReport")
    @Headers("Content-Type: application/fhir+json", "Accept: application/fhir+json")
    suspend fun createDiagnosticReport(@Body report: FhirDiagnosticReport): Response<FhirDiagnosticReport>

    @PUT("DiagnosticReport/{id}")
    @Headers("Content-Type: application/fhir+json", "Accept: application/fhir+json")
    suspend fun updateDiagnosticReport(
        @Path("id") id: String,
        @Body report: FhirDiagnosticReport
    ): Response<FhirDiagnosticReport>

    @GET("metadata")
    suspend fun capabilityStatement(): Response<Any>
}
