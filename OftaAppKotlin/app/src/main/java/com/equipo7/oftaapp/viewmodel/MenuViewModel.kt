package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.SeccionApp
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MenuUiState(
    val seccionActiva: SeccionApp = SeccionApp.INICIO,
    val totalExamenes: Int = 0,
    val examenesCompletados: Int = 0,
    val examenesPendientes: Int = 0,
    val pacientes: List<Paciente> = emptyList()
)

class MenuViewModel : ViewModel() {
    private val repository = ExamenRepository()

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        refrescarResumen()
    }

    fun refrescarResumen() {
        val examenes = repository.obtenerExamenes()

        _uiState.value = _uiState.value.copy(
            totalExamenes = examenes.size,
            examenesCompletados = examenes.count { it.estado == "Completado" },
            examenesPendientes = examenes.count { it.estado == "Pendiente" },
            pacientes = repository.obtenerPacientes()
        )
    }

    fun seleccionar(nueva: SeccionApp) {
        if (nueva == SeccionApp.INICIO) refrescarResumen()
        _uiState.value = _uiState.value.copy(seccionActiva = nueva)
    }
}
