package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExamenUiState(
    val examenes: List<ExamenOftalmologico> = emptyList(),

    val todos: List<ExamenOftalmologico> = emptyList(),
    val filtroBusqueda: String = ""
)

class ExamenViewModel : ViewModel() {
    private val repository = ExamenRepository()

    private val _uiState = MutableStateFlow(ExamenUiState())
    val uiState: StateFlow<ExamenUiState> = _uiState.asStateFlow()

    init {
        cargarExamenes()
    }

    fun cargarExamenes() {
        val completos = repository.obtenerExamenes()
        _uiState.value = _uiState.value.copy(
            examenes = completos,
            todos = completos
        )
    }

    fun onFiltroCambiado(nuevoTexto: String) {
        _uiState.value = _uiState.value.copy(
            filtroBusqueda = nuevoTexto,
            examenes = repository.buscarPorTipo(nuevoTexto)
        )
    }

    fun registrarExamen(datos: ExamenOftalmologico) {
        repository.agregarExamen(datos.copy(id = repository.siguienteId()))
        val completos = repository.obtenerExamenes()
        _uiState.value = ExamenUiState(examenes = completos, todos = completos)
    }
}
