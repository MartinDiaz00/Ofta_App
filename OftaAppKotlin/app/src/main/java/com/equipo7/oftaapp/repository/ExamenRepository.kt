package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.OftaApplication
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.entity.ExamenEntity
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.data.local.entity.UsuarioEntity
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.util.Validadores
import java.util.UUID

/** Repositorio local para el MVP académico. No requiere Internet ni credenciales externas. */
class ExamenRepository {
    private val database: AppDatabase
        get() = AppDatabase.obtener(OftaApplication.instance)

    suspend fun inicializarDatosDemo() {
        if (database.pacienteDao().contar() > 0) return
        val pacientes = listOf(
            PacienteEntity("pac-001", "11.111.111-1", "Camila Rojas", "1995-03-12", "camila.rojas@example.test", "900000001", prevision = "Fonasa"),
            PacienteEntity("pac-002", "22.222.222-2", "Matías González", "1988-07-21", "matias.gonzalez@example.test", "900000002", prevision = "Isapre"),
            PacienteEntity("pac-003", "33.333.333-3", "Valentina Soto", "2001-11-04", "valentina.soto@example.test", "900000003", prevision = "Fonasa")
        )
        pacientes.forEach { database.pacienteDao().insertar(it) }
        val ejemplos = listOf(
            ExamenEntity("exa-001", "pac-001", "Tonometría de Aplanación", "15/10/2026", "10:30", "Puerto Montt", "Dra. Fernández", "Derecho", "Completado", "Registro de demostración."),
            ExamenEntity("exa-002", "pac-002", "Campimetría Computarizada", "18/10/2026", "11:00", "Puerto Montt", "Dr. Roberto Silva", "Ambos", "Pendiente", "Examen pendiente de revisión."),
            ExamenEntity("exa-003", "pac-003", "Fondo de ojo", "20/10/2026", "09:15", "Valdivia", "Dra. Fernández", "Izquierdo", "Pendiente", "Datos ficticios para probar la aplicación.")
        )
        ejemplos.forEach { database.examenDao().insertar(it) }
        val hashDemo = hashContrasena("1234")
        listOf(
            UsuarioEntity("usr-001", "dra.fernandez", hashDemo, "Dra. Fernández", "fernandez@example.test", "OFTALMOLOGO"),
            UsuarioEntity("usr-002", "dr.silva", hashDemo, "Dr. Roberto Silva", "silva@example.test", "OFTALMOLOGO"),
            UsuarioEntity("usr-003", "tens.martinez", hashDemo, "TENS Martínez", "martinez@example.test", "TENS"),
            UsuarioEntity("usr-004", "paciente.demo", hashDemo, "Paciente Demo", "paciente.demo@example.test", "PACIENTE", pacienteId = "pac-001")
        ).forEach { database.usuarioDao().insertar(it) }
    }

    suspend fun obtenerExamenes(): List<ExamenOftalmologico> =
        database.examenDao().listarConPaciente().map { row ->
            ExamenOftalmologico(
                id = row.id,
                pacienteNombre = row.pacienteNombre,
                tipoExamen = row.tipoExamen,
                fecha = row.fecha,
                medicoSolicitante = row.medicoSolicitante,
                estado = row.estado,
                observaciones = row.observaciones,
                ojoEvaluado = row.ojoEvaluado,
                sucursal = row.sucursal,
                documento = row.documento,
                documentoUri = row.documentoUri
            )
        }

    suspend fun obtenerPacientes(): List<Paciente> = database.pacienteDao().listar().map {
        Paciente(
            id = it.id,
            rut = it.rut,
            nombreCompleto = it.nombreCompleto,
            edad = edadDesde(it.fechaNacimiento),
            prevision = it.prevision
        )
    }

    suspend fun agregarExamen(examen: ExamenOftalmologico) {
        require(examen.pacienteNombre.isNotBlank()) { "Debes seleccionar un paciente." }
        require(examen.tipoExamen.isNotBlank()) { "Debes seleccionar el tipo de examen." }
        require(Validadores.fechaValida(examen.fecha)) { "La fecha debe tener el formato DD/MM/AAAA y ser válida." }
        val paciente = database.pacienteDao().obtenerPorNombre(examen.pacienteNombre)
            ?: PacienteEntity(
                id = "pac-${UUID.randomUUID()}",
                rut = "DEMO-${UUID.randomUUID().toString().take(8)}",
                nombreCompleto = examen.pacienteNombre.trim(),
                prevision = "Sin especificar"
            ).also { database.pacienteDao().insertar(it) }

        val idExamen = examen.id.takeIf { it.isNotBlank() && it != "DB" } ?: "exa-${UUID.randomUUID()}"
        database.examenDao().insertar(
            ExamenEntity(
                id = idExamen,
                pacienteId = paciente.id,
                tipoExamen = examen.tipoExamen.trim(),
                fecha = examen.fecha.trim(),
                sucursal = examen.sucursal.trim(),
                profesional = examen.medicoSolicitante.trim(),
                ojoEvaluado = examen.ojoEvaluado.trim(),
                estado = examen.estado.trim(),
                observaciones = examen.observaciones.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        if (examen.documentoUri.isNotBlank()) {
            database.documentoDao().insertar(
                DocumentoExamenEntity(
                    id = "doc-${UUID.randomUUID()}",
                    examenId = idExamen,
                    nombreOriginal = examen.documento,
                    rutaInterna = examen.documentoUri,
                    mimeType = "application/octet-stream",
                    tamanoBytes = 0L
                )
            )
        }
    }

    suspend fun examenesDePaciente(nombrePaciente: String): List<ExamenOftalmologico> =
        obtenerExamenes().filter { it.pacienteNombre.equals(nombrePaciente, ignoreCase = true) }

    fun siguienteId(): String = "exa-${UUID.randomUUID()}"

    suspend fun buscarPorTipo(filtro: String): List<ExamenOftalmologico> {
        val todos = obtenerExamenes()
        if (filtro.isBlank()) return todos
        return todos.filter {
            it.tipoExamen.contains(filtro, ignoreCase = true) ||
                it.pacienteNombre.contains(filtro, ignoreCase = true) ||
                it.estado.contains(filtro, ignoreCase = true) ||
                it.sucursal.contains(filtro, ignoreCase = true) ||
                it.fecha.contains(filtro, ignoreCase = true)
        }
    }

    private fun hashContrasena(valor: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(valor.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun edadDesde(fecha: String): Int = try {
        if (fecha.isBlank()) 0 else java.time.Period.between(java.time.LocalDate.parse(fecha), java.time.LocalDate.now()).years
    } catch (_: Exception) { 0 }
}

