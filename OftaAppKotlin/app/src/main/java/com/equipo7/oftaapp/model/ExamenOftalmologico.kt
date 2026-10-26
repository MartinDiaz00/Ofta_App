package com.equipo7.oftaapp.model

import com.equipo7.oftaapp.util.Fechas

enum class EstadoExamen(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    VALIDADO("Validado"),
    OBSERVADO("Observado"),
    ENTREGADO("Entregado");

    companion object {
        fun desde(valor: String?): EstadoExamen = entries.firstOrNull {
            it.name.equals(valor?.trim(), ignoreCase = true) ||
                it.etiqueta.equals(valor?.trim(), ignoreCase = true)
        } ?: PENDIENTE
    }
}

data class DocumentoAdjunto(
    val id: String,
    val examenId: String,
    val nombreOriginal: String,
    val rutaInterna: String,
    val mimeType: String,
    val tamanoBytes: Long,
    val createdAt: Long
) {
    val esPdf: Boolean
        get() = mimeType == "application/pdf" || nombreOriginal.endsWith(".pdf", ignoreCase = true)
}

data class ExamenOftalmologico(
    val id: String,
    val pacienteId: String,
    val pacienteNombre: String,
    val pacienteRut: String,
    val tipoExamen: String,
    val fecha: String,
    val hora: String,
    val sucursal: String,
    val profesional: String,
    val ojoEvaluado: String,
    val estado: EstadoExamen,
    val observaciones: String,
    val documento: DocumentoAdjunto? = null,
    val creadoPorId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val fechaVisible: String
        get() = Fechas.aVisible(fecha)

    val fechaHoraVisible: String
        get() = "$fechaVisible $hora"

    val tieneDocumento: Boolean
        get() = documento != null
}

data class FiltrosExamen(
    val texto: String = "",
    val estado: EstadoExamen? = null,
    val tipoExamen: String? = null,
    val sucursal: String? = null,
    val desde: String? = null,
    val hasta: String? = null
) {
    val activos: Int
        get() = listOf(
            texto.isNotBlank(),
            estado != null,
            tipoExamen != null,
            sucursal != null,
            desde != null,
            hasta != null
        ).count { it }

    val hayFiltros: Boolean
        get() = activos > 0
}

data class RespuestaExamen(
    val id: String? = null,
    val error: String? = null
)
