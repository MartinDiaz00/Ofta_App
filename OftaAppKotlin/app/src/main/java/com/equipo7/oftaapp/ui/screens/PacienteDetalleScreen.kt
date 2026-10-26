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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeContenedor
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.viewmodel.PacienteDetalleUiState

@Composable
fun PacienteDetalleScreen(
    uiState: PacienteDetalleUiState,
    usuario: Usuario,
    onExamenClick: (String) -> Unit,
    onEditarPaciente: (Paciente) -> Unit,
    onFiltrarTipo: (String?) -> Unit,
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

    val paciente = uiState.paciente
    if (paciente == null) {
        EstadoVacio(
            titulo = "Paciente no disponible",
            detalle = uiState.error ?: "No se encontró la ficha solicitada.",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = paciente.nombreCompleto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RUT ${paciente.rut} · ${paciente.edad} años",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DivisorSuave)
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaDato("Nacimiento", Fechas.larga(paciente.fechaNacimiento))
                    FilaDato("Previsión", paciente.prevision)
                    FilaDato(
                        "Correo",
                        paciente.correo.ifBlank { "No registrado" }
                    )
                    FilaDato("Teléfono principal", paciente.telefonoPrincipal)
                    FilaDato(
                        "Teléfono secundario",
                        paciente.telefonoSecundario?.ifBlank { "No registrado" } ?: "No registrado"
                    )
                    FilaDato("Estado", if (paciente.activo) "Activo" else "Inactivo")

                    if (usuario.rol.puedeGestionarPacientes()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onEditarPaciente(paciente) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Editar datos del paciente",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exámenes del paciente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (uiState.tipoSeleccionado == null) {
                            uiState.totalExamenes.toString()
                        } else {
                            "${uiState.examenes.size}/${uiState.totalExamenes}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta
                    )
                }
            }
        }

        if (uiState.tiposDisponibles.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.tipoSeleccionado == null,
                            onClick = { onFiltrarTipo(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(uiState.tiposDisponibles, key = { it }) { tipo ->
                        FilterChip(
                            selected = uiState.tipoSeleccionado == tipo,
                            onClick = { onFiltrarTipo(tipo) },
                            label = { Text(tipo) }
                        )
                    }
                }
            }
        }

        if (uiState.examenes.isEmpty()) {
            item {
                EstadoVacio(
                    titulo = if (uiState.totalExamenes == 0) "Sin exámenes" else "Sin resultados",
                    detalle = if (uiState.totalExamenes == 0) {
                        "Este paciente todavía no tiene exámenes registrados."
                    } else {
                        "No hay exámenes de este tipo. Selecciona Todos para ver el historial completo."
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
                text = "La ficha y sus exámenes usan datos ficticios para la defensa académica.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f)
        )
    }
}
