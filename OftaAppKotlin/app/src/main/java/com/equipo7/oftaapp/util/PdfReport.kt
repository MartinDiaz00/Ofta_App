package com.equipo7.oftaapp.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.equipo7.oftaapp.model.ExamenOftalmologico
import java.io.File
import java.io.FileOutputStream

object PdfReport {
    fun generarExamen(context: Context, examen: ExamenOftalmologico): File {
        val carpeta = File(context.filesDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "examen_${examen.id}.pdf")
        val paginaAncho = 595
        val paginaAlto = 842
        val documentoPdf = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(paginaAncho, paginaAlto, 1).create()
        val pagina = documentoPdf.startPage(info)
        val canvas = pagina.canvas
        var y = 100f
        val paint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.rgb(15, 122, 99)
        }
        canvas.drawText("Ofta App - Informe de Examen", 40f, y, paint)
        y += 36f

        paint.apply {
            textSize = 12f
            isFakeBoldText = false
            color = Color.BLACK
        }
        val lineas = listOf(
            "Paciente: ${examen.pacienteNombre}",
            "ID examen: ${examen.id}",
            "Tipo: ${examen.tipoExamen}",
            "Fecha: ${examen.fecha}",
            "Ojo evaluado: ${examen.ojoEvaluado}",
            "Estado: ${examen.estado}",
            "Sucursal: ${examen.sucursal}",
            "Medico solicitante: ${examen.medicoSolicitante}",
            "Observaciones: ${examen.observaciones.ifBlank { "Sin observaciones" }}",
            "Documento asociado: ${examen.documento}",
            "",
            "AVISO: Documento generado para un MVP académico con datos ficticios.",
            "No constituye diagnóstico ni reemplaza el criterio médico."
        )
        lineas.forEach {
            canvas.drawText(it, 40f, y, paint)
            y += 24f
        }
        documentoPdf.finishPage(pagina)
        FileOutputStream(archivo).use { salida ->
            documentoPdf.writeTo(salida)
        }
        documentoPdf.close()
        return archivo
    }
}