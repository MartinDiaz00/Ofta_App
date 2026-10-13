package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.equipo7.oftaapp.notifications.NotificationHelper
import com.equipo7.oftaapp.util.PdfReport
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.ui.components.OpcionCard
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta

@Composable
fun ContenidoDetalle(
    examen: ExamenOftalmologico,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OpcionCard(
            titulo = examen.pacienteNombre,
            subtitulo = "${examen.tipoExamen} · Ojo ${examen.ojoEvaluado.lowercase()}",
            colorChevron = VerdeOfta
        )

        Spacer(modifier = Modifier.height(16.dp))

        OpcionCard(
            titulo = "Estado",
            subtitulo = examen.estado,
            colorChevron = VerdeOfta
        )

        Spacer(modifier = Modifier.height(16.dp))

        OpcionCard(
            titulo = "Observaciones",
            subtitulo = examen.observaciones.ifBlank { "Sin observaciones" },
            colorChevron = VerdeOfta
        )

        Spacer(modifier = Modifier.height(16.dp))

        OpcionCard(
            titulo = "Documento",
            subtitulo = examen.documento,
            colorChevron = VerdeOfta
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Examen ${examen.id} · ${examen.fecha} · ${examen.medicoSolicitante}\n" +
                    "Sucursal ${examen.sucursal}",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        val contexto = LocalContext.current

        Button(
            onClick = {
                val archivo = PdfReport.generarExamen(contexto, examen)
                val uri = FileProvider.getUriForFile(
                    contexto,
                    "${contexto.packageName}.fileprovider",
                    archivo
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                contexto.startActivity(Intent.createChooser(intent, "Abrir PDF"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Generar y abrir PDF")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                NotificationHelper.mostrar(
                    contexto,
                    "Prueba Ofta App",
                    "Notificacion generada correctamente."
                )
                NotificationHelper.enviarCorreoDePrueba(contexto)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Probar notificacion y enviar correo")
        }
    }
}
