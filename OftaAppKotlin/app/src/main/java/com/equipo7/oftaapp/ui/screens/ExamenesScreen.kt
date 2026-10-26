package com.equipo7.oftaapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.Catalogos
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.components.SelectorCampo
import com.equipo7.oftaapp.ui.components.TextoError
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.viewmodel.ExamenesUiState
import java.time.LocalDate

@Composable
fun ExamenesScreen(
    uiState: ExamenesUiState,
    onTexto: (String) -> Unit,
    onEstado: (EstadoExamen?) -> Unit,
    onTipo: (String?) -> Unit,
    onSucursal: (String?) -> Unit,
    onDesde: (String?) -> Unit,
    onHasta: (String?) -> Unit,
    onLimpiar: () -> Unit,
    onExamenClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filtrosAvanzados by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.filtros.texto,
                onValueChange = onTexto,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por paciente, RUT, tipo o sucursal") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.filtros.texto.isNotBlank()) {
                        IconButton(onClick = { onTexto("") }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filtros.estado == null,
                    onClick = { onEstado(null) },
                    label = { Text("Todos") }
                )
                Catalogos.estados.forEach { estado ->
                    FilterChip(
                        selected = uiState.filtros.estado == estado,
                        onClick = { onEstado(estado) },
                        label = { Text(estado.etiqueta) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { filtrosAvanzados = !filtrosAvanzados }) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (filtrosAvanzados) "Ocultar filtros" else "Más filtros",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    if (uiState.filtros.activos > 0) {
                        Text(
                            text = " (${uiState.filtros.activos})",
                            color = VerdeOfta,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${uiState.examenes.size} de ${uiState.total}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
        }

        if (filtrosAvanzados) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SelectorCampo(
                            etiqueta = "Tipo de examen",
                            valor = uiState.filtros.tipoExamen ?: "Todos",
                            opciones = listOf("Todos") + Catalogos.tiposExamen,
                            etiquetaOpcion = { it },
                            onSeleccionar = { onTipo(if (it == "Todos") null else it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectorCampo(
                            etiqueta = "Sucursal",
                            valor = uiState.filtros.sucursal ?: "Todas",
                            opciones = listOf("Todas") + Catalogos.sucursales,
                            etiquetaOpcion = { it },
                            onSeleccionar = { onSucursal(if (it == "Todas") null else it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BotonFecha(
                                etiqueta = "Desde",
                                fechaIso = uiState.filtros.desde,
                                onFecha = onDesde,
                                modifier = Modifier.weight(1f)
                            )
                            BotonFecha(
                                etiqueta = "Hasta",
                                fechaIso = uiState.filtros.hasta,
                                onFecha = onHasta,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (uiState.filtros.hayFiltros) {
                            TextButton(onClick = onLimpiar) {
                                Text(text = "Limpiar todos los filtros", color = VerdeOfta)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.error != null) {
            item { TextoError(uiState.error) }
        }

        if (uiState.examenes.isEmpty()) {
            item {
                EstadoVacio(
                    titulo = "Sin resultados",
                    detalle = "Prueba con otro texto o ajusta los filtros seleccionados."
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
    }
}

@Composable
private fun BotonFecha(
    etiqueta: String,
    fechaIso: String?,
    onFecha: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val fecha = Fechas.parseIso(fechaIso)
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = TextoSecundario
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    val inicial = fecha ?: LocalDate.now()
                    DatePickerDialog(
                        contexto,
                        { _, anio, mes, dia ->
                            onFecha(LocalDate.of(anio, mes + 1, dia).toString())
                        },
                        inicial.year,
                        inicial.monthValue - 1,
                        inicial.dayOfMonth
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fecha?.format(Fechas.formatoVisible) ?: "Elegir",
                    maxLines = 1
                )
            }
            if (fechaIso != null) {
                IconButton(onClick = { onFecha(null) }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Quitar fecha",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
