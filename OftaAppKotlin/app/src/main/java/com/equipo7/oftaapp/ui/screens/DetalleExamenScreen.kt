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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.DocumentoAdjunto
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.DocumentoRepository
import com.equipo7.oftaapp.ui.components.EtiquetaEstado
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.components.TextoError
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.ui.theme.VerdeContenedor
import com.equipo7.oftaapp.util.Archivos
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.viewmodel.DetalleExamenUiState
import kotlinx.coroutines.launch

@Composable
fun DetalleExamenScreen(
    uiState: DetalleExamenUiState,
    usuario: Usuario,
    documentoRepository: DocumentoRepository,
    onEditar: (String) -> Unit,
    onCambiarEstado: (EstadoExamen, String) -> Unit,
    onEliminarDocumento: (DocumentoAdjunto) -> Unit,
    onMensaje: (String) -> Unit,
    onMensajeAtendido: () -> Unit,
    onErrorAtendido: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialogoObservacion by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var documentoParaEliminar by remember { mutableStateOf<DocumentoAdjunto?>(null) }
    var generandoPdf by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            onMensaje(it)
            onMensajeAtendido()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onMensaje(it)
            onErrorAtendido()
        }
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

    val examen = uiState.examen
    if (examen == null) {
        EstadoVacio(
            titulo = "Examen no disponible",
            detalle = uiState.error ?: "No se encontró el examen solicitado.",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val puedeValidar = usuario.rol.puedeValidarExamen() &&
        examen.estado != EstadoExamen.VALIDADO &&
        examen.estado != EstadoExamen.ENTREGADO
    val puedeObservar = usuario.rol.puedeValidarExamen() &&
        examen.estado != EstadoExamen.ENTREGADO
    val puedeEntregar = usuario.rol.puedeEntregarExamen() &&
        examen.estado == EstadoExamen.VALIDADO
    val puedeEditar = usuario.rol.puedeEditarExamen(examen.estado)

    if (dialogoObservacion) {
        AlertDialog(
            onDismissRequest = { dialogoObservacion = false },
            containerColor = TarjetaBlanca,
            title = { Text(text = "Marcar como observado") },
            text = {
                Column {
                    Text(
                        text = "Describe el motivo clínico de la observación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    com.equipo7.oftaapp.ui.components.CampoTexto(
                        etiqueta = "Observación",
                        valor = observacion,
                        onValorCambiado = { observacion = it.take(500) },
                        textoAyuda = "${observacion.length}/500 caracteres",
                        lineas = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCambiarEstado(EstadoExamen.OBSERVADO, observacion)
                        dialogoObservacion = false
                    },
                    enabled = observacion.isNotBlank()
                ) {
                    Text(text = "Guardar observación", color = VerdeOfta)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoObservacion = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    documentoParaEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoParaEliminar = null },
            containerColor = TarjetaBlanca,
            title = { Text(text = "Eliminar documento") },
            text = {
                Text(
                    text = "Se eliminará ${documento.nombreOriginal} del examen. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarDocumento(documento)
                        documentoParaEliminar = null
                    }
                ) {
                    Text(text = "Eliminar", color = ErrorFormulario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentoParaEliminar = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    fun generarInforme(compartir: Boolean) {
        if (generandoPdf) return
        generandoPdf = true
        scope.launch {
            documentoRepository.generarInforme(examen)
                .onSuccess { ruta ->
                    if (compartir) {
                        documentoRepository.compartir(
                            ruta,
                            "application/pdf",
                            "informe_${examen.id}.pdf"
                        )
                    } else {
                        documentoRepository.abrir(ruta, "application/pdf")
                    }
                }
                .onFailure { onMensaje(it.message ?: "No se pudo generar el informe.") }
            generandoPdf = false
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.procesando) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = examen.tipoExamen,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaEstado(estado = examen.estado)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = examen.pacienteNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "RUT ${examen.pacienteRut}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DivisorSuave)
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaDato("Fecha", "${Fechas.larga(examen.fecha)} · ${examen.hora}")
                    FilaDato("Sucursal", examen.sucursal)
                    FilaDato("Profesional", examen.profesional)
                    FilaDato("Ojo evaluado", examen.ojoEvaluado)
                    FilaDato("ID del examen", examen.id)
                }
            }
        }

        if (examen.observaciones.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Observaciones",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = examen.observaciones,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Documentos adjuntos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.documentos.isEmpty()) {
                        Text(
                            text = "Sin documentos adjuntos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario
                        )
                    } else {
                        uiState.documentos.forEach { documento ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = VerdeOfta
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                ) {
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
                                IconButton(
                                    onClick = {
                                        documentoRepository.abrir(
                                            documento.rutaInterna,
                                            documento.mimeType
                                        ).onFailure {
                                            onMensaje(it.message ?: "No se pudo abrir el archivo.")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInNew,
                                        contentDescription = "Abrir documento"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        documentoRepository.compartir(
                                            documento.rutaInterna,
                                            documento.mimeType,
                                            documento.nombreOriginal
                                        ).onFailure {
                                            onMensaje(it.message ?: "No se pudo compartir el archivo.")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Compartir documento"
                                    )
                                }
                                if (
                                    usuario.rol.puedeEliminarDocumentos() &&
                                    examen.estado != EstadoExamen.ENTREGADO
                                ) {
                                    IconButton(onClick = { documentoParaEliminar = documento }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Eliminar documento",
                                            tint = ErrorFormulario
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informe del examen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { generarInforme(false) },
                            enabled = !generandoPdf,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Ver PDF", modifier = Modifier.padding(start = 6.dp))
                        }
                        OutlinedButton(
                            onClick = { generarInforme(true) },
                            enabled = !generandoPdf,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Compartir PDF", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Text(
                        text = "El informe resume los datos del examen y se puede compartir con el paciente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        if (puedeEditar || puedeValidar || puedeObservar || puedeEntregar) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Acciones clínicas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (puedeEditar) {
                            OutlinedButton(
                                onClick = { onEditar(examen.id) },
                                enabled = !uiState.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = "Editar examen", modifier = Modifier.padding(start = 8.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (puedeValidar) {
                            Button(
                                onClick = {
                                    if (uiState.documentos.isEmpty()) {
                                        onMensaje("Adjunta un documento antes de validar.")
                                    } else {
                                        onCambiarEstado(EstadoExamen.VALIDADO, "")
                                    }
                                },
                                enabled = !uiState.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Validar examen")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (puedeEntregar) {
                            Button(
                                onClick = {
                                    if (uiState.documentos.isEmpty()) {
                                        onMensaje("El examen necesita un documento antes de entregarse.")
                                    } else {
                                        onCambiarEstado(EstadoExamen.ENTREGADO, "")
                                    }
                                },
                                enabled = !uiState.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Marcar como entregado")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (puedeObservar) {
                            OutlinedButton(
                                onClick = {
                                    observacion = examen.observaciones
                                    dialogoObservacion = true
                                },
                                enabled = !uiState.procesando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Marcar como observado")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Los datos son ficticios. Esta aplicación no emite diagnósticos ni reemplaza una evaluación clínica.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
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
            modifier = Modifier.weight(1.4f)
        )
    }
}
