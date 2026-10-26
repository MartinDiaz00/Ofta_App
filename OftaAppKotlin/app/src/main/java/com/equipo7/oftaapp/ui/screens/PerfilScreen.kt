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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeContenedor
import com.equipo7.oftaapp.ui.theme.VerdeOfta

@Composable
fun PerfilScreen(
    usuario: Usuario,
    textoGrande: Boolean,
    onCambiarTextoGrande: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmarSalida by remember { mutableStateOf(false) }

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
                        text = "Datos y privacidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "La información se guarda en Room dentro del dispositivo. Los documentos adjuntos se copian al almacenamiento interno y solo se comparten cuando tú lo solicitas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DivisorSuave)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las notificaciones son locales e inmediatas. La app no envía datos a un servidor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
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
