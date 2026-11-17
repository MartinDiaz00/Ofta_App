package com.equipo7.oftaapp.repository

import android.content.Context
import androidx.room.withTransaction
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.aDominio
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.ExamenEntity
import com.equipo7.oftaapp.model.DocumentoAdjunto
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.ReglasExamen
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.network.FhirRemoteService
import com.equipo7.oftaapp.network.SincronizacionMonitor
import com.equipo7.oftaapp.notifications.NotificationHelper
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

data class EntradaExamen(
    val id: String = "",
    val pacienteId: String,
    val tipoExamen: String,
    val fecha: String,
    val hora: String,
    val sucursal: String,
    val profesional: String,
    val ojoEvaluado: String,
    val estado: EstadoExamen,
    val observaciones: String
)

class ExamenRepository(
    private val database: AppDatabase,
    private val documentoRepository: DocumentoRepository,
    private val context: Context,
    private val fhirRemoteService: FhirRemoteService? = null
) {
    fun observarTodos(usuario: Usuario): Flow<List<ExamenOftalmologico>> {
        val flujo = if (usuario.rol == com.equipo7.oftaapp.model.Rol.PACIENTE) {
            val pacienteId = usuario.pacienteId
            if (pacienteId.isNullOrBlank()) flowOf(emptyList()) else {
                database.examenDao().observarPorPaciente(pacienteId)
            }
        } else {
            database.examenDao().observarTodos()
        }
        return flujo.map { filas -> filas.map { it.aDominio() } }
    }

    fun observarPorId(id: String, usuario: Usuario): Flow<ExamenOftalmologico?> =
        database.examenDao().observarPorId(id).map { fila ->
            val visible = if (usuario.rol == com.equipo7.oftaapp.model.Rol.PACIENTE) {
                fila?.takeIf { it.pacienteId == usuario.pacienteId }
            } else {
                fila
            }
            visible?.aDominio()
        }

    fun observarDocumentos(examenId: String): Flow<List<DocumentoAdjunto>> =
        database.documentoDao().observarPorExamen(examenId).map { documentos ->
            documentos.map { it.aDominio() }
        }

    suspend fun obtenerPorId(id: String): ExamenOftalmologico? =
        database.examenDao().obtenerDetallePorId(id)?.aDominio()

    suspend fun obtenerDocumentos(examenId: String): List<DocumentoAdjunto> =
        database.documentoDao().listarPorExamen(examenId).map { it.aDominio() }

    suspend fun contarDocumentos(examenId: String): Int =
        database.documentoDao().contarPorExamen(examenId)

    suspend fun guardar(
        datos: EntradaExamen,
        documento: DocumentoSeleccionado?,
        usuario: Usuario
    ): Result<String> = runCatching {
        val paciente = database.pacienteDao().obtenerPorId(datos.pacienteId)
            ?: error("Selecciona un paciente válido.")
        val fechaIso = Fechas.aIso(datos.fecha) ?: error("La fecha del examen no es válida.")
        require(Fechas.parseIso(fechaIso) != null) { "La fecha del examen no es válida." }
        require(Validadores.horaValida(datos.hora)) { "La hora debe tener el formato HH:mm." }
        require(Validadores.textoValido(datos.tipoExamen)) { "Selecciona el tipo de examen." }
        require(Validadores.textoValido(datos.sucursal)) { "Selecciona la sucursal." }
        require(Validadores.textoValido(datos.profesional)) { "Selecciona el profesional." }
        require(Validadores.textoValido(datos.ojoEvaluado)) { "Selecciona el ojo evaluado." }
        require(Validadores.observacionValida(datos.observaciones)) {
            "Las observaciones no pueden superar 500 caracteres."
        }

        val id = datos.id.ifBlank { "exa-${UUID.randomUUID()}" }
        val anterior = if (datos.id.isBlank()) null else {
            database.examenDao().obtenerPorId(datos.id)
                ?: error("El examen que intentas editar ya no existe.")
        }
        val estadoAnterior = anterior?.let { EstadoExamen.desde(it.estado) }
        if (anterior == null) {
            require(usuario.rol.puedeRegistrarExamen()) {
                "Tu rol no puede registrar exámenes."
            }
        } else {
            val estadoActual = estadoAnterior ?: EstadoExamen.PENDIENTE
            require(usuario.rol.puedeEditarExamen(estadoActual)) {
                "Tu rol no puede editar un examen en estado ${estadoActual.etiqueta}."
            }
            val errorTransicion = ReglasExamen.validarTransicion(
                rol = usuario.rol,
                estadoActual = estadoActual,
                estadoNuevo = datos.estado
            )
            require(errorTransicion == null) { errorTransicion.orEmpty() }
        }

        val documentosExistentes = database.documentoDao().contarPorExamen(id)
        val tieneDocumento = documento != null || documentosExistentes > 0
        val errorEstado = ReglasExamen.validarEstado(
            estado = datos.estado,
            rol = usuario.rol,
            observaciones = datos.observaciones,
            tieneDocumento = tieneDocumento
        )
        require(errorEstado == null) { errorEstado.orEmpty() }

        val copia = documento?.let { seleccionado ->
            documentoRepository.copiarDocumento(seleccionado).getOrElse { fallo ->
                throw IllegalArgumentException(fallo.message ?: "No se pudo guardar el documento.")
            }
        }
        val ahora = System.currentTimeMillis()
        val examenEntity = ExamenEntity(
            id = id,
            pacienteId = paciente.id,
            tipoExamen = datos.tipoExamen.trim(),
            fecha = fechaIso,
            hora = datos.hora.trim(),
            sucursal = datos.sucursal.trim(),
            profesional = datos.profesional.trim(),
            ojoEvaluado = datos.ojoEvaluado.trim(),
            estado = datos.estado.name,
            observaciones = datos.observaciones.trim(),
            creadoPorId = anterior?.creadoPorId ?: usuario.id,
            createdAt = anterior?.createdAt ?: ahora,
            updatedAt = ahora
        )

        try {
            database.withTransaction {
                if (anterior == null) {
                    database.examenDao().insertar(examenEntity)
                } else {
                    database.examenDao().actualizar(examenEntity)
                }
                if (copia != null) {
                    database.documentoDao().insertar(
                        DocumentoExamenEntity(
                            id = "doc-${UUID.randomUUID()}",
                            examenId = id,
                            nombreOriginal = copia.nombreOriginal,
                            rutaInterna = copia.rutaInterna,
                            mimeType = copia.mimeType,
                            tamanoBytes = copia.tamanoBytes,
                            createdAt = ahora
                        )
                    )
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            copia?.let { documentoRepository.eliminarArchivo(it.rutaInterna) }
            throw error
        }
        // Integración API/FHIR (best-effort): envía DiagnosticReport al microservicio HAPI FHIR vía Retrofit.
        fhirRemoteService?.sincronizarExamen(
            ExamenOftalmologico(
                id = id,
                pacienteId = paciente.id,
                pacienteNombre = paciente.nombreCompleto,
                pacienteRut = paciente.rut,
                tipoExamen = datos.tipoExamen.trim(),
                fecha = fechaIso,
                hora = datos.hora.trim(),
                sucursal = datos.sucursal.trim(),
                profesional = datos.profesional.trim(),
                ojoEvaluado = datos.ojoEvaluado.trim(),
                estado = datos.estado,
                observaciones = datos.observaciones.trim(),
                creadoPorId = anterior?.creadoPorId ?: usuario.id,
                createdAt = anterior?.createdAt ?: ahora,
                updatedAt = ahora
            )
        )?.let { resultado ->
            resultado.onSuccess { SincronizacionMonitor.exito() }
                .onFailure { fallo ->
                    SincronizacionMonitor.pendiente(
                        "Examen guardado en el dispositivo. Sincronización FHIR pendiente: " +
                            (fallo.message ?: "sin conexión").take(110)
                    )
                }
        }
        if (anterior == null) {
            notificarExamenAgendado(
                examenId = id,
                paciente = paciente.nombreCompleto,
                tipoExamen = datos.tipoExamen,
                fechaHora = "${Fechas.aVisible(fechaIso)} ${datos.hora}",
                sucursal = datos.sucursal,
                profesional = datos.profesional
            )
        }
        if (estadoAnterior != datos.estado) {
            notificarResultadoDisponible(
                paciente = paciente.nombreCompleto,
                tipoExamen = datos.tipoExamen,
                estado = datos.estado
            )
        }
        id
    }

    suspend fun cambiarEstado(
        examenId: String,
        nuevoEstado: EstadoExamen,
        observaciones: String,
        usuario: Usuario
    ): Result<Unit> = runCatching {
        val examen = database.examenDao().obtenerPorId(examenId)
            ?: error("El examen ya no existe.")
        val actual = EstadoExamen.desde(examen.estado)
        val transicion = ReglasExamen.validarTransicion(
            rol = usuario.rol,
            estadoActual = actual,
            estadoNuevo = nuevoEstado
        )
        require(transicion == null) { transicion.orEmpty() }
        val tieneDocumento = database.documentoDao().contarPorExamen(examenId) > 0
        val errorEstado = ReglasExamen.validarEstado(
            estado = nuevoEstado,
            rol = usuario.rol,
            observaciones = observaciones,
            tieneDocumento = tieneDocumento
        )
        require(errorEstado == null) { errorEstado.orEmpty() }
        val observacionFinal = observaciones.trim().ifBlank { examen.observaciones }
        val ahoraEstado = System.currentTimeMillis()
        database.examenDao().actualizarEstado(
            id = examenId,
            estado = nuevoEstado.name,
            observaciones = observacionFinal,
            updatedAt = ahoraEstado
        )
        val paciente = database.pacienteDao().obtenerPorId(examen.pacienteId)
        fhirRemoteService?.sincronizarExamen(
            ExamenOftalmologico(
                id = examen.id,
                pacienteId = examen.pacienteId,
                pacienteNombre = paciente?.nombreCompleto ?: "",
                pacienteRut = paciente?.rut ?: "",
                tipoExamen = examen.tipoExamen,
                fecha = examen.fecha,
                hora = examen.hora,
                sucursal = examen.sucursal,
                profesional = examen.profesional,
                ojoEvaluado = examen.ojoEvaluado,
                estado = nuevoEstado,
                observaciones = observacionFinal,
                creadoPorId = examen.creadoPorId,
                createdAt = examen.createdAt,
                updatedAt = ahoraEstado
            )
        )?.let { resultado ->
            resultado.onSuccess { SincronizacionMonitor.exito() }
                .onFailure { fallo ->
                    SincronizacionMonitor.pendiente(
                        "Estado actualizado localmente. Sincronización FHIR pendiente: " +
                            (fallo.message ?: "sin conexión").take(110)
                    )
                }
        }
        if (actual != nuevoEstado) {
            notificarResultadoDisponible(
                paciente = paciente?.nombreCompleto ?: "Paciente",
                tipoExamen = examen.tipoExamen,
                estado = nuevoEstado
            )
        }
    }

    /** Reenvía todos los exámenes locales al servidor FHIR (idempotente: reutiliza por identificador). */
    suspend fun sincronizarPendentes(): Result<Int> = runCatching {
        val servicio = fhirRemoteService ?: error("Sincronización FHIR no configurada.")
        var total = 0
        database.examenDao().observarTodos().first().forEach { fila ->
            servicio.sincronizarExamen(fila.aDominio()).getOrThrow()
            total++
        }
        total
    }

    suspend fun eliminarDocumento(
        documentoId: String,
        usuario: Usuario
    ): Result<Unit> = runCatching {
        require(usuario.rol.puedeEliminarDocumentos()) {
            "Solo TENS o administrador pueden eliminar documentos."
        }
        val documento = database.documentoDao().obtenerPorId(documentoId)
            ?: error("El documento ya no existe.")
        val examen = database.examenDao().obtenerPorId(documento.examenId)
            ?: error("El examen asociado ya no existe.")
        require(EstadoExamen.desde(examen.estado) != EstadoExamen.ENTREGADO) {
            "No se pueden eliminar documentos de un examen entregado."
        }
        database.documentoDao().eliminar(documentoId)
        documentoRepository.eliminarArchivo(documento.rutaInterna)
    }

    private fun notificarExamenAgendado(
        examenId: String,
        paciente: String,
        tipoExamen: String,
        fechaHora: String,
        sucursal: String,
        profesional: String
    ) {
        NotificationHelper.notificarExamenAgendado(
            contexto = context,
            examenId = examenId,
            paciente = paciente,
            tipoExamen = tipoExamen,
            fechaHora = fechaHora,
            sucursal = sucursal,
            profesional = profesional
        )
    }

    private fun notificarResultadoDisponible(
        paciente: String,
        tipoExamen: String,
        estado: EstadoExamen
    ) {
        if (estado != EstadoExamen.VALIDADO && estado != EstadoExamen.ENTREGADO) return
        NotificationHelper.mostrar(
            contexto = context,
            titulo = "Resultado disponible",
            mensaje = "$paciente · $tipoExamen está ${estado.etiqueta.lowercase()}."
        )
    }
}
