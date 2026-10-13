package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExamenRepository {
    private val dbUrl =
        "jdbc:postgresql://ep-wild-rain-b6ycnirq-pooler.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require&maxResultBuffer=65536"
    private val dbUser = "neondb_owner"
    private val dbPassword = "npg_dQbnc3uqVGw6"

    private val dateParser = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private fun openConnection(): Connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)

    fun obtenerExamenes(): List<ExamenOftalmologico> = runBlocking(Dispatchers.IO) {
        val result = mutableListOf<ExamenOftalmologico>()
        openConnection().use { connection ->
            "SELECT e.id::text, p.nombre_completo, e.tipo_examen, e.fecha_examen, " +
                "COALESCE(u.nombre_completo, 'Sin asignar') AS medico, " +
                "e.ojo_evaluado, e.sucursal, e.estado, e.observaciones, e.documento_url " +
                "FROM examenes e " +
                "JOIN pacientes p ON p.id = e.paciente_id " +
                "LEFT JOIN usuarios u ON u.id = e.medico_id " +
                "ORDER BY e.created_at DESC".let { SQL ->
                    connection.prepareStatement(SQL).use { statement ->
                        statement.executeQuery().use { resultSet ->
                            while (resultSet.next()) {
                                result.add(resultSet.toExamen())
                            }
                        }
                    }
                }
        }
        result
    }

    fun obtenerPacientes(): List<Paciente> = runBlocking(Dispatchers.IO) {
        val result = mutableListOf<Paciente>()
        openConnection().use { connection ->
            "SELECT id::text, rut, nombre_completo, edad, prevision " +
                "FROM pacientes ORDER BY nombre_completo".let { SQL ->
                connection.prepareStatement(SQL).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            result.add(
                                Paciente(
                                    id = resultSet.getString("id"),
                                    rut = resultSet.getString("rut") ?: "",
                                    nombreCompleto = resultSet.getString("nombre_completo"),
                                    edad = resultSet.getObject("edad")?.toString()?.toIntOrNull() ?: 0,
                                    prevision = resultSet.getString("prevision") ?: ""
                                )
                            )
                        }
                    }
                }
            }
        }
        result
    }

    fun agregarExamen(examen: ExamenOftalmologico) = runBlocking(Dispatchers.IO) {
        openConnection().use { connection ->
            val pacienteId = pacienteIdOrCreate(connection, examen.pacienteNombre)
            val medicoId = medicoId(connection, examen.medicoSolicitante)
            val fecha = LocalDate.parse(examen.fecha, dateParser)

            "INSERT INTO examenes " +
                "(paciente_id, tipo_examen, fecha_examen, medico_id, ojo_evaluado, sucursal, estado, observaciones, documento_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)".let { sql ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setObject(1, java.util.UUID.fromString(pacienteId))
                    statement.setString(2, examen.tipoExamen)
                    statement.setDate(3, java.sql.Date.valueOf(fecha.toString()))
                    if (medicoId == null) statement.setNull(4, java.sql.Types.OTHER) else statement.setObject(4, java.util.UUID.fromString(medicoId))
                    statement.setString(5, examen.ojoEvaluado)
                    statement.setString(6, examen.sucursal)
                    statement.setString(7, examen.estado)
                    statement.setString(8, examen.observaciones)
                    statement.setString(9, examen.documento)
                    statement.execute()
                }
            }
        }
    }

    fun examenesDePaciente(nombrePaciente: String): List<ExamenOftalmologico> =
        obtenerExamenes().filter { it.pacienteNombre.equals(nombrePaciente, ignoreCase = true) }

    fun siguienteId(): String = "DB"

    fun buscarPorTipo(filtro: String): List<ExamenOftalmologico> {
        if (filtro.isBlank()) return obtenerExamenes()
        return obtenerExamenes().filter {
            it.tipoExamen.contains(filtro, ignoreCase = true) ||
                it.pacienteNombre.contains(filtro, ignoreCase = true)
        }
    }

    private fun pacienteIdOrCreate(connection: Connection, nombreCompleto: String): String {
        val select = "SELECT id::text FROM pacientes WHERE nombre_completo = ? LIMIT 1"
        connection.prepareStatement(select).use { statement ->
            statement.setString(1, nombreCompleto)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) return resultSet.getString("id")
            }
        }

        val insert = "INSERT INTO pacientes (nombre_completo) VALUES (?) RETURNING id::text"
        connection.prepareStatement(insert).use { statement ->
            statement.setString(1, nombreCompleto)
            statement.executeQuery().use { resultSet ->
                check(resultSet.next()) { "No se pudo crear el paciente" }
                return resultSet.getString("id")
            }
        }
    }

    private fun medicoId(connection: Connection, nombreCompleto: String): String? {
        val sql = "SELECT id::text FROM usuarios WHERE nombre_completo = ? OR username = ? LIMIT 1"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, nombreCompleto)
            statement.setString(2, nombreCompleto)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) return resultSet.getString("id")
            }
        }
        return null
    }

    private fun ResultSet.toExamen(): ExamenOftalmologico = ExamenOftalmologico(
        id = getString("id"),
        pacienteNombre = getString("nombre_completo"),
        tipoExamen = getString("tipo_examen"),
        fecha = parseDate(getString("fecha_examen")) ?: "",
        medicoSolicitante = getString("medico") ?: "",
        estado = getString("estado"),
        observaciones = getString("observaciones") ?: "",
        ojoEvaluado = getString("ojo_evaluado") ?: "",
        sucursal = getString("sucursal") ?: "",
        documento = getString("documento_url") ?: "resultado.pdf"
    )

    private fun parseDate(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val date = LocalDate.parse(value)
        return date.format(dateParser)
    }
}