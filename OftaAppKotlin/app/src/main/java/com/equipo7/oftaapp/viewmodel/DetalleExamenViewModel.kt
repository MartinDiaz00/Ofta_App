package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.DocumentoAdjunto
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleExamenUiState(
    val cargando: Boolean = true,
    val examen: ExamenOftalmologico? = null,
    val documentos: List<DocumentoAdjunto> = emptyList(),
    val procesando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class DetalleExamenViewModel(
    private val examenRepository: ExamenRepository,
    private val usuario: Usuario,
    private val examenId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleExamenUiState())
    val uiState: StateFlow<DetalleExamenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            examenRepository.observarPorId(examenId, usuario)
                .catch {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudo cargar el examen.")
                    }
                }
                .collect { examen ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            examen = examen,
                            error = if (examen == null) "Examen no encontrado." else it.error
                        )
                    }
                }
        }
        viewModelScope.launch {
            examenRepository.observarDocumentos(examenId)
                .catch { }
                .collect { documentos ->
                    _uiState.update { it.copy(documentos = documentos) }
                }
        }
    }

    fun cambiarEstado(nuevoEstado: EstadoExamen, observaciones: String = "") {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            examenRepository.cambiarEstado(examenId, nuevoEstado, observaciones, usuario)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            mensaje = "Estado actualizado a ${nuevoEstado.etiqueta}."
                        )
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            error = fallo.message ?: "No se pudo actualizar el estado."
                        )
                    }
                }
        }
    }

    fun eliminarDocumento(documento: DocumentoAdjunto) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            examenRepository.eliminarDocumento(documento.id, usuario)
                .onSuccess {
                    _uiState.update {
                        it.copy(procesando = false, mensaje = "Documento eliminado.")
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            error = fallo.message ?: "No se pudo eliminar el documento."
                        )
                    }
                }
        }
    }

    fun mensajeAtendido() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun errorAtendido() {
        _uiState.update { it.copy(error = null) }
    }
}
