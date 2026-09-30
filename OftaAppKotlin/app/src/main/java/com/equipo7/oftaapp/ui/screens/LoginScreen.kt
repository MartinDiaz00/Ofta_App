package com.equipo7.oftaapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
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
import com.equipo7.oftaapp.repository.UsuarioRepository
import com.equipo7.oftaapp.ui.components.IconosDeBarra
import com.equipo7.oftaapp.ui.theme.CampoLoginOscuro
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.FondoClaro
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPlaceholder
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.ui.theme.VerdeOftaClaro

@Composable
fun LoginScreen(
    error: String?,
    onIniciarSesion: (usuario: String, contrasena: String) -> Unit,
    onCampoModificado: () -> Unit
) {
    IconosDeBarra(fondoClaro = true)

    var confirmarSalida by remember { mutableStateOf(false) }

    val contexto = LocalContext.current

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
                    Text(
                        text = "Salir",
                        fontWeight = FontWeight.Bold,
                        color = ErrorFormulario
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta
                    )
                }
            }
        )
    }

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val intentarIniciarSesion = {
        focusManager.clearFocus()
        onIniciarSesion(usuario, contrasena)
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
            Spacer(modifier = Modifier.height(8.dp))

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

            CampoOscuro(
                valor = usuario,
                onValorCambiado = {
                    usuario = it
                    onCampoModificado()
                },
                placeholder = "Usuario",
                tipoTeclado = KeyboardType.Text,
                accionTeclado = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            CampoOscuro(
                valor = contrasena,
                onValorCambiado = {
                    contrasena = it
                    onCampoModificado()
                },
                placeholder = "Contraseña",
                tipoTeclado = KeyboardType.Password,
                accionTeclado = ImeAction.Done,
                transformacion = PasswordVisualTransformation(),
                onFinalizar = intentarIniciarSesion
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOfta,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            TextButton(onClick = { }) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = VerdeOfta
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = UsuarioRepository.ayudaPrueba,
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CampoOscuro(
    valor: String,
    onValorCambiado: (String) -> Unit,
    placeholder: String,
    tipoTeclado: KeyboardType,
    accionTeclado: ImeAction,
    transformacion: VisualTransformation = VisualTransformation.None,
    onFinalizar: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val estilo = MaterialTheme.typography.bodyLarge.copy(
        color = androidx.compose.ui.graphics.Color.White
    )

    BasicTextField(
        value = valor,
        onValueChange = onValorCambiado,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = transformacion,
        textStyle = estilo,
        cursorBrush = SolidColor(VerdeOftaClaro),
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = accionTeclado
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { onFinalizar() }
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CampoLoginOscuro, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 17.dp)
            ) {
                if (valor.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextoPlaceholder
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}
