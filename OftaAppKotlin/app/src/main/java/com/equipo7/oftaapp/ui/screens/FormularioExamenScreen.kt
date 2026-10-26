package com.equipo7.oftaapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.Catalogos
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.CampoTexto
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.components.IconoConTexto
import com.equipo7.oftaapp.ui.components.SelectorCampo
import com.equipo7.oftaapp.ui.components.TextoError
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.ui.theme.VerdeContenedor
import com.equipo7.oftaapp.util.Archivos
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.viewmodel.FormularioExamenUiState
import java.time.LocalTime

@Composable
fun FormularioExamenScreen(
    uiState: FormularioExamenUiState,
    usuario: Usuario,
    onPaciente: (String) -> Unit,
    onFecha: (String) -> Unit,
    onHora: (String) -> Unit,
    onSucursal: (String) -> Unit,
    onProfesional: (String) -> Unit,
    onTipoExamen: (String) -> Unit,
    onOjoEvaluado: (String) -> Unit,
    onEstado: (EstadoExamen) -> Unit,
    onObservaciones: (String) -> Unit,
    onDocumentoSeleccionado: (Uri) -> Unit,
    onQuitarDocumento: () -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    onGuardado: (String) -> Unit,
    onIrPacientes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val selectorDocumento = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onDocumentoSeleccionado(uri)
    }

    LaunchedEffect(uiState.guardadoId) {
        uiState.guardadoId?.let(onGuardado)
    }

    if (uiState.accesoDenegado) {
        EstadoVacio(
            titulo = "Acceso restringido",
            detalle = uiState.errorGeneral ?: "No tienes permisos para realizar esta acción.",
            modifier = modifier.fillMaxSize()
        )
        return
    }

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

    val pacienteSeleccionado = uiState.pacientes.firstOrNull { it.id == uiState.pacienteId }
    val etiquetaPaciente = pacienteSeleccionado?.let { "${it.nombreCompleto} · ${it.rut}" }
        ?: "Seleccionar paciente"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.errorGeneral != null) {
            item { TextoError(uiState.errorGeneral) }
        }

        item {
            Text(
                text = "Datos del examen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SelectorCampo(
                etiqueta = "Paciente",
                valor = etiquetaPaciente,
                opciones = uiState.pacientes,
                etiquetaOpcion = { "${it.nombreCompleto} · ${it.rut}" },
                onSeleccionar = { onPaciente(it.id) },
                error = uiState.errores["paciente"],
                placeholder = "Seleccionar paciente"
            )
        }

        if (uiState.pacientes.isEmpty() && usuario.rol.puedeGestionarPacientes()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VerdeContenedor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "No hay pacientes registrados.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onIrPacientes) {
                            Text(text = "Crear el primer paciente", color = VerdeOfta)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoBoton(
                    etiqueta = "Fecha del examen",
                    valor = uiState.fecha,
                    icono = Icons.Filled.CalendarMonth,
                    error = uiState.errores["fecha"],
                    onClick = {
                        val inicial = Fechas.parseVisible(uiState.fecha) ?: java.time.LocalDate.now()
                        DatePickerDialog(
                            contexto,
                            { _, anio, mes, dia ->
                                onFecha(
                                    String.format(
                                        java.util.Locale.ROOT,
                                        "%02d/%02d/%04d",
                                        dia,
                                        mes + 1,
                                        anio
                                    )
                                )
                            },
                            inicial.year,
                            inicial.monthValue - 1,
                            inicial.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                )
                CampoBoton(
                    etiqueta = "Hora",
                    valor = uiState.hora,
                    icono = Icons.Filled.Schedule,
                    error = uiState.errores["hora"],
                    onClick = {
                        val inicial = runCatching {
                            LocalTime.parse(uiState.hora)
                        }.getOrDefault(LocalTime.now())
                        TimePickerDialog(
                            contexto,
                            { _, hora, minuto ->
                                onHora("%02d:%02d".format(hora, minuto))
                            },
                            inicial.hour,
                            inicial.minute,
                            true
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SelectorCampo(
                etiqueta = "Tipo de examen",
                valor = uiState.tipoExamen,
                opciones = Catalogos.tiposExamen,
                etiquetaOpcion = { it },
                onSeleccionar = onTipoExamen,
                error = uiState.errores["tipoExamen"]
            )
        }

        item {
            SelectorCampo(
                etiqueta = "Sucursal",
                valor = uiState.sucursal,
                opciones = Catalogos.sucursales,
                etiquetaOpcion = { it },
                onSeleccionar = onSucursal,
                error = uiState.errores["sucursal"]
            )
        }

        item {
            val profesionales = if (
                uiState.profesional.isNotBlank() &&
                uiState.profesional !in uiState.profesionales
            ) {
                uiState.profesionales + uiState.profesional
            } else {
                uiState.profesionales
            }
            SelectorCampo(
                etiqueta = "Profesional",
                valor = uiState.profesional,
                opciones = profesionales,
                etiquetaOpcion = { it },
                onSeleccionar = onProfesional,
                error = uiState.errores["profesional"]
            )
        }

        item {
            SelectorCampo(
                etiqueta = "Ojo evaluado",
                valor = uiState.ojoEvaluado,
                opciones = Catalogos.ojos,
                etiquetaOpcion = { it },
                onSeleccionar = onOjoEvaluado,
                error = uiState.errores["ojoEvaluado"]
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estado clínico y documento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            if (uiState.estadosDisponibles.size > 1) {
                SelectorCampo(
                    etiqueta = "Estado",
                    valor = uiState.estado.etiqueta,
                    opciones = uiState.estadosDisponibles,
                    etiquetaOpcion = { it.etiqueta },
                    onSeleccionar = onEstado,
                    error = uiState.errores["estado"]
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Estado",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextoSecundario
                        )
                        Text(
                            text = uiState.estado.etiqueta,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (
                                uiState.estadoOriginal == EstadoExamen.OBSERVADO &&
                                usuario.rol == com.equipo7.oftaapp.model.Rol.TENS
                            ) {
                                "Este examen estaba Observado. Al guardar la corrección volverá a Pendiente."
                            } else {
                                "El perfil TENS registra exámenes en estado Pendiente; la validación la realiza un oftalmólogo o administrador."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }
            }
        }

        item {
            uiState.documentoExistente?.let { documento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VerdeContenedor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Description, contentDescription = null)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = "Documento actual",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = documento.nombreOriginal,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = Archivos.formatearTamano(documento.tamanoBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                    }
                }
            }
        }

        item {
            uiState.documentoSeleccionado?.let { documento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.AttachFile, contentDescription = null)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = "Nuevo documento",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextoSecundario
                            )
                            Text(
                                text = documento.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (documento.tamanoBytes >= 0) {
                                    Archivos.formatearTamano(documento.tamanoBytes)
                                } else {
                                    "Tamaño por verificar"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        IconButton(onClick = onQuitarDocumento) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Quitar documento"
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    selectorDocumento.launch(arrayOf("application/pdf", "image/*"))
                },
                enabled = !uiState.preparandoDocumento && !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.preparandoDocumento) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Leyendo documento…")
                } else {
                    Icon(imageVector = Icons.Filled.AttachFile, contentDescription = null)
                    Text(
                        text = if (uiState.documentoSeleccionado == null) {
                            "Adjuntar PDF o imagen"
                        } else {
                            "Reemplazar documento seleccionado"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            uiState.errores["documento"]?.let {
                Spacer(modifier = Modifier.height(4.dp))
                TextoError(it)
            }
            Text(
                text = "Formatos PDF o imagen. Tamaño máximo: 10 MB.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            CampoTexto(
                etiqueta = "Observaciones",
                valor = uiState.observaciones,
                onValorCambiado = onObservaciones,
                error = uiState.errores["observaciones"],
                textoAyuda = "${uiState.observaciones.length}/500 caracteres",
                lineas = 4,
                placeholder = "Detalle clínico, indicaciones o motivo de observación"
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onGuardar,
                enabled = !uiState.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Guardando…")
                } else {
                    Text(
                        text = if (uiState.modoEdicion) "Guardar cambios" else "Registrar examen",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TextButton(
                onClick = onCancelar,
                enabled = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar")
            }
        }

        item {
            IconoConTexto(
                icono = Icons.Filled.Description,
                texto = "Los estados Validado y Entregado requieren un documento adjunto. Un examen Observado requiere una observación."
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CampoBoton(
    etiqueta: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = if (error == null) TextoSecundario else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = VerdeOfta
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
