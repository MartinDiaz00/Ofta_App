package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.AutenticacionRepository
import com.equipo7.oftaapp.repository.DatosDemo
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SesionUiState(
    val usuario: Usuario? = null,
    val preparando: Boolean = true,
    val error: String? = null,
    val errores: Map<String, String> = emptyMap()
)

class SesionViewModel(
    private val autenticacionRepository: AutenticacionRepository,
    private val datosDemo: DatosDemo
) : ViewModel() {
    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { datosDemo.inicializar() }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(error = fallo.message ?: "No se pudieron preparar los datos demo.")
                    }
                }
            _uiState.update { it.copy(preparando = false) }
        }
    }

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (_uiState.value.preparando) return
        val errores = linkedMapOf<String, String>()
        if (usuario.isBlank()) {
            errores["usuario"] = "Ingresa tu usuario."
        }
        if (contrasena.isBlank()) {
            errores["contrasena"] = "Ingresa tu contraseña."
        } else if (!Validadores.claveValida(contrasena)) {
            errores["contrasena"] = "La contraseña debe tener al menos 4 caracteres."
        }
        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(preparando = false, error = null, errores = errores)
            }
            return
        }
        _uiState.update {
            it.copy(preparando = true, error = null, errores = emptyMap())
        }
        viewModelScope.launch {
            val resultado = autenticacionRepository.autenticar(usuario, contrasena)
            _uiState.value = SesionUiState(
                usuario = resultado.usuario,
                preparando = false,
                error = resultado.error
            )
        }
    }

    fun cerrarSesion() {
        _uiState.value = SesionUiState(preparando = false)
    }

    fun errorAtendido() {
        _uiState.update { it.copy(error = null, errores = emptyMap()) }
    }
}
