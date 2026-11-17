package com.equipo7.oftaapp.network

import android.util.Log
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.network.api.FhirApiService
import com.equipo7.oftaapp.network.fhir.FhirIdentifier
import com.equipo7.oftaapp.network.fhir.FhirMappers
import com.equipo7.oftaapp.network.fhir.FhirReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Capa remota: usa Retrofit (FhirApiService) contra el microservicio HAPI FHIR.
 * Maneja respuestas HTTP/errores y convierte entre dominio y recursos FHIR R4.
 *
 * Los recursos se identifican de forma estable (RUT para Patient, id local para
 * DiagnosticReport): antes de crear se busca por identificador y, si existe, se
 * actualiza con PUT. Así guardar varias veces el mismo paciente/examen no genera
 * duplicados en el servidor FHIR.
 */
class FhirRemoteService(private val api: FhirApiService = FhirNetworkModule.api) {
    private val tag = "FhirRemote"

    data class EstadoConexionFhir(
        val ok: Boolean,
        val mensaje: String,
        val detalle: String? = null
    )

    suspend fun verificarConexion(): Result<EstadoConexionFhir> = withContext(Dispatchers.IO) {
        runCatching {
            val meta = api.capabilityStatement()
            if (!meta.isSuccessful) falloHttp("metadata", meta)

            val pacientesRes = runCatching { api.searchPatients() }.getOrNull()
            val examenesRes = runCatching { api.searchDiagnosticReports() }.getOrNull()

            val pacientesTotal = if (pacientesRes?.isSuccessful == true) {
                pacientesRes.body()?.total ?: pacientesRes.body()?.entry?.size ?: 0
            } else null
            val examenesTotal = if (examenesRes?.isSuccessful == true) {
                examenesRes.body()?.total ?: examenesRes.body()?.entry?.size ?: 0
            } else null

            val detalles = buildString {
                appendLine("Endpoint: ${FhirNetworkModule.baseUrl()}")
                appendLine("CapabilityStatement (/metadata): OK (HTTP ${meta.code()})")
                if (pacientesTotal != null) appendLine("Patients en servidor FHIR: $pacientesTotal")
                else appendLine("Patients en servidor FHIR: no disponible (${pacientesRes?.code() ?: "sin respuesta"})")
                if (examenesTotal != null) append("DiagnosticReports en servidor FHIR: $examenesTotal")
                else append("DiagnosticReports en servidor FHIR: no disponible (${examenesRes?.code() ?: "sin respuesta"})")
            }

            EstadoConexionFhir(
                ok = true,
                mensaje = "FHIR OK (${FhirNetworkModule.baseUrl()})",
                detalle = detalles
            )
        }
    }

    suspend fun sincronizarPaciente(paciente: Paciente): Result<String?> = withContext(Dispatchers.IO) {
        runCatching {
            val idRemoto = buscarIdPaciente(paciente.rut)
            val body = FhirMappers.pacienteToFhir(paciente).copy(id = idRemoto)
            val res = if (idRemoto.isNullOrBlank()) api.createPatient(body)
            else api.updatePatient(idRemoto, body)
            if (!res.isSuccessful) falloHttp("paciente", res)
            res.body()?.id ?: idRemoto
        }
    }

    suspend fun sincronizarExamen(examen: ExamenOftalmologico): Result<String?> = withContext(Dispatchers.IO) {
        runCatching {
            val idPacienteRemoto = asegurarPacienteRemoto(examen)
            val idRemoto = buscarIdExamen(examen.id)
            val body = FhirMappers.examenToFhir(examen).copy(
                id = idRemoto,
                identifier = listOf(
                    FhirIdentifier(system = FhirMappers.EXAM_ID_SYSTEM, value = examen.id)
                ),
                subject = FhirReference(
                    reference = "Patient/$idPacienteRemoto",
                    display = examen.pacienteNombre
                )
            )
            val res = if (idRemoto.isNullOrBlank()) api.createDiagnosticReport(body)
            else api.updateDiagnosticReport(idRemoto, body)
            if (!res.isSuccessful) falloHttp("examen", res)
            res.body()?.id ?: idRemoto
        }
    }

    suspend fun listarPacientesRemotos(): Result<List<Paciente>> = withContext(Dispatchers.IO) {
        runCatching {
            val res = api.searchPatients()
            if (!res.isSuccessful) falloHttp("listado de pacientes", res)
            res.body()?.entry.orEmpty().mapNotNull { it.resource?.let { p -> FhirMappers.fhirToPaciente(p) } }
        }
    }

    /** Busca el Patient por RUT; la búsqueda es best-effort para no romper servidores que no soporten el parámetro. */
    private suspend fun buscarIdPaciente(rut: String): String? {
        if (rut.isBlank()) return null
        val res = runCatching { api.searchPatients(identifier = FhirMappers.identificadorRut(rut)) }.getOrNull()
        if (res?.isSuccessful != true) {
            if (res != null) Log.w(tag, "búsqueda de Patient por RUT omitida (HTTP ${res.code()})")
            return null
        }
        return res.body()?.entry?.firstOrNull()?.resource?.id
    }

    private suspend fun buscarIdExamen(id: String): String? {
        if (id.isBlank()) return null
        val res = runCatching {
            api.searchDiagnosticReports(identifier = FhirMappers.identificadorExamen(id))
        }.getOrNull()
        if (res?.isSuccessful != true) {
            if (res != null) Log.w(tag, "búsqueda de DiagnosticReport por identificador omitida (HTTP ${res.code()})")
            return null
        }
        return res.body()?.entry?.firstOrNull()?.resource?.id
    }

    /** Garantiza que el Patient referenciado por el examen exista y devuelve su id FHIR real. */
    private suspend fun asegurarPacienteRemoto(examen: ExamenOftalmologico): String {
        buscarIdPaciente(examen.pacienteRut)?.let { return it }

        val paciente = Paciente(
            id = examen.pacienteId,
            rut = examen.pacienteRut.ifBlank { "SIN-RUT-${examen.pacienteId}" },
            nombreCompleto = examen.pacienteNombre.ifBlank { "Paciente FHIR" },
            fechaNacimiento = "",
            correo = "",
            telefonoPrincipal = "",
            prevision = "Sin especificar"
        )
        val res = api.createPatient(FhirMappers.pacienteToFhir(paciente))
        if (!res.isSuccessful) falloHttp("paciente del examen", res)
        return res.body()?.id ?: error("El servidor FHIR no devolvió el id del Patient.")
    }

    private fun falloHttp(operacion: String, res: Response<*>): Nothing {
        val detalle = res.errorBody()?.string().orEmpty().take(180)
        Log.w(tag, "sync ${operacion} falló HTTP ${res.code()}: $detalle")
        error("API FHIR ${operacion} HTTP ${res.code()}: $detalle")
    }
}
