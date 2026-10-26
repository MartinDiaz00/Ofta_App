package com.equipo7.oftaapp.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.ui.components.EtiquetaEstado
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario

@Composable
fun ContenidoHistorial(
    pacientes: List<Paciente>,
    examenes: List<ExamenOftalmologico>,
    modifier: Modifier = Modifier
) {
    if (pacientes.isEmpty()) {
        Text(
            text = "No hay pacientes con antecedentes registrados.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(pacientes) { paciente ->
            val previos = examenes.filter { it.pacienteNombre == paciente.nombreCompleto }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = paciente.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ficha ${paciente.id} · ${previos.size} exámenes previos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )

                    if (previos.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin exámenes registrados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    } else {
                        previos.forEach { examen ->
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = com.equipo7.oftaapp.ui.theme.DivisorSuave)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = examen.tipoExamen,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextoPrincipal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${examen.fecha} · Ojo ${examen.ojoEvaluado.lowercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSecundario
                                    )
                                }

                                EtiquetaEstado(estado = examen.estado)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}
