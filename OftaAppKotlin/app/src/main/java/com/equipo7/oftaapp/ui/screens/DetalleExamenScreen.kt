package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    }
}
