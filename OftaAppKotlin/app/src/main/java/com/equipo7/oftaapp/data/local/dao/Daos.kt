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

@Dao
interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertar(paciente: PacienteEntity)
    @Update suspend fun actualizar(paciente: PacienteEntity)
    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1") suspend fun obtenerPorId(id: String): PacienteEntity?
    @Query("SELECT * FROM pacientes ORDER BY nombreCompleto") suspend fun listar(): List<PacienteEntity>
    @Query("SELECT * FROM pacientes WHERE nombreCompleto LIKE '%' || :texto || '%' OR rut LIKE '%' || :texto || '%' ORDER BY nombreCompleto") suspend fun buscar(texto: String): List<PacienteEntity>
    @Query("SELECT * FROM pacientes WHERE nombreCompleto = :nombre COLLATE NOCASE LIMIT 1") suspend fun obtenerPorNombre(nombre: String): PacienteEntity?
    @Query("SELECT COUNT(*) FROM pacientes") suspend fun contar(): Int
    @Query("SELECT EXISTS(SELECT 1 FROM pacientes WHERE rut = :rut)") suspend fun existePorRut(rut: String): Boolean
}

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertar(usuario: UsuarioEntity)
    @Query("SELECT * FROM usuarios WHERE username = :username COLLATE NOCASE AND activo = 1 LIMIT 1") suspend fun obtenerPorUsername(username: String): UsuarioEntity?
    @Query("SELECT * FROM usuarios ORDER BY nombreCompleto") suspend fun listar(): List<UsuarioEntity>
}

@Dao
interface ExamenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertar(examen: ExamenEntity)
    @Update suspend fun actualizar(examen: ExamenEntity)
    @Query("DELETE FROM examenes WHERE id = :id") suspend fun eliminar(id: String)
    @Query("SELECT * FROM examenes WHERE id = :id LIMIT 1") suspend fun obtenerPorId(id: String): ExamenEntity?
    @Query("SELECT COUNT(*) FROM examenes") suspend fun contar(): Int
    @Query("SELECT e.id, p.nombreCompleto AS pacienteNombre, e.tipoExamen, e.fecha, e.profesional AS medicoSolicitante, e.estado, e.observaciones, e.ojoEvaluado, e.sucursal, COALESCE((SELECT d.nombreOriginal FROM documentos_examen d WHERE d.examenId = e.id LIMIT 1), 'Sin documento adjunto') AS documento, COALESCE((SELECT d.rutaInterna FROM documentos_examen d WHERE d.examenId = e.id LIMIT 1), '') AS documentoUri FROM examenes e INNER JOIN pacientes p ON p.id = e.pacienteId ORDER BY e.createdAt DESC") suspend fun listarConPaciente(): List<ExamenListaRow>
    @Query("SELECT e.id, p.nombreCompleto AS pacienteNombre, e.tipoExamen, e.fecha, e.profesional AS medicoSolicitante, e.estado, e.observaciones, e.ojoEvaluado, e.sucursal, COALESCE((SELECT d.nombreOriginal FROM documentos_examen d WHERE d.examenId = e.id LIMIT 1), 'Sin documento adjunto') AS documento, COALESCE((SELECT d.rutaInterna FROM documentos_examen d WHERE d.examenId = e.id LIMIT 1), '') AS documentoUri FROM examenes e INNER JOIN pacientes p ON p.id = e.pacienteId WHERE e.pacienteId = :pacienteId ORDER BY e.createdAt DESC") suspend fun listarPorPaciente(pacienteId: String): List<ExamenListaRow>
    @Query("SELECT * FROM examenes WHERE estado = :estado ORDER BY createdAt DESC") suspend fun listarPorEstado(estado: String): List<ExamenEntity>
}

data class ExamenListaRow(
    val id: String,
    val pacienteNombre: String,
    val tipoExamen: String,
    val fecha: String,
    val medicoSolicitante: String,
    val estado: String,
    val observaciones: String,
    val ojoEvaluado: String,
    val sucursal: String,
    val documento: String,
    val documentoUri: String
)

@Dao
interface DocumentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertar(documento: DocumentoExamenEntity)
    @Query("SELECT * FROM documentos_examen WHERE examenId = :examenId ORDER BY createdAt DESC") suspend fun listarPorExamen(examenId: String): List<DocumentoExamenEntity>
    @Query("DELETE FROM documentos_examen WHERE id = :id") suspend fun eliminar(id: String)
}
