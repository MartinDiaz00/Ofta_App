package com.equipo7.oftaapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPlaceholder
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorCambiado: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    textoAyuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        isError = error != null,
        supportingText = when {
            error != null -> {
                { Text(error) }
            }
            textoAyuda != null -> {
                { Text(textoAyuda) }
            }
            else -> null
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = lineas == 1,
        minLines = lineas,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun <T> SelectorCampo(
    etiqueta: String,
    valor: String,
    opciones: List<T>,
    etiquetaOpcion: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar",
    error: String? = null,
    habilitado: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }

    if (visible) {
        AlertDialog(
            onDismissRequest = { visible = false },
            containerColor = TarjetaBlanca,
            title = {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (opciones.isEmpty()) {
                        Text(
                            text = "No hay opciones disponibles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario
                        )
                    }
                    opciones.forEach { opcion ->
                        TextButton(
                            onClick = {
                                onSeleccionar(opcion)
                                visible = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = etiquetaOpcion(opcion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (etiquetaOpcion(opcion) == valor) VerdeOfta else TextoPrincipal,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { visible = false }) {
                    Text(text = "Cerrar", color = VerdeOfta)
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { visible = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (error != null) ErrorFormulario else TextoSecundario
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = valor.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valor.isBlank()) TextoPlaceholder else TextoPrincipal
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorFormulario
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Elegir $etiqueta",
                tint = if (habilitado) TextoSecundario else TextoPlaceholder
            )
        }
    }
}

@Composable
fun EstadoVacio(
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TextoError(texto: String?, modifier: Modifier = Modifier) {
    if (texto.isNullOrBlank()) return
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = ErrorFormulario,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun IconoConTexto(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = TextoSecundario,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )
    }
}
