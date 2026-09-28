package com.equipo7.oftaapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.ExamenOftalmologico

@Composable
fun ExamenCard(
    examen: ExamenOftalmologico,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = examen.tipoExamen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = examen.estado,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (examen.estado == "Completado") Color(0xFF0B7A63) else Color(0xFFD32F2F)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paciente: ${examen.pacienteNombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Médico: ${examen.medicoSolicitante}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Fecha: ${examen.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (examen.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Obs: ${examen.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}