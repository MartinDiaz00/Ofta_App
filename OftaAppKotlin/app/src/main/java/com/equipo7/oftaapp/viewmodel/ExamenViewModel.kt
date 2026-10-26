package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExamenUiState(
    val examenes: List<ExamenOftalmologico> = emptyList(),
    val todos: List<ExamenOftalmologico> = emptyList(),
    val filtroBusqueda: String = "",
    val filtroEstado: String = "Todos",
    val cargando: Boolean = false,
    val error: String? = null
)

class ExamenViewModel : ViewModel() {
    private val repository = ExamenRepository()
    private val _uiState = MutableStateFlow(ExamenUiState(cargando = true))
    val uiState: StateFlow<ExamenUiState> = _uiState.asStateFlow()

    init { cargarExamenes() }

    fun cargarExamenes() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        try {
            repository.inicializarDatosDemo()
            val completos = repository.obtenerExamenes()
            _uiState.value = _uiState.value.copy(
                examenes = filtrar(completos, _uiState.value.filtroBusqueda, _uiState.value.filtroEstado),
                todos = completos,
                cargando = false,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(cargando = false, error = e.message ?: "No se pudieron cargar los exámenes.")
        }
    }

    fun onFiltroCambiado(nuevoTexto: String) {
        val todos = _uiState.value.todos
        _uiState.value = _uiState.value.copy(
            filtroBusqueda = nuevoTexto,
            examenes = filtrar(todos, nuevoTexto, _uiState.value.filtroEstado)
        )
    }

    fun onFiltroEstadoCambiado(nuevoEstado: String) {
        _uiState.value = _uiState.value.copy(
            filtroEstado = nuevoEstado,
            examenes = filtrar(_uiState.value.todos, _uiState.value.filtroBusqueda, nuevoEstado)
        )
    }

    fun registrarExamen(datos: ExamenOftalmologico, rol: Rol, onResult: (Boolean, String?) -> Unit) = viewModelScope.launch {
        if (!rol.puedeRegistrarExamen()) {
            onResult(false, "Tu rol no tiene permiso para registrar exámenes.")
            return@launch
        }
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        try {
            repository.inicializarDatosDemo()
            repository.agregarExamen(datos.copy(id = repository.siguienteId()))
            val completos = repository.obtenerExamenes()
            _uiState.value = _uiState.value.copy(
                examenes = filtrar(completos, _uiState.value.filtroBusqueda, _uiState.value.filtroEstado),
                todos = completos,
                cargando = false,
                error = null
            )
            onResult(true, null)
        } catch (e: Exception) {
            val mensaje = e.message ?: "No se pudo guardar el examen. Inténtalo nuevamente."
            _uiState.value = _uiState.value.copy(cargando = false, error = mensaje)
            onResult(false, mensaje)
        }
    }

    private fun filtrar(
        lista: List<ExamenOftalmologico>,
        texto: String,
        estado: String
    ): List<ExamenOftalmologico> = lista.filter { examen ->
        val coincideTexto = texto.isBlank() ||
            examen.tipoExamen.contains(texto, ignoreCase = true) ||
            examen.pacienteNombre.contains(texto, ignoreCase = true) ||
            examen.estado.contains(texto, ignoreCase = true) ||
            examen.fecha.contains(texto, ignoreCase = true) ||
            examen.sucursal.contains(texto, ignoreCase = true)
        val coincideEstado = estado == "Todos" || examen.estado.equals(estado, ignoreCase = true)
        coincideTexto && coincideEstado
    }
}
