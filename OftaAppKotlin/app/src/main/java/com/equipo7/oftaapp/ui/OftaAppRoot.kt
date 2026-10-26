package com.equipo7.oftaapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo7.oftaapp.OftaApplication
import com.equipo7.oftaapp.ui.navigation.OftaNavHost
import com.equipo7.oftaapp.ui.screens.LoginScreen
import com.equipo7.oftaapp.viewmodel.FabricasViewModel
import com.equipo7.oftaapp.viewmodel.SesionViewModel

@Composable
fun OftaAppRoot() {
    val app = LocalContext.current.applicationContext as OftaApplication
    val sesionViewModel: SesionViewModel = viewModel(
        key = "sesion",
        factory = FabricasViewModel.sesion(app)
    )
    val sesion by sesionViewModel.uiState.collectAsStateWithLifecycle()
    val textoGrande by app.preferenciasRepository.textoGrande.collectAsStateWithLifecycle()
    val densidad = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = densidad.density,
            fontScale = if (textoGrande) 1.2f else densidad.fontScale
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val usuario = sesion.usuario
            if (usuario == null) {
                LoginScreen(
                    error = sesion.error,
                    errores = sesion.errores,
                    preparando = sesion.preparando,
                    onIniciarSesion = sesionViewModel::iniciarSesion,
                    onCampoModificado = sesionViewModel::errorAtendido
                )
            } else {
                OftaNavHost(
                    app = app,
                    usuario = usuario,
                    textoGrande = textoGrande,
                    onCambiarTextoGrande = app.preferenciasRepository::cambiarTextoGrande,
                    onCerrarSesion = sesionViewModel::cerrarSesion
                )
            }
        }
    }
}
