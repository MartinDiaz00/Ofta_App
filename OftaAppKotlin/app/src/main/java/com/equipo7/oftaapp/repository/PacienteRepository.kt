package com.equipo7.oftaapp.repository

import androidx.room.withTransaction
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.aDominio
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.network.FhirRemoteService
import com.equipo7.oftaapp.network.SincronizacionMonitor
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class PacienteRepository(
    private val database: AppDatabase,
    private val fhirRemoteService: FhirRemoteService? = null
) {
    fun observarTodos(): Flow<List<Paciente>> =
        database.pacienteDao().observarTodos().map { pacientes ->
            pacientes.map { it.aDominio() }
        }

    fun observarPorId(id: String): Flow<Paciente?> =
        database.pacienteDao().observarPorId(id).map { it?.aDominio() }

    suspend fun obtenerPorId(id: String): Paciente? =
        database.pacienteDao().obtenerPorId(id)?.aDominio()

    suspend fun contar(): Int = database.pacienteDao().contar()

    suspend fun guardar(paciente: Paciente): Result<String> = runCatching {
        val nombre = paciente.nombreCompleto.trim().replace(Regex("\\s+"), " ")
        val rut = Validadores.rutFormateado(paciente.rut)
        val fecha = paciente.fechaNacimiento.trim()
        val fechaIso = Fechas.aIso(fecha) ?: fecha
        val correo = paciente.correo.trim()
        val telefono = Validadores.telefonoNormalizado(paciente.telefonoPrincipal)
        val telefonoSecundario = paciente.telefonoSecundario?.trim()?.takeIf { it.isNotBlank() }
            ?.let(Validadores::telefonoNormalizado)
        val prevision = paciente.prevision.trim()

        require(Validadores.rutValido(rut)) { "El RUT ingresado no es válido (formato xx.xxx.xxx-x)." }
        require(Validadores.nombreValido(nombre)) { "El nombre debe ser válido (solo letras, 3 a 80 caracteres)." }
        require(Fechas.parseIso(fechaIso) != null && !Fechas.fechaFutura(fechaIso) && Fechas.edad(fechaIso) <= 120) {
            "La fecha de nacimiento no es válida."
        }
        require(correo.isBlank() || Validadores.correoValido(correo)) { "El correo no es válido." }
        require(Validadores.telefonoValido(telefono)) { "El teléfono debe ser un celular chileno válido (ej. 912345678 o +56912345678)." }
        require(telefonoSecundario == null || Validadores.telefonoValido(telefonoSecundario)) {
            "El teléfono secundario no es válido."
        }
        require(Validadores.previsionValida(prevision)) { "La previsión es obligatoria." }

        val idNuevo = paciente.id.isBlank()
        val id = paciente.id.ifBlank { "pac-${UUID.randomUUID()}" }
        if (database.pacienteDao().existeRut(rut, if (idNuevo) "" else id)) {
            error("Ya existe un paciente registrado con ese RUT.")
        }

        val entity = PacienteEntity(
            id = id,
            rut = rut,
            nombreCompleto = nombre,
            fechaNacimiento = fechaIso,
            correo = correo,
            telefonoPrincipal = telefono,
            telefonoSecundario = telefonoSecundario,
            prevision = prevision,
            activo = true
        )
        database.withTransaction {
            if (idNuevo) {
                database.pacienteDao().insertar(entity)
            } else {
                database.pacienteDao().actualizar(entity)
            }
        }
        // Integración API/FHIR (best-effort): registra/actualiza Patient en HAPI FHIR vía Retrofit.
        // El guardado local ya se completó; un fallo de red solo se avisa, nunca se propaga.
        fhirRemoteService?.sincronizarPaciente(entity.aDominio())?.let { resultado ->
            resultado.onSuccess { SincronizacionMonitor.exito() }
                .onFailure { fallo ->
                    SincronizacionMonitor.pendiente(
                        "Paciente guardado en el dispositivo. Sincronización FHIR pendiente: " +
                            (fallo.message ?: "sin conexión").take(110)
                    )
                }
        }
        id
    }

    /** Reenvía todos los pacientes locales al servidor FHIR (idempotente: reutiliza por RUT). */
    suspend fun sincronizarPendentes(): Result<Int> = runCatching {
        val servicio = fhirRemoteService ?: error("Sincronización FHIR no configurada.")
        var total = 0
        database.pacienteDao().observarTodos().first().forEach { entidad ->
            servicio.sincronizarPaciente(entidad.aDominio()).getOrThrow()
            total++
        }
        total
    }
}
