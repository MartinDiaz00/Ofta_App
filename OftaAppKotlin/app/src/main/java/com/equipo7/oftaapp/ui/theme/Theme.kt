package com.equipo7.oftaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaVerde = lightColorScheme(
    primary = VerdeOfta,
    onPrimary = TextoSobreVerde,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = VerdeOftaOscuro,
    secondary = VerdeOftaClaro,
    onSecondary = TextoSobreVerde,
    secondaryContainer = VerdeContenedor,
    onSecondaryContainer = VerdeOftaOscuro,
    tertiary = VerdeOftaOscuro,
    onTertiary = TextoSobreVerde,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = TarjetaBlanca,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFE9F1ED),
    onSurfaceVariant = TextoSecundario,
    surfaceTint = VerdeOfta,
    outline = DivisorSuave,
    outlineVariant = DivisorSuave,
    error = ErrorFormulario,
    onError = TextoSobreVerde
)

@Composable
fun OftaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaVerde,
        typography = Typography,
        content = content
    )
}
