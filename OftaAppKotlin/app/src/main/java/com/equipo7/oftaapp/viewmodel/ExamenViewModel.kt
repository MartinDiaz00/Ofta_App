package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExamenUiState(
    val examenes: List<ExamenOftalmologico> = emptyList(),
    val filtroBusqueda: String = ""
)

class ExamenViewModel(
    private val repository: ExamenRepository = ExamenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamenUiState())
    val uiState: StateFlow<ExamenUiState> = _uiState.asStateFlow()

    init {
        cargarExamenes()
    }

    fun cargarExamenes() {
        val lista = repository.obtenerExamenes()
        _uiState.value = _uiState.value.copy(examenes = lista)
    }

    fun onFiltroCambiado(nuevoTexto: String) {
        _uiState.value = _uiState.value.copy(filtroBusqueda = nuevoTexto)
        val resultados = repository.buscarPorTipo(nuevoTexto)
        _uiState.value = _uiState.value.copy(examenes = resultados)
    }
}