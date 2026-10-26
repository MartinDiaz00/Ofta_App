package com.equipo7.oftaapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes", indices = [Index(value = ["rut"], unique = true)])
data class PacienteEntity(
    @PrimaryKey val id: String,
    val rut: String,
    val nombreCompleto: String,
    val fechaNacimiento: String,
    val correo: String,
    val telefonoPrincipal: String,
    val telefonoSecundario: String? = null,
    val prevision: String = "Sin especificar",
    val activo: Boolean = true
)

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = PacienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["username"], unique = true), Index("pacienteId")]
)
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val pacienteId: String? = null,
    val activo: Boolean = true
)

@Entity(
    tableName = "examenes",
    foreignKeys = [
        ForeignKey(
            entity = PacienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("pacienteId"),
        Index("estado"),
        Index("fecha"),
        Index("creadoPorId")
    ]
)
data class ExamenEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
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
    val updatedAt: Long
)

@Entity(
    tableName = "documentos_examen",
    foreignKeys = [
        ForeignKey(
            entity = ExamenEntity::class,
            parentColumns = ["id"],
            childColumns = ["examenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("examenId")]
)
data class DocumentoExamenEntity(
    @PrimaryKey val id: String,
    val examenId: String,
    val nombreOriginal: String,
    val rutaInterna: String,
    val mimeType: String,
    val tamanoBytes: Long,
    val createdAt: Long = System.currentTimeMillis()
)
