package com.equipo7.oftaapp.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.util.Archivos
import com.equipo7.oftaapp.util.PdfReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class DocumentoSeleccionado(
    val uri: Uri,
    val nombre: String,
    val mimeType: String,
    val tamanoBytes: Long
) {
    val esPdf: Boolean
        get() = mimeType == "application/pdf" || nombre.endsWith(".pdf", ignoreCase = true)
}

data class DocumentoCopiado(
    val nombreOriginal: String,
    val rutaInterna: String,
    val mimeType: String,
    val tamanoBytes: Long
)

class DocumentoRepository(private val context: Context) {
    suspend fun prepararDocumento(uri: Uri): Result<DocumentoSeleccionado> = withContext(Dispatchers.IO) {
        runCatching {
            var nombre = "documento"
            var tamano = -1L
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val indiceNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val indiceTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (indiceNombre >= 0) nombre = cursor.getString(indiceNombre) ?: nombre
                    if (indiceTamano >= 0 && !cursor.isNull(indiceTamano)) {
                        tamano = cursor.getLong(indiceTamano)
                    }
                }
            }
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            require(tamano <= Archivos.LIMITE_BYTES) {
                "El documento supera el límite de 10 MB."
            }
            DocumentoSeleccionado(
                uri = uri,
                nombre = File(nombre).name.take(120).ifBlank { "documento" },
                mimeType = mime,
                tamanoBytes = tamano
            )
        }
    }

    suspend fun copiarDocumento(seleccionado: DocumentoSeleccionado): Result<DocumentoCopiado> =
        withContext(Dispatchers.IO) {
            runCatching {
                val carpeta = File(context.filesDir, "documentos").apply { mkdirs() }
                val extension = Archivos.extensionPara(seleccionado.nombre, seleccionado.mimeType)
                val archivo = File(carpeta, "doc_${UUID.randomUUID()}.$extension")
                val entrada = context.contentResolver.openInputStream(seleccionado.uri)
                    ?: error("No se pudo leer el documento seleccionado.")
                var total = 0L
                try {
                    entrada.use { input ->
                        FileOutputStream(archivo).use { salida ->
                            val buffer = ByteArray(8 * 1024)
                            while (true) {
                                val leidos = input.read(buffer)
                                if (leidos <= 0) break
                                total += leidos
                                require(total <= Archivos.LIMITE_BYTES) {
                                    "El documento supera el límite de 10 MB."
                                }
                                salida.write(buffer, 0, leidos)
                            }
                        }
                    }
                } catch (error: Exception) {
                    archivo.delete()
                    throw error
                }
                if (total <= 0L) {
                    archivo.delete()
                    error("El documento seleccionado está vacío.")
                }
                DocumentoCopiado(
                    nombreOriginal = seleccionado.nombre,
                    rutaInterna = "documentos/${archivo.name}",
                    mimeType = seleccionado.mimeType,
                    tamanoBytes = archivo.length()
                )
            }
        }

    suspend fun generarInforme(examen: ExamenOftalmologico): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val archivo = PdfReport.generarExamen(context, examen)
                "reportes/${archivo.name}"
            }
        }

    fun abrir(rutaInterna: String, mimeType: String): Result<Unit> =
        Archivos.abrir(context, rutaInterna, mimeType)

    fun compartir(rutaInterna: String, mimeType: String, nombre: String): Result<Unit> =
        Archivos.compartir(context, rutaInterna, mimeType, nombre)

    fun eliminarArchivo(rutaInterna: String): Boolean =
        Archivos.eliminar(context, rutaInterna)
}
