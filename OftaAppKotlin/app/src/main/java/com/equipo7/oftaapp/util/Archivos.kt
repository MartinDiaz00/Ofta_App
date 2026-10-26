package com.equipo7.oftaapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object Archivos {
    const val LIMITE_BYTES = 10L * 1024L * 1024L

    fun rutaAbsoluta(context: Context, rutaInterna: String): File? {
        if (rutaInterna.isBlank()) return null
        return runCatching {
            val raiz = context.filesDir.canonicalFile
            val archivo = File(context.filesDir, rutaInterna).canonicalFile
            if (!archivo.path.startsWith(raiz.path + File.separator)) return null
            archivo.takeIf { it.exists() }
        }.getOrNull()
    }

    fun abrir(context: Context, rutaInterna: String, mimeType: String): Result<Unit> = runCatching {
        val archivo = rutaAbsoluta(context, rutaInterna)
            ?: error("El archivo ya no está disponible.")
        val uri = uriCompartible(context, archivo)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType.ifBlank { "application/octet-stream" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Abrir documento").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun compartir(
        context: Context,
        rutaInterna: String,
        mimeType: String,
        nombreVisible: String
    ): Result<Unit> = runCatching {
        val archivo = rutaAbsoluta(context, rutaInterna)
            ?: error("El archivo ya no está disponible.")
        val uri = uriCompartible(context, archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType.ifBlank { "application/octet-stream" }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, nombreVisible)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartir documento").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun eliminar(context: Context, rutaInterna: String): Boolean {
        val archivo = rutaAbsoluta(context, rutaInterna) ?: return false
        return archivo.delete()
    }

    fun formatearTamano(bytes: Long): String = when {
        bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format("%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    fun extensionPara(nombre: String, mimeType: String): String {
        val desdeNombre = nombre.substringAfterLast('.', "").lowercase()
        if (desdeNombre in setOf("pdf", "jpg", "jpeg", "png", "webp")) return desdeNombre
        return when (mimeType) {
            "application/pdf" -> "pdf"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "bin"
        }
    }

    private fun uriCompartible(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
