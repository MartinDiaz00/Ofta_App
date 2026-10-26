package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.viewmodel.ExamenUiState

@Composable
fun ListaExamenes(
    state: ExamenUiState,
    onFiltroCambiado: (String) -> Unit,
    onFiltroEstadoCambiado: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onExamenClick: (ExamenOftalmologico) -> Unit = {},
    onReintentar: () -> Unit = {}
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.filtroBusqueda,
            onValueChange = onFiltroCambiado,
            label = { Text("Filtrar por examen o paciente") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TarjetaBlanca,
                unfocusedContainerColor = TarjetaBlanca,
                focusedBorderColor = VerdeOfta,
                unfocusedBorderColor = DivisorSuave,
                focusedTextColor = TextoPrincipal,
                unfocusedTextColor = TextoPrincipal,
                focusedLabelColor = VerdeOfta,
                unfocusedLabelColor = TextoSecundario,
                cursorColor = VerdeOfta
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Pendiente", "Completado").forEach { opcion ->
                FilterChip(
                    selected = state.filtroEstado.equals(opcion, ignoreCase = true),
                    onClick = { onFiltroEstadoCambiado(opcion) },
                    label = { Text(if (opcion == "Todos") "Todos" else if (opcion == "Pendiente") "Pendientes" else "Completados") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.cargando) {
            CircularProgressIndicator(color = VerdeOfta)
        } else if (state.error != null) {
            Text(state.error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            Button(onClick = onReintentar) { Text("Reintentar") }
        } else if (state.examenes.isEmpty()) {
            Text(
                text = "No hay exámenes que coincidan con la búsqueda.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(state.examenes) { examen ->
                    ExamenCard(
                        examen = examen,
                        modifier = Modifier.padding(bottom = 8.dp),
                        onClick = { onExamenClick(examen) }
                    )
                }
            }
        }
    }
}