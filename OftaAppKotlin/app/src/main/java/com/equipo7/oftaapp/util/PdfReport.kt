package com.equipo7.oftaapp.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import com.equipo7.oftaapp.model.ExamenOftalmologico
import java.io.File
import java.io.FileOutputStream

object PdfReport {
    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 42f

    fun generarExamen(context: Context, examen: ExamenOftalmologico): File {
        val carpeta = File(context.filesDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "informe_${examen.id}.pdf")
        val lineas = listOf(
            "Paciente: ${examen.pacienteNombre}",
            "RUT: ${examen.pacienteRut}",
            "ID examen: ${examen.id}",
            "Tipo de examen: ${examen.tipoExamen}",
            "Fecha: ${examen.fechaVisible}",
            "Hora: ${examen.hora}",
            "Sucursal: ${examen.sucursal}",
            "Profesional: ${examen.profesional}",
            "Ojo evaluado: ${examen.ojoEvaluado}",
            "Estado: ${examen.estado.etiqueta}",
            "",
            "Observaciones:",
            examen.observaciones.ifBlank { "Sin observaciones registradas." },
            "",
            "Documento asociado: ${examen.documento?.nombreOriginal ?: "Sin documento adjunto"}"
        )
        escribirPdf(
            archivo = archivo,
            titulo = "Ofta App - Informe de Examen",
            subtitulo = "Informe clínico de demostración",
            lineas = lineas
        )
        return archivo
    }

    fun generarDemo(
        context: Context,
        nombreArchivo: String,
        titulo: String,
        lineas: List<String>
    ): File {
        val carpeta = File(context.filesDir, "documentos").apply { mkdirs() }
        val archivo = File(carpeta, nombreArchivo)
        escribirPdf(
            archivo = archivo,
            titulo = titulo,
            subtitulo = "Documento de prueba generado por Ofta App",
            lineas = lineas
        )
        return archivo
    }

    private fun escribirPdf(
        archivo: File,
        titulo: String,
        subtitulo: String,
        lineas: List<String>
    ) {
        val documento = PdfDocument()
        val pagina = documento.startPage(
            PdfDocument.PageInfo.Builder(ANCHO, ALTO, 1).create()
        )
        val canvas = pagina.canvas
        val paint = Paint().apply {
            color = Color.rgb(15, 122, 99)
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(titulo, MARGEN, 64f, paint)
        paint.color = Color.rgb(90, 100, 96)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(subtitulo, MARGEN, 84f, paint)
        canvas.drawLine(MARGEN, 96f, ANCHO - MARGEN, 96f, paint)

        var y = 120f
        val anchoTexto = (ANCHO - MARGEN * 2).toInt()
        lineas.forEach { linea ->
            val texto = linea.take(1200)
            if (y > ALTO - 120f) return@forEach
            y = dibujarTexto(canvas, texto, y, anchoTexto) + 8f
        }

        val pie = "AVISO: Documento generado para un MVP académico con datos ficticios. " +
            "No constituye diagnóstico ni reemplaza el criterio médico."
        val piePaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 9f
        }
        val pieLayout = StaticLayout.Builder
            .obtain(pie, 0, pie.length, TextPaint(piePaint), anchoTexto)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(MARGEN, ALTO - 78f)
        pieLayout.draw(canvas)
        canvas.restore()

        documento.finishPage(pagina)
        FileOutputStream(archivo).use { salida -> documento.writeTo(salida) }
        documento.close()
    }

    private fun dibujarTexto(
        canvas: android.graphics.Canvas,
        texto: String,
        y: Float,
        ancho: Int
    ): Float {
        val paint = TextPaint().apply {
            color = Color.BLACK
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val layout = StaticLayout.Builder
            .obtain(texto, 0, texto.length, paint, ancho)
            .setIncludePad(false)
            .setLineSpacing(2f, 1f)
            .build()
        canvas.save()
        canvas.translate(MARGEN, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }
}
