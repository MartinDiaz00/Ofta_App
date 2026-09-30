package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPlaceholder
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoRegistrar(
    pacientes: List<Paciente>,
    onGuardar: (ExamenOftalmologico) -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombresPacientes = remember(pacientes) {
        if (pacientes.isEmpty()) listOf("Sin pacientes")
        else pacientes.map { it.nombreCompleto }
    }

    var paciente by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var sucursal by remember { mutableStateOf("") }
    var profesional by remember { mutableStateOf("") }
    var tipoExamen by remember { mutableStateOf("") }
    var ojoEvaluado by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var tituloAlerta by remember { mutableStateOf("") }
    var textoAlerta by remember { mutableStateOf("") }
    var alertaVisible by remember { mutableStateOf(false) }

    val guardar = {
        val camposVacios = buildList {
            if (paciente.isBlank()) add("Paciente")
            if (fecha.isBlank()) add("Fecha")
            if (sucursal.isBlank()) add("Sucursal")
            if (profesional.isBlank()) add("Profesional")
            if (tipoExamen.isBlank()) add("Tipo de examen")
            if (ojoEvaluado.isBlank()) add("Ojo evaluado")
            if (estado.isBlank()) add("Estado")
            if (observaciones.isBlank()) add("Observaciones")
        }

        when {
            camposVacios.isNotEmpty() -> {
                tituloAlerta = "Datos vacíos o datos incompletos"
                textoAlerta = "Faltan los siguientes campos: " +
                        camposVacios.joinToString(", ") +
                        ". Completa el formulario antes de guardar."
                alertaVisible = true
            }

            !esFechaValida(fecha) -> {
                tituloAlerta = "Fecha inválida"
                textoAlerta = "Revisa el campo Fecha: el formato debe ser " +
                        "DD/MM/YYYY (ejemplo: 28/09/2026)."
                alertaVisible = true
            }

            else -> {
                onGuardar(
                    ExamenOftalmologico(
                        id = "",
                        pacienteNombre = paciente.trim(),
                        tipoExamen = tipoExamen.trim(),
                        fecha = fecha.trim(),
                        medicoSolicitante = profesional.trim(),
                        estado = estado.trim(),
                        observaciones = observaciones.trim().ifEmpty { "Sin observaciones" },
                        ojoEvaluado = ojoEvaluado.trim(),
                        sucursal = sucursal.trim()
                    )
                )
                onGuardado()
            }
        }
    }

    if (alertaVisible) {
        AlertDialog(
            onDismissRequest = { alertaVisible = false },
            containerColor = TarjetaBlanca,
            title = {
                Text(
                    text = tituloAlerta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorFormulario
                )
            },
            text = {
                Text(
                    text = textoAlerta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(onClick = { alertaVisible = false }) {
                    Text(
                        text = "Entendido",
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        CampoSelector(
            etiqueta = "Paciente",
            valor = paciente,
            placeholder = "Seleccione un paciente",
            opciones = nombresPacientes,
            onSeleccionar = { paciente = it }
        )

        CampoTexto(
            etiqueta = "Fecha",
            valor = fecha,
            placeholder = "DD/MM/YYYY",
            onValorCambiado = { fecha = it },
            tipoTeclado = KeyboardType.Text
        )

        CampoSelector(
            etiqueta = "Sucursal",
            valor = sucursal,
            placeholder = "Seleccione una sucursal",
            opciones = listOf("Puerto Montt", "Santiago", "Valdivia"),
            onSeleccionar = { sucursal = it }
        )

        CampoSelector(
            etiqueta = "Profesional",
            valor = profesional,
            placeholder = "Seleccione un profesional",
            opciones = listOf("Dr. Roberto Silva", "Dra. Ana López", "Dra. Fernández"),
            onSeleccionar = { profesional = it }
        )

        CampoSelector(
            etiqueta = "Tipo de examen",
            valor = tipoExamen,
            placeholder = "Seleccione un tipo de examen",
            opciones = listOf(
                "Tonometría de Aplanación",
                "Campimetría Computarizada",
                "Tomografía de Coherencia Óptica (OCT)",
                "Fondo de ojo",
                "Agudeza visual"
            ),
            onSeleccionar = { tipoExamen = it }
        )

        CampoSelector(
            etiqueta = "Ojo evaluado",
            valor = ojoEvaluado,
            placeholder = "Seleccione el ojo evaluado",
            opciones = listOf("Derecho", "Izquierdo", "Ambos"),
            onSeleccionar = { ojoEvaluado = it }
        )

        CampoSelector(
            etiqueta = "Estado",
            valor = estado,
            placeholder = "Seleccione el estado",
            opciones = listOf("Pendiente", "Completado"),
            onSeleccionar = { estado = it }
        )

        CampoTexto(
            etiqueta = "Observaciones",
            valor = observaciones,
            placeholder = "Escriba las observaciones",
            onValorCambiado = { observaciones = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "resultado.pdf",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoPrincipal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp)
                )
            }

            Button(
                onClick = guardar,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOfta,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Guardar examen",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorCambiado: (String) -> Unit,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = valor,
                onValueChange = onValorCambiado,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoSecundario),
                keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
                decorationBox = { innerTextField ->
                    Box {
                        if (valor.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoPlaceholder
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun CampoSelector(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    placeholder: String = ""
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
                    opciones.forEach { opcion ->
                        TextButton(
                            onClick = {
                                onSeleccionar(opcion)
                                visible = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = opcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (opcion == valor) VerdeOfta else TextoPrincipal,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { visible = true },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = valor.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (valor.isEmpty()) TextoPlaceholder else TextoSecundario
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Elegir $etiqueta",
                tint = TextoSecundario
            )
        }
    }
}

private fun esFechaValida(valor: String): Boolean {
    val texto = valor.trim()

    if (!texto.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))) return false

    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        formato.parse(texto) != null
    } catch (e: ParseException) {
        false
    }
}
