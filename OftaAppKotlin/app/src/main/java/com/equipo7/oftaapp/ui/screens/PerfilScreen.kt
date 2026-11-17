package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.BuildConfig
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.network.FhirRemoteService
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeContenedor
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    usuario: Usuario,
    textoGrande: Boolean,
    onCambiarTextoGrande: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    fhirRemoteService: FhirRemoteService? = null,
    onReintentarSincronizacion: (() -> Unit)? = null
) {
    var confirmarSalida by remember { mutableStateOf(false) }
    var probandoFhir by remember { mutableStateOf(false) }
    var resultadoFhir by remember { mutableStateOf<String?>(null) }
    var detalleFhir by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = TarjetaBlanca,
            title = { Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Volverás a la pantalla de inicio de sesión. Los datos locales se conservarán.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        onCerrarSesion()
                    }
                ) {
                    Text(text = "Cerrar sesión", color = ErrorFormulario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .background(VerdeContenedor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales(usuario.nombreCompleto),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOfta
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "@${usuario.usuario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = usuario.rol.nombreMostrar,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta,
                        modifier = Modifier
                            .background(VerdeContenedor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
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
                        text = "Permisos del rol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    permisos(usuario.rol).forEach { permiso ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = VerdeOfta,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = permiso,
                                style = MaterialTheme.typography.bodyMedium,
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
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accesibilidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Texto grande",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Aumenta el tamaño del texto en toda la aplicación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        Switch(
                            checked = textoGrande,
                            onCheckedChange = onCambiarTextoGrande
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
                        text = "Datos, API y privacidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "La información operativa se guarda en Room dentro del dispositivo. Los documentos adjuntos se copian al almacenamiento interno y solo se comparten cuando tú lo solicitas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DivisorSuave)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "La app integra Retrofit para comunicación HTTP y sincroniza recursos FHIR R4 (Patient, DiagnosticReport) con el microservicio HAPI FHIR cuando hay conectividad. Las notificaciones son locales. Datos de demostración académica.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    if (fhirRemoteService != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                probandoFhir = true
                                resultadoFhir = null
                                detalleFhir = null
                                scope.launch {
                                    val r = fhirRemoteService.verificarConexion()
                                    r.fold(
                                        onSuccess = {
                                            resultadoFhir = it.mensaje
                                            detalleFhir = it.detalle
                                        },
                                        onFailure = {
                                            resultadoFhir = "Error: ${it.message}"
                                            detalleFhir = null
                                        }
                                    )
                                    probandoFhir = false
                                }
                            },
                            enabled = !probandoFhir,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (probandoFhir) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.CloudSync, contentDescription = null)
                            }
                            Text(
                                text = if (probandoFhir) "Probando..." else "Probar conexión API/HAPI FHIR",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = "Qué hace: verifica el endpoint FHIR y consulta su CapabilityStatement " +
                                "(/metadata) para confirmar que el servidor responde. Al tocarlo se envía " +
                                "una petición HTTP y aquí se muestra el resultado (FHIR OK, versión R4 y " +
                                "cantidad de Patients/DiagnosticReports) o el error de red si el servicio " +
                                "está caído. No modifica ni envía datos clínicos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            text = "Endpoint: ${BuildConfig.FHIR_BASE_URL}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        resultadoFhir?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (it.startsWith("Error")) ErrorFormulario else VerdeOfta,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        detalleFhir?.let {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario
                            )
                        }
                        if (onReintentarSincronizacion != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onReintentarSincronizacion,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.CloudSync, contentDescription = null)
                                Text(
                                    text = "Reintentar sincronización",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Text(
                                text = "Reenvía pacientes y exámenes guardados en el dispositivo al servidor FHIR. " +
                                    "Es seguro: si el dato ya existe se actualiza en vez de duplicarse.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundario,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { confirmarSalida = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorFormulario,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(imageVector = Icons.Filled.Logout, contentDescription = null)
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        item {
            Text(
                text = "MVP académico de OftaApp · Datos ficticios · No emite diagnósticos ni reemplaza una evaluación clínica.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

private fun iniciales(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    if (partes.isEmpty()) return "OA"
    val primera = partes.first().firstOrNull()?.uppercaseChar() ?: 'O'
    val segunda = partes.getOrNull(1)?.firstOrNull()?.uppercaseChar()
    return if (segunda != null) "$primera$segunda" else primera.toString()
}

private fun permisos(rol: Rol): List<String> = when (rol) {
    Rol.ADMINISTRADOR -> listOf(
        "Gestionar pacientes y usuarios",
        "Registrar y editar exámenes",
        "Validar, observar y entregar exámenes",
        "Eliminar documentos adjuntos",
        "Ver todos los pacientes y exámenes"
    )
    Rol.OFTALMOLOGO -> listOf(
        "Ver todos los pacientes y exámenes",
        "Editar exámenes",
        "Validar, observar y entregar exámenes"
    )
    Rol.TENS -> listOf(
        "Gestionar pacientes",
        "Registrar exámenes pendientes",
        "Editar exámenes pendientes",
        "Ver todos los pacientes y exámenes"
    )
    Rol.PACIENTE -> listOf(
        "Ver solo mis exámenes",
        "Abrir el detalle de mis exámenes",
        "Ajustar el tamaño del texto"
    )
}
