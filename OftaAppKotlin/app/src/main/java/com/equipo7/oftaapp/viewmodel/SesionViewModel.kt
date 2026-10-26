package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SesionUiState(
    val usuario: Usuario? = null,
    val error: String? = null
)

class SesionViewModel : ViewModel() {
    private val repository = UsuarioRepository()

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    fun iniciarSesion(usuario: String, contrasena: String) {
        when {
            usuario.isBlank() || contrasena.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Ingresa tu usuario y contraseña.")
            }

            else -> {
                val cuenta = repository.autenticar(usuario, contrasena)
                if (cuenta == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Usuario o contraseña incorrectos."
                    )
                } else {
                    _uiState.value = SesionUiState(usuario = cuenta)
                }
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = SesionUiState()
    }

    fun errorAtendido() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }
}
