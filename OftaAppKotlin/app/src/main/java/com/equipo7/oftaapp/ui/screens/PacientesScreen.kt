package com.equipo7.oftaapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.equipo7.oftaapp.model.Catalogos
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.CampoTexto
import com.equipo7.oftaapp.ui.components.EstadoVacio
import com.equipo7.oftaapp.ui.components.PacienteCard
import com.equipo7.oftaapp.ui.components.SelectorCampo
import com.equipo7.oftaapp.ui.components.TextoError
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import com.equipo7.oftaapp.viewmodel.EditorPaciente
import com.equipo7.oftaapp.viewmodel.PacientesUiState
import java.time.LocalDate

@Composable
fun PacientesScreen(
    uiState: PacientesUiState,
    usuario: Usuario,
    onBusqueda: (String) -> Unit,
    onNuevo: () -> Unit,
    onEditar: (Paciente) -> Unit,
    onAbrirFicha: (String) -> Unit,
    onActualizarEditor: ((EditorPaciente) -> EditorPaciente) -> Unit,
    onGuardar: () -> Unit,
    onCerrarEditor: () -> Unit,
    onMensaje: (String) -> Unit,
    onMensajeAtendido: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            onMensaje(it)
            onMensajeAtendido()
        }
    }

    if (usuario.rol == Rol.PACIENTE) {
        EstadoVacio(
            titulo = "Acceso restringido",
            detalle = "La gestión de pacientes está disponible para TENS, oftalmólogos y administradores.",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    uiState.editor?.let { editor ->
        EditorPacienteDialog(
            editor = editor,
            guardando = uiState.guardando,
            onActualizar = onActualizarEditor,
            onGuardar = onGuardar,
            onCerrar = onCerrarEditor
        )
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.error != null) {
            item { TextoError(uiState.error) }
        }

        item {
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = onBusqueda,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, RUT o teléfono") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.busqueda.isNotBlank()) {
                        IconButton(onClick = { onBusqueda("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pacientes registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = uiState.pacientes.size.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = VerdeOfta,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val filtrados = uiState.pacientes.filter { paciente ->
            val texto = uiState.busqueda.trim()
            texto.isBlank() ||
                paciente.nombreCompleto.contains(texto, ignoreCase = true) ||
                paciente.rut.contains(texto, ignoreCase = true) ||
                paciente.telefonoPrincipal.contains(texto, ignoreCase = true)
        }

        if (filtrados.isEmpty()) {
            item {
                EstadoVacio(
                    titulo = if (uiState.pacientes.isEmpty()) "Sin pacientes" else "Sin resultados",
                    detalle = if (uiState.pacientes.isEmpty()) {
                        if (usuario.rol.puedeGestionarPacientes()) {
                            "Usa el botón + para registrar el primer paciente."
                        } else {
                            "No hay pacientes registrados."
                        }
                    } else {
                        "Prueba con otro nombre, RUT o teléfono."
                    }
                )
            }
        } else {
            items(filtrados, key = Paciente::id) { paciente ->
                PacienteCard(
                    paciente = paciente,
                    onClick = { onAbrirFicha(paciente.id) },
                    onEditar = if (usuario.rol.puedeGestionarPacientes()) {
                        { onEditar(paciente) }
                    } else {
                        null
                    }
                )
            }
        }

        item {
            Text(
                text = "Los datos de los pacientes son ficticios y se almacenan localmente.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 6.dp, bottom = 70.dp)
            )
        }
    }
}

@Composable
private fun EditorPacienteDialog(
    editor: EditorPaciente,
    guardando: Boolean,
    onActualizar: ((EditorPaciente) -> EditorPaciente) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    val contexto = LocalContext.current
    val fecha = Fechas.parseVisible(editor.fechaNacimiento) ?: LocalDate.of(1990, 1, 1)

    Dialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(16.dp)
                .imePadding(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (editor.id.isBlank()) "Nuevo paciente" else "Editar paciente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCerrar, enabled = !guardando) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (editor.errorGeneral != null) {
                    TextoError(editor.errorGeneral)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                CampoTexto(
                    etiqueta = "RUT",
                    valor = editor.rut,
                    onValorCambiado = { valor ->
                        onActualizar {
                            it.copy(rut = Validadores.formatearRutMientrasEscribe(valor))
                        }
                    },
                    placeholder = "12.345.678-9",
                    error = editor.errores["rut"]
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoTexto(
                    etiqueta = "Nombre completo",
                    valor = editor.nombreCompleto,
                    onValorCambiado = { valor ->
                        onActualizar { it.copy(nombreCompleto = valor) }
                    },
                    error = editor.errores["nombreCompleto"]
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fecha de nacimiento",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            contexto,
                            { _, anio, mes, dia ->
                                val elegida = LocalDate.of(anio, mes + 1, dia)
                                onActualizar { it.copy(fechaNacimiento = elegida.format(Fechas.formatoVisible)) }
                            },
                            fecha.year,
                            fecha.monthValue - 1,
                            fecha.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null
                    )
                    Text(
                        text = editor.fechaNacimiento.ifBlank { "Seleccionar fecha" },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                editor.errores["fechaNacimiento"]?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                CampoTexto(
                    etiqueta = "Correo electrónico",
                    valor = editor.correo,
                    onValorCambiado = { valor ->
                        onActualizar { it.copy(correo = valor) }
                    },
                    tipoTeclado = KeyboardType.Email,
                    error = editor.errores["correo"]
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoTexto(
                    etiqueta = "Teléfono principal",
                    valor = editor.telefonoPrincipal,
                    onValorCambiado = { valor ->
                        onActualizar { it.copy(telefonoPrincipal = valor) }
                    },
                    placeholder = "+56 9 1234 5678",
                    tipoTeclado = KeyboardType.Phone,
                    error = editor.errores["telefonoPrincipal"]
                )
                Spacer(modifier = Modifier.height(8.dp))
                CampoTexto(
                    etiqueta = "Teléfono secundario (opcional)",
                    valor = editor.telefonoSecundario,
                    onValorCambiado = { valor ->
                        onActualizar { it.copy(telefonoSecundario = valor) }
                    },
                    tipoTeclado = KeyboardType.Phone,
                    error = editor.errores["telefonoSecundario"]
                )
                Spacer(modifier = Modifier.height(8.dp))
                SelectorCampo(
                    etiqueta = "Previsión",
                    valor = editor.prevision,
                    opciones = Catalogos.previsiones,
                    etiquetaOpcion = { it },
                    onSeleccionar = { valor ->
                        onActualizar { it.copy(prevision = valor) }
                    },
                    habilitado = !guardando
                )

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onGuardar,
                    enabled = !guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Guardando…")
                    } else {
                        Text(text = "Guardar paciente", fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(
                    onClick = onCerrar,
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancelar")
                }
            }
        }
    }
}
