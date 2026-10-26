package com.equipo7.oftaapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.R
import com.equipo7.oftaapp.ui.components.IconosDeBarra
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.FondoClaro
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta

private data class CuentaDemo(
    val usuario: String,
    val nombre: String,
    val rol: String
)

private val cuentasDemo = listOf(
    CuentaDemo("admin.ofta", "Administrador Ofta", "Administrador"),
    CuentaDemo("dra.fernandez", "Dra. Valentina Fernández", "Oftalmóloga"),
    CuentaDemo("dr.silva", "Dr. Roberto Silva", "Oftalmólogo"),
    CuentaDemo("tens.martinez", "TENS Camila Martínez", "TENS"),
    CuentaDemo("tens.rojas", "TENS Diego Rojas", "TENS"),
    CuentaDemo("paciente.demo", "Camila Rojas", "Paciente")
)

@Composable
fun LoginScreen(
    error: String?,
    errores: Map<String, String>,
    preparando: Boolean,
    onIniciarSesion: (usuario: String, contrasena: String) -> Unit,
    onCampoModificado: () -> Unit
) {
    IconosDeBarra(fondoClaro = true)

    val contexto = LocalContext.current
    var confirmarSalida by remember { mutableStateOf(false) }
    var mostrarCuentas by remember { mutableStateOf(false) }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (confirmarSalida) confirmarSalida = false else confirmarSalida = true
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = TarjetaBlanca,
            title = {
                Text(
                    text = "Salir de OftaApp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Text(
                    text = "¿Quieres salir de la aplicación?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        (contexto as? Activity)?.finish()
                    }
                ) {
                    Text(text = "Salir", fontWeight = FontWeight.Bold, color = ErrorFormulario)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(text = "Cancelar", fontWeight = FontWeight.Bold, color = VerdeOfta)
                }
            }
        )
    }

    if (mostrarCuentas) {
        AlertDialog(
            onDismissRequest = { mostrarCuentas = false },
            containerColor = TarjetaBlanca,
            title = {
                Text(
                    text = "Cuentas de demostración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Todas usan la contraseña 1234. Toca una cuenta para completar el formulario.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    cuentasDemo.forEach { cuenta ->
                        TextButton(
                            onClick = {
                                usuario = cuenta.usuario
                                contrasena = "1234"
                                mostrarCuentas = false
                                onCampoModificado()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = cuenta.nombre,
                                    color = TextoPrincipal,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${cuenta.usuario} · ${cuenta.rol}",
                                    color = TextoSecundario,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarCuentas = false }) {
                    Text(text = "Cerrar", color = VerdeOfta)
                }
            }
        )
    }

    val intentarIniciarSesion = {
        if (!preparando) {
            focusManager.clearFocus()
            onIniciarSesion(usuario, contrasena)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de OftaApp",
                modifier = Modifier.size(88.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Ofta App",
                style = MaterialTheme.typography.headlineSmall,
                color = TextoPrincipal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fichas Oftalmológicas Digitales",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    onCampoModificado()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuario") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                },
                isError = errores["usuario"] != null,
                supportingText = errores["usuario"]?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TarjetaBlanca,
                    unfocusedContainerColor = TarjetaBlanca,
                    focusedBorderColor = VerdeOfta,
                    cursorColor = VerdeOfta
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    onCampoModificado()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { verContrasena = !verContrasena }) {
                        Icon(
                            imageVector = if (verContrasena) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (verContrasena) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        )
                    }
                },
                isError = errores["contrasena"] != null,
                supportingText = errores["contrasena"]?.let { { Text(it) } },
                singleLine = true,
                visualTransformation = if (verContrasena) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { intentarIniciarSesion() }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TarjetaBlanca,
                    unfocusedContainerColor = TarjetaBlanca,
                    focusedBorderColor = VerdeOfta,
                    cursorColor = VerdeOfta
                )
            )

            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorFormulario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = intentarIniciarSesion,
                enabled = !preparando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOfta,
                    contentColor = Color.White
                )
            ) {
                if (preparando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Preparando datos…", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TextButton(
                onClick = { mostrarCuentas = true },
                enabled = !preparando
            ) {
                Text(
                    text = "Ver cuentas demo",
                    style = MaterialTheme.typography.labelLarge,
                    color = VerdeOfta
                )
            }

            HorizontalDivider(color = DivisorSuave)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MVP académico con datos ficticios. No reemplaza una evaluación ni el criterio de un profesional de salud.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
