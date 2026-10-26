package com.equipo7.oftaapp.repository

import androidx.room.withTransaction
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.aDominio
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PacienteRepository(private val database: AppDatabase) {
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
        val nombre = paciente.nombreCompleto.trim()
        val rut = Validadores.rutFormateado(paciente.rut)
        val fecha = paciente.fechaNacimiento.trim()
        val fechaIso = Fechas.aIso(fecha) ?: fecha
        val correo = paciente.correo.trim()
        val telefono = Validadores.telefonoNormalizado(paciente.telefonoPrincipal)
        val telefonoSecundario = paciente.telefonoSecundario?.trim()?.takeIf { it.isNotBlank() }
            ?.let(Validadores::telefonoNormalizado)

        require(Validadores.rutValido(rut)) { "El RUT ingresado no es válido." }
        require(Validadores.nombreValido(nombre)) { "El nombre debe tener entre 3 y 80 caracteres." }
        require(Fechas.parseIso(fechaIso) != null && !Fechas.fechaFutura(fechaIso) && Fechas.edad(fechaIso) <= 120) {
            "La fecha de nacimiento no es válida."
        }
        require(correo.isBlank() || Validadores.correoValido(correo)) { "El correo no es válido." }
        require(Validadores.telefonoValido(telefono)) { "El teléfono debe ser un celular chileno válido." }
        require(telefonoSecundario == null || Validadores.telefonoValido(telefonoSecundario)) {
            "El teléfono secundario no es válido."
        }

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
            prevision = paciente.prevision.trim().ifBlank { "Sin especificar" },
            activo = true
        )
        database.withTransaction {
            if (idNuevo) {
                database.pacienteDao().insertar(entity)
            } else {
                database.pacienteDao().actualizar(entity)
            }
        }
        id
    }
}
