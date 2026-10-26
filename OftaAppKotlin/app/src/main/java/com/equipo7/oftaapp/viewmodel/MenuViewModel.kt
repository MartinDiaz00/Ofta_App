package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.SeccionApp
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MenuUiState(
    val seccionActiva: SeccionApp = SeccionApp.INICIO,
    val totalExamenes: Int = 0,
    val examenesCompletados: Int = 0,
    val examenesPendientes: Int = 0,
    val pacientes: List<Paciente> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class MenuViewModel : ViewModel() {
    private val repository = ExamenRepository()
    private val _uiState = MutableStateFlow(MenuUiState(cargando = true))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init { refrescarResumen() }

    fun refrescarResumen() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        try {
            repository.inicializarDatosDemo()
            val examenes = repository.obtenerExamenes()
            _uiState.value = _uiState.value.copy(
                totalExamenes = examenes.size,
                examenesCompletados = examenes.count { it.estado.equals("Completado", true) },
                examenesPendientes = examenes.count { it.estado.equals("Pendiente", true) },
                pacientes = repository.obtenerPacientes(),
                cargando = false,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(cargando = false, error = e.message ?: "No se pudo cargar el resumen.")
        }
    }

    fun seleccionar(nueva: SeccionApp) {
        _uiState.value = _uiState.value.copy(seccionActiva = nueva)
        if (nueva == SeccionApp.INICIO || nueva == SeccionApp.PACIENTES) refrescarResumen()
    }
}
