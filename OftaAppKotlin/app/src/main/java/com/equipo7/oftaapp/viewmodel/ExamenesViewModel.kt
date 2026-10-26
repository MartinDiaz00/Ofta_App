package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.FiltroExamenes
import com.equipo7.oftaapp.model.FiltrosExamen
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.ExamenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ExamenesUiState(
    val cargando: Boolean = true,
    val examenes: List<ExamenOftalmologico> = emptyList(),
    val total: Int = 0,
    val filtros: FiltrosExamen = FiltrosExamen(),
    val error: String? = null
)

class ExamenesViewModel(
    examenRepository: ExamenRepository,
    usuario: Usuario,
    estadoInicial: EstadoExamen? = null
) : ViewModel() {
    private val filtros = MutableStateFlow(FiltrosExamen(estado = estadoInicial))
    private val error = MutableStateFlow<String?>(null)

    val uiState = combine(
        examenRepository.observarTodos(usuario),
        filtros,
        error
    ) { examenes, filtrosActuales, errorActual ->
        ExamenesUiState(
            cargando = false,
            examenes = FiltroExamenes.aplicar(examenes, filtrosActuales),
            total = examenes.size,
            filtros = filtrosActuales,
            error = errorActual
        )
    }
        .catch {
            emit(ExamenesUiState(cargando = false, error = "No se pudieron cargar los exámenes."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExamenesUiState(filtros = FiltrosExamen(estado = estadoInicial))
        )

    fun setTexto(valor: String) {
        filtros.update { it.copy(texto = valor) }
    }

    fun setEstado(valor: EstadoExamen?) {
        filtros.update { it.copy(estado = valor) }
    }

    fun setTipo(valor: String?) {
        filtros.update { it.copy(tipoExamen = valor) }
    }

    fun setSucursal(valor: String?) {
        filtros.update { it.copy(sucursal = valor) }
    }

    fun setDesde(fechaIso: String?) {
        filtros.update { it.copy(desde = fechaIso) }
    }

    fun setHasta(fechaIso: String?) {
        filtros.update { it.copy(hasta = fechaIso) }
    }

    fun limpiar() {
        filtros.value = FiltrosExamen()
        error.value = null
    }
}
