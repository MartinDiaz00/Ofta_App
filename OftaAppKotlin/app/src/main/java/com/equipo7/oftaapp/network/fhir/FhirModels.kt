package com.equipo7.oftaapp.network.fhir

import com.google.gson.annotations.SerializedName

/**
 * Modelos mínimos FHIR R4 (JSON) para interoperar con el microservicio HAPI FHIR.
 * Se usan con Retrofit+Gson; respetan estructura FHIR (resourceType, referencias, codificaciones).
 */
data class FhirBundle(
    val resourceType: String? = null,
    val type: String? = null,
    val total: Int? = null,
    val entry: List<FhirBundleEntry> = emptyList()
)

data class FhirBundleEntry(
    val fullUrl: String? = null,
    val resource: FhirAnyResource? = null
)

data class FhirPatientBundle(
    val resourceType: String? = null,
    val type: String? = null,
    val total: Int? = null,
    val entry: List<FhirPatientEntry> = emptyList()
)
data class FhirPatientEntry(val resource: FhirPatient? = null)

data class FhirDiagnosticReportBundle(
    val resourceType: String? = null,
    val type: String? = null,
    val total: Int? = null,
    val entry: List<FhirDiagnosticReportEntry> = emptyList()
)
data class FhirDiagnosticReportEntry(val resource: FhirDiagnosticReport? = null)

/** Contenedor genérico para recursos embebidos en Bundle; Gson usa resourceType para desambiguar en uso manual. */
data class FhirAnyResource(
    val resourceType: String? = null,
    val id: String? = null,
    val status: String? = null,
    val name: List<FhirHumanName>? = null,
    val identifier: List<FhirIdentifier>? = null,
    val telecom: List<FhirContactPoint>? = null,
    val birthDate: String? = null,
    val subject: FhirReference? = null,
    val code: FhirCodeableConcept? = null,
    val conclusion: String? = null,
    val effectiveDateTime: String? = null,
    val issued: String? = null,
    val performer: List<FhirReference>? = null
)

data class FhirPatient(
    val resourceType: String = "Patient",
    val id: String? = null,
    val identifier: List<FhirIdentifier>? = null,
    val name: List<FhirHumanName>? = null,
    val telecom: List<FhirContactPoint>? = null,
    val birthDate: String? = null,
    val active: Boolean? = true
)

data class FhirDiagnosticReport(
    val resourceType: String = "DiagnosticReport",
    val id: String? = null,
    /** Identificador estable del examen local: permite reutilizar el mismo recurso FHIR en cada guardado. */
    val identifier: List<FhirIdentifier>? = null,
    val status: String? = "registered",
    val code: FhirCodeableConcept? = null,
    val subject: FhirReference? = null,
    val effectiveDateTime: String? = null,
    val issued: String? = null,
    val conclusion: String? = null,
    val performer: List<FhirReference>? = null
)

data class FhirHumanName(
    val use: String? = "official",
    val text: String? = null,
    val family: String? = null,
    val given: List<String>? = null
)

data class FhirIdentifier(
    val system: String? = null,
    val value: String? = null
)

data class FhirContactPoint(
    val system: String? = null, // phone | email
    val value: String? = null,
    val use: String? = null
)

data class FhirReference(
    val reference: String? = null,
    val display: String? = null
)

data class FhirCodeableConcept(
    val text: String? = null,
    val coding: List<FhirCoding>? = null
)

data class FhirCoding(
    val system: String? = null,
    val code: String? = null,
    val display: String? = null
)

data class FhirOperationOutcome(
    val resourceType: String? = null,
    val issue: List<FhirIssue>? = null
)

data class FhirIssue(
    val severity: String? = null,
    val code: String? = null,
    val diagnostics: String? = null
)
