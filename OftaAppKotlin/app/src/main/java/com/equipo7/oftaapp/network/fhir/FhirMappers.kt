package com.equipo7.oftaapp.network.fhir

import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.util.Fechas

/**
 * Mapeo dominio local <-> recursos FHIR R4 (compatibles con HAPI FHIR del backend).
 * - Paciente -> Patient (identifier RUT, name, birthDate, telecom)
 * - ExamenOftalmologico -> DiagnosticReport (subject, status, code/tipo, conclusion, effectiveDateTime)
 */
object FhirMappers {
    /** Sistemas de identificadores usados para reutilizar recursos FHIR en lugar de crear duplicados. */
    const val RUT_SYSTEM = "https://oftaapp.demo/identifier/rut"
    const val EXAM_ID_SYSTEM = "https://oftaapp.demo/identifier/examen"
    private const val EXAM_SYSTEM = "https://oftaapp.demo/exam-type"

    fun identificadorRut(rut: String): String = "$RUT_SYSTEM|$rut"
    fun identificadorExamen(id: String): String = "$EXAM_ID_SYSTEM|$id"

    fun pacienteToFhir(p: Paciente): FhirPatient {
        val partes = p.nombreCompleto.trim().split(Regex("\\s+"), limit = 2)
        val given = if (partes.isNotEmpty()) listOf(partes[0]) else listOf(p.nombreCompleto)
        val family = if (partes.size > 1) partes[1] else null
        return FhirPatient(
            id = p.id.takeIf { it.isNotBlank() && !it.startsWith("pac-") },
            identifier = listOf(FhirIdentifier(system = RUT_SYSTEM, value = p.rut)),
            name = listOf(FhirHumanName(text = p.nombreCompleto, family = family, given = given)),
            telecom = buildList {
                if (p.correo.isNotBlank()) add(FhirContactPoint(system = "email", value = p.correo))
                if (p.telefonoPrincipal.isNotBlank()) add(FhirContactPoint(system = "phone", value = p.telefonoPrincipal))
            },
            birthDate = Fechas.aIso(p.fechaNacimiento) ?: p.fechaNacimiento.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) },
            active = p.activo
        )
    }

    fun fhirToPaciente(f: FhirPatient, fallbackId: String = ""): Paciente {
        val nombre = f.name?.firstOrNull()?.let { n ->
            n.text ?: listOfNotNull(n.given?.joinToString(" "), n.family).joinToString(" ").trim()
        }.orEmpty().ifBlank { "Paciente FHIR" }
        val rut = f.identifier?.firstOrNull { it.system == RUT_SYSTEM }?.value
            ?: f.identifier?.firstOrNull()?.value.orEmpty()
        val correo = f.telecom?.firstOrNull { it.system == "email" }?.value.orEmpty()
        val telefono = f.telecom?.firstOrNull { it.system == "phone" }?.value.orEmpty()
        return Paciente(
            id = f.id ?: fallbackId,
            rut = rut.ifBlank { "SIN-RUT-${f.id ?: fallbackId}" },
            nombreCompleto = nombre,
            fechaNacimiento = Fechas.aVisible(f.birthDate ?: "").ifBlank { f.birthDate ?: "" },
            correo = correo,
            telefonoPrincipal = telefono,
            prevision = "Sin especificar",
            activo = f.active ?: true
        )
    }

    fun examenToFhir(e: ExamenOftalmologico): FhirDiagnosticReport {
        val iso = Fechas.aIso(e.fecha) ?: e.fecha
        val effective = if (e.hora.isNotBlank() && iso.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            "${iso}T${e.hora}:00Z" else iso
        return FhirDiagnosticReport(
            id = e.id.takeIf { it.isNotBlank() && !it.startsWith("exa-") },
            identifier = listOf(FhirIdentifier(system = EXAM_ID_SYSTEM, value = e.id)),
            status = estadoToFhir(e.estado),
            code = FhirCodeableConcept(
                text = e.tipoExamen,
                coding = listOf(FhirCoding(system = EXAM_SYSTEM, code = e.tipoExamen.replace(" ", "_"), display = e.tipoExamen))
            ),
            subject = FhirReference(reference = "Patient/${e.pacienteId.removePrefix("pac-")}", display = e.pacienteNombre),
            effectiveDateTime = effective,
            conclusion = listOf(e.observaciones, "Sucursal: ${e.sucursal}", "Ojo: ${e.ojoEvaluado}", "Profesional: ${e.profesional}")
                .filter { it.isNotBlank() && it != "Sucursal: " && it != "Ojo: " && it != "Profesional: " }
                .joinToString("\n"),
            performer = listOf(FhirReference(display = e.profesional))
        )
    }

    fun fhirToExamen(f: FhirDiagnosticReport, fallbackPacienteNombre: String = ""): ExamenOftalmologico {
        val subjectRef = f.subject?.reference.orEmpty()
        val pacienteId = subjectRef.removePrefix("Patient/").let { if (it.isBlank()) "" else "pac-$it" }
        val effective = f.effectiveDateTime ?: ""
        val fechaIso = effective.take(10).takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: effective
        val hora = if (effective.length >= 16 && effective.contains('T')) effective.substring(11, 16) else "00:00"
        val conclusion = f.conclusion.orEmpty()
        return ExamenOftalmologico(
            id = f.id?.let { "exa-fhir-$it" } ?: "",
            pacienteId = pacienteId,
            pacienteNombre = f.subject?.display ?: fallbackPacienteNombre,
            pacienteRut = "",
            tipoExamen = f.code?.text ?: f.code?.coding?.firstOrNull()?.display ?: "Examen oftalmológico",
            fecha = fechaIso,
            hora = hora,
            sucursal = extraerLinea(conclusion, "Sucursal:"),
            profesional = f.performer?.firstOrNull()?.display ?: extraerLinea(conclusion, "Profesional:"),
            ojoEvaluado = extraerLinea(conclusion, "Ojo:").ifBlank { "No especificado" },
            estado = fhirToEstado(f.status),
            observaciones = conclusion.lines().firstOrNull { !it.startsWith("Sucursal:") && !it.startsWith("Ojo:") && !it.startsWith("Profesional:") }.orEmpty()
        )
    }

    private fun estadoToFhir(estado: EstadoExamen): String = when (estado) {
        EstadoExamen.PENDIENTE -> "registered"
        EstadoExamen.VALIDADO -> "final"
        EstadoExamen.OBSERVADO -> "corrected"
        EstadoExamen.ENTREGADO -> "amended"
    }

    private fun fhirToEstado(status: String?): EstadoExamen = when (status?.lowercase()) {
        "final" -> EstadoExamen.VALIDADO
        "corrected", "preliminary" -> EstadoExamen.OBSERVADO
        "amended", "appended" -> EstadoExamen.ENTREGADO
        else -> EstadoExamen.PENDIENTE
    }

    private fun extraerLinea(texto: String, prefijo: String): String =
        texto.lines().firstOrNull { it.startsWith(prefijo) }?.removePrefix(prefijo)?.trim().orEmpty()
}
