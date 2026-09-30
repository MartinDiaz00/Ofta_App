package com.equipo7.oftaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo7.oftaapp.ui.screens.LoginScreen
import com.equipo7.oftaapp.ui.screens.MenuPrincipalScreen
import com.equipo7.oftaapp.ui.theme.OftaAppTheme
import com.equipo7.oftaapp.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OftaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sesionViewModel: SesionViewModel = viewModel()
                    val sesion by sesionViewModel.uiState.collectAsState()
                    val usuario = sesion.usuario

                    if (usuario == null) {
                        LoginScreen(
                            error = sesion.error,
                            onIniciarSesion = sesionViewModel::iniciarSesion,
                            onCampoModificado = sesionViewModel::errorAtendido
                        )
                    } else {
                        MenuPrincipalScreen(
                            usuario = usuario,
                            onCerrarSesion = sesionViewModel::cerrarSesion
                        )
                    }
                }
            }
        }
    }
}
