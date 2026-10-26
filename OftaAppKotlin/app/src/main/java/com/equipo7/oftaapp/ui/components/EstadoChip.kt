package com.equipo7.oftaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.ui.theme.EstadoEntregado
import com.equipo7.oftaapp.ui.theme.EstadoObservado
import com.equipo7.oftaapp.ui.theme.EstadoPendiente
import com.equipo7.oftaapp.ui.theme.VerdeOfta

@Composable
fun EtiquetaEstado(estado: EstadoExamen, modifier: Modifier = Modifier) {
    val color = colorEstado(estado)
    Text(
        text = estado.etiqueta,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

fun colorEstado(estado: EstadoExamen): Color = when (estado) {
    EstadoExamen.PENDIENTE -> EstadoPendiente
    EstadoExamen.VALIDADO -> VerdeOfta
    EstadoExamen.OBSERVADO -> EstadoObservado
    EstadoExamen.ENTREGADO -> EstadoEntregado
}
