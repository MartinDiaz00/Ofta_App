package com.equipo7.oftaapp.data.local

import com.equipo7.oftaapp.data.local.dao.ExamenListaRow
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.data.local.entity.UsuarioEntity
import com.equipo7.oftaapp.model.DocumentoAdjunto
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario

fun PacienteEntity.aDominio(): Paciente = Paciente(
    id = id,
    rut = rut,
    nombreCompleto = nombreCompleto,
    fechaNacimiento = fechaNacimiento,
    correo = correo,
    telefonoPrincipal = telefonoPrincipal,
    telefonoSecundario = telefonoSecundario,
    prevision = prevision,
    activo = activo
)

fun UsuarioEntity.aDominio(): Usuario = Usuario(
    id = id,
    usuario = username,
    nombreCompleto = nombreCompleto,
    correo = correo,
    rol = Rol.desde(rol),
    pacienteId = pacienteId
)

fun DocumentoExamenEntity.aDominio(): DocumentoAdjunto = DocumentoAdjunto(
    id = id,
    examenId = examenId,
    nombreOriginal = nombreOriginal,
    rutaInterna = rutaInterna,
    mimeType = mimeType,
    tamanoBytes = tamanoBytes,
    createdAt = createdAt
)

fun ExamenListaRow.aDominio(): ExamenOftalmologico {
    val adjunto = if (documentoId != null && documentoRuta != null) {
        DocumentoAdjunto(
            id = documentoId,
            examenId = id,
            nombreOriginal = documentoNombre.orEmpty(),
            rutaInterna = documentoRuta,
            mimeType = documentoMime.orEmpty(),
            tamanoBytes = documentoTamano ?: 0L,
            createdAt = createdAt
        )
    } else {
        null
    }
    return ExamenOftalmologico(
        id = id,
        pacienteId = pacienteId,
        pacienteNombre = pacienteNombre,
        pacienteRut = pacienteRut,
        tipoExamen = tipoExamen,
        fecha = fecha,
        hora = hora,
        sucursal = sucursal,
        profesional = profesional,
        ojoEvaluado = ojoEvaluado,
        estado = EstadoExamen.desde(estado),
        observaciones = observaciones,
        documento = adjunto,
        creadoPorId = creadoPorId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
