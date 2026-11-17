package com.equipo7.oftaapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo decorativo del login: degradado azul clínico con un ojo abstracto y puntos,
 * inspirado en la imagen de referencia de oftalmología (sin marcas de agua ni stock).
 */
@androidx.compose.runtime.Composable
fun FondoOftalmologia(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val ancho = size.width
        val alto = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF2C6FA8),
                    Color(0xFF5A9BD1),
                    Color(0xFFA9D4EE),
                    Color(0xFFE8F6FE)
                )
            )
        )
        val centro = Offset(ancho / 2f, alto * 0.40f)
        // Halo suave detrás del ojo
        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = ancho * 0.62f,
            center = centro
        )
        // Ojo abstracto
        drawOval(
            color = Color.White.copy(alpha = 0.16f),
            topLeft = Offset(ancho * 0.12f, alto * 0.30f),
            size = Size(ancho * 0.76f, alto * 0.20f)
        )
        drawCircle(color = Color(0xFF7FB0E0).copy(alpha = 0.85f), radius = ancho * 0.13f, center = centro)
        drawCircle(color = Color(0xFF244C7C), radius = ancho * 0.070f, center = centro)
        drawCircle(color = Color.White.copy(alpha = 0.55f), radius = ancho * 0.022f,
            center = Offset(centro.x - ancho * 0.035f, centro.y - ancho * 0.035f))
        // Puntos around the iris
        for (i in 0 until 28) {
            val angulo = (i / 28f) * 2f * kotlin.math.PI
            val radio = ancho * 0.20f
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = ancho * 0.008f,
                center = Offset(centro.x + cos(angulo).toFloat() * radio,
                    centro.y + sin(angulo).toFloat() * radio * 0.55f)
            )
        }
    }
}
