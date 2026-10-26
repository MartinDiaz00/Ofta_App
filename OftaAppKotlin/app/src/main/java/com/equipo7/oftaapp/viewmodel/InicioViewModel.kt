package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.ExamenRepository
import com.equipo7.oftaapp.repository.PacienteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InicioUiState(
    val cargando: Boolean = true,
    val examenes: List<ExamenOftalmologico> = emptyList(),
    val totalExamenes: Int = 0,
    val pacientes: Int = 0,
    val pendientes: Int = 0,
    val validados: Int = 0,
    val observados: Int = 0,
    val entregados: Int = 0,
    val error: String? = null
)

class InicioViewModel(
    examenRepository: ExamenRepository,
    pacienteRepository: PacienteRepository,
    private val usuario: Usuario
) : ViewModel() {
    val uiState = combine(
        examenRepository.observarTodos(usuario),
        pacienteRepository.observarTodos()
    ) { examenes, pacientes ->
        InicioUiState(
            cargando = false,
            examenes = examenes.take(5),
            totalExamenes = examenes.size,
            pacientes = if (usuario.rol == Rol.PACIENTE) 1 else pacientes.size,
            pendientes = examenes.count { it.estado == EstadoExamen.PENDIENTE },
            validados = examenes.count { it.estado == EstadoExamen.VALIDADO },
            observados = examenes.count { it.estado == EstadoExamen.OBSERVADO },
            entregados = examenes.count { it.estado == EstadoExamen.ENTREGADO }
        )
    }
        .catch {
            emit(InicioUiState(cargando = false, error = "No se pudieron cargar los datos."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InicioUiState()
        )
}
