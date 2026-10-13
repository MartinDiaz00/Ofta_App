package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private fun cargarExamenes() = viewModelScope.launch {
        val completos = repository.obtenerExamenes()
        _uiState.value = _uiState.value.copy(
            examenes = completos,
            todos = completos
        )
    }

    fun onFiltroCambiado(nuevoTexto: String) = viewModelScope.launch {
        val resultados = repository.buscarPorTipo(nuevoTexto)
        _uiState.value = _uiState.value.copy(
            filtroBusqueda = nuevoTexto,
            examenes = resultados
        )
    }

    fun registrarExamen(datos: ExamenOftalmologico) = viewModelScope.launch {
        repository.agregarExamen(datos.copy(id = repository.siguienteId()))
        val completos = repository.obtenerExamenes()
        _uiState.value = ExamenUiState(examenes = completos, todos = completos)
    }
}
