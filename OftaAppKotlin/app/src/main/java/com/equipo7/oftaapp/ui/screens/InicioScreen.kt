package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.components.ResumenCard
import com.equipo7.oftaapp.ui.components.TextoError
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.viewmodel.InicioUiState

@Composable
fun InicioScreen(
    uiState: InicioUiState,
    usuario: Usuario,
    onExamenClick: (String) -> Unit,
    onVerTodos: () -> Unit,
    onFiltrarEstado: (EstadoExamen?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.cargando) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.error != null) {
            item { TextoError(uiState.error) }
        }

        item {
            Text(
                text = if (usuario.rol == Rol.PACIENTE) "Mis exámenes" else "Resumen de actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenCard(
                    titulo = "Exámenes",
                    valor = uiState.totalExamenes.toString(),
                    onClick = { onFiltrarEstado(null) },
                    modifier = Modifier.weight(1f)
                )
                ResumenCard(
                    titulo = "Pendientes",
                    valor = uiState.pendientes.toString(),
                    onClick = { onFiltrarEstado(EstadoExamen.PENDIENTE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenCard(
                    titulo = "Validados",
                    valor = uiState.validados.toString(),
                    onClick = { onFiltrarEstado(EstadoExamen.VALIDADO) },
                    modifier = Modifier.weight(1f)
                )
                ResumenCard(
                    titulo = "Observados",
                    valor = uiState.observados.toString(),
                    onClick = { onFiltrarEstado(EstadoExamen.OBSERVADO) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenCard(
                    titulo = "Entregados",
                    valor = uiState.entregados.toString(),
                    onClick = { onFiltrarEstado(EstadoExamen.ENTREGADO) },
                    modifier = Modifier.weight(1f)
                )
                if (usuario.rol != Rol.PACIENTE) {
                    ResumenCard(
                        titulo = "Pacientes",
                        valor = uiState.pacientes.toString(),
                        onClick = onVerTodos,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos exámenes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onVerTodos) {
                    Text(text = "Ver todos")
                }
            }
        }

        if (uiState.examenes.isEmpty()) {
            item {
                EstadoVacio(
                    titulo = "Sin exámenes registrados",
                    detalle = if (usuario.rol.puedeRegistrarExamen()) {
                        "Usa el botón + para registrar el primer examen."
                    } else {
                        "Aún no hay exámenes disponibles para tu cuenta."
                    }
                )
            }
        } else {
            items(uiState.examenes, key = ExamenOftalmologico::id) { examen ->
                ExamenCard(
                    examen = examen,
                    onClick = { onExamenClick(examen.id) }
                )
            }
        }

        item {
            Text(
                text = "Los datos de esta aplicación son ficticios y se almacenan localmente para una entrega académica.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
