package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.ExamenRepository
import com.equipo7.oftaapp.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PacienteDetalleUiState(
    val cargando: Boolean = true,
    val paciente: Paciente? = null,
    val examenes: List<ExamenOftalmologico> = emptyList(),
    val tiposDisponibles: List<String> = emptyList(),
    val tipoSeleccionado: String? = null,
    val totalExamenes: Int = 0,
    val error: String? = null
)

class PacienteDetalleViewModel(
    pacienteRepository: PacienteRepository,
    examenRepository: ExamenRepository,
    usuario: Usuario,
    private val pacienteId: String
) : ViewModel() {
    private val tipoSeleccionado = MutableStateFlow<String?>(null)

    val uiState = combine(
        pacienteRepository.observarPorId(pacienteId),
        examenRepository.observarTodos(usuario),
        tipoSeleccionado
    ) { paciente, examenes, tipo ->
        val delPaciente = examenes.filter { it.pacienteId == pacienteId }
        val tipos = delPaciente.map { it.tipoExamen }.distinct().sorted()
        val filtrados = tipo?.let { elegido ->
            delPaciente.filter { it.tipoExamen == elegido }
        } ?: delPaciente
        PacienteDetalleUiState(
            cargando = false,
            paciente = paciente,
            examenes = filtrados,
            tiposDisponibles = tipos,
            tipoSeleccionado = tipo,
            totalExamenes = delPaciente.size,
            error = if (paciente == null) "Paciente no encontrado." else null
        )
    }
        .catch {
            emit(PacienteDetalleUiState(cargando = false, error = "No se pudo cargar la ficha."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PacienteDetalleUiState()
        )

    fun setTipoExamen(tipo: String?) {
        tipoSeleccionado.value = tipo
    }
}
