package com.equipo7.oftaapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.ExamenEntity
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(paciente: PacienteEntity)

    @Update
    suspend fun actualizar(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): PacienteEntity?

    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<PacienteEntity?>

    @Query("SELECT * FROM pacientes WHERE activo = 1 ORDER BY nombreCompleto COLLATE NOCASE")
    suspend fun listar(): List<PacienteEntity>

    @Query("SELECT * FROM pacientes WHERE activo = 1 ORDER BY nombreCompleto COLLATE NOCASE")
    fun observarTodos(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE activo = 1 AND (nombreCompleto LIKE '%' || :texto || '%' COLLATE NOCASE OR rut LIKE '%' || :texto || '%') ORDER BY nombreCompleto COLLATE NOCASE")
    suspend fun buscar(texto: String): List<PacienteEntity>

    @Query("SELECT COUNT(*) FROM pacientes")
    suspend fun contar(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE rut = :rut AND id != :exceptoId)")
    suspend fun existeRut(rut: String, exceptoId: String = ""): Boolean
}

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE username = :username COLLATE NOCASE AND activo = 1 LIMIT 1")
    suspend fun obtenerPorUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY nombreCompleto COLLATE NOCASE")
    suspend fun listar(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombreCompleto COLLATE NOCASE")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface ExamenDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(examen: ExamenEntity)

    @Update
    suspend fun actualizar(examen: ExamenEntity)

    @Query("UPDATE examenes SET estado = :estado, observaciones = :observaciones, updatedAt = :updatedAt WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String, observaciones: String, updatedAt: Long)

    @Query("DELETE FROM examenes WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("SELECT * FROM examenes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): ExamenEntity?

    @Query("SELECT COUNT(*) FROM examenes")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM examenes WHERE pacienteId = :pacienteId")
    suspend fun contarPorPaciente(pacienteId: String): Int

    @Query(CONSULTA_LISTA + " ORDER BY e.fecha DESC, e.hora DESC, e.createdAt DESC")
    fun observarTodos(): Flow<List<ExamenListaRow>>

    @Query(CONSULTA_LISTA + " WHERE e.pacienteId = :pacienteId ORDER BY e.fecha DESC, e.hora DESC, e.createdAt DESC")
    fun observarPorPaciente(pacienteId: String): Flow<List<ExamenListaRow>>

    @Query(CONSULTA_LISTA + " WHERE e.id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<ExamenListaRow?>

    @Query(CONSULTA_LISTA + " WHERE e.id = :id LIMIT 1")
    suspend fun obtenerDetallePorId(id: String): ExamenListaRow?

    @Query("SELECT * FROM examenes WHERE estado = :estado ORDER BY fecha DESC, hora DESC")
    suspend fun listarPorEstado(estado: String): List<ExamenEntity>
}

data class ExamenListaRow(
    val id: String,
    val pacienteId: String,
    val pacienteRut: String,
    val pacienteNombre: String,
    val tipoExamen: String,
    val fecha: String,
    val hora: String,
    val sucursal: String,
    val profesional: String,
    val ojoEvaluado: String,
    val estado: String,
    val observaciones: String,
    val creadoPorId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val documentoId: String?,
    val documentoNombre: String?,
    val documentoRuta: String?,
    val documentoMime: String?,
    val documentoTamano: Long?
)

private const val CONSULTA_LISTA = """
    SELECT
        e.id AS id,
        e.pacienteId AS pacienteId,
        p.rut AS pacienteRut,
        p.nombreCompleto AS pacienteNombre,
        e.tipoExamen AS tipoExamen,
        e.fecha AS fecha,
        e.hora AS hora,
        e.sucursal AS sucursal,
        e.profesional AS profesional,
        e.ojoEvaluado AS ojoEvaluado,
        e.estado AS estado,
        e.observaciones AS observaciones,
        e.creadoPorId AS creadoPorId,
        e.createdAt AS createdAt,
        e.updatedAt AS updatedAt,
        d.id AS documentoId,
        d.nombreOriginal AS documentoNombre,
        d.rutaInterna AS documentoRuta,
        d.mimeType AS documentoMime,
        d.tamanoBytes AS documentoTamano
    FROM examenes e
    INNER JOIN pacientes p ON p.id = e.pacienteId
    LEFT JOIN documentos_examen d ON d.id = (
        SELECT d2.id FROM documentos_examen d2
        WHERE d2.examenId = e.id
        ORDER BY d2.createdAt DESC
        LIMIT 1
    )
"""

@Dao
interface DocumentoDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(documento: DocumentoExamenEntity)

    @Query("SELECT * FROM documentos_examen WHERE examenId = :examenId ORDER BY createdAt DESC")
    suspend fun listarPorExamen(examenId: String): List<DocumentoExamenEntity>

    @Query("SELECT * FROM documentos_examen WHERE examenId = :examenId ORDER BY createdAt DESC")
    fun observarPorExamen(examenId: String): Flow<List<DocumentoExamenEntity>>

    @Query("SELECT * FROM documentos_examen WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): DocumentoExamenEntity?

    @Query("SELECT COUNT(*) FROM documentos_examen WHERE examenId = :examenId")
    suspend fun contarPorExamen(examenId: String): Int

    @Query("DELETE FROM documentos_examen WHERE id = :id")
    suspend fun eliminar(id: String)
}
