package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.PacienteRepository
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorPaciente(
    val id: String = "",
    val rut: String = "",
    val nombreCompleto: String = "",
    val fechaNacimiento: String = "",
    val correo: String = "",
    val telefonoPrincipal: String = "",
    val telefonoSecundario: String = "",
    val prevision: String = "Fonasa",
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null
)

data class PacientesUiState(
    val cargando: Boolean = true,
    val pacientes: List<Paciente> = emptyList(),
    val busqueda: String = "",
    val editor: EditorPaciente? = null,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class PacientesViewModel(
    private val pacienteRepository: PacienteRepository,
    private val usuario: Usuario
) : ViewModel() {
    private val _uiState = MutableStateFlow(PacientesUiState())
    val uiState: StateFlow<PacientesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pacienteRepository.observarTodos()
                .catch {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudieron cargar los pacientes.")
                    }
                }
                .collect { pacientes ->
                    _uiState.update { it.copy(cargando = false, pacientes = pacientes) }
                }
        }
    }

    fun setBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun abrirNuevo() {
        if (!usuario.rol.puedeGestionarPacientes()) return
        _uiState.update { it.copy(editor = EditorPaciente()) }
    }

    fun abrirEditar(paciente: Paciente) {
        if (!usuario.rol.puedeGestionarPacientes()) return
        _uiState.update {
            it.copy(
                editor = EditorPaciente(
                    id = paciente.id,
                    rut = paciente.rut,
                    nombreCompleto = paciente.nombreCompleto,
                    fechaNacimiento = Fechas.aVisible(paciente.fechaNacimiento),
                    correo = paciente.correo,
                    telefonoPrincipal = paciente.telefonoPrincipal,
                    telefonoSecundario = paciente.telefonoSecundario.orEmpty(),
                    prevision = paciente.prevision
                )
            )
        }
    }

    fun cerrarEditor() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(editor = null) }
    }

    fun actualizarEditor(transformacion: (EditorPaciente) -> EditorPaciente) {
        _uiState.update { estado ->
            val editor = estado.editor ?: return@update estado
            estado.copy(editor = transformacion(editor).copy(errores = emptyMap(), errorGeneral = null))
        }
    }

    fun guardar() {
        if (!usuario.rol.puedeGestionarPacientes()) return
        val estado = _uiState.value
        val editor = estado.editor ?: return
        if (estado.guardando) return

        val errores = linkedMapOf<String, String>()
        val rut = Validadores.rutFormateado(editor.rut)
        val fechaIso = Fechas.aIso(editor.fechaNacimiento)
        val correo = editor.correo.trim()
        val telefono = editor.telefonoPrincipal.trim()
        val telefonoSecundario = editor.telefonoSecundario.trim()

        if (!Validadores.rutValido(rut)) errores["rut"] = "RUT chileno inválido."
        if (!Validadores.nombreValido(editor.nombreCompleto)) {
            errores["nombreCompleto"] = "Ingresa un nombre de 3 a 80 caracteres."
        }
        if (fechaIso == null || Fechas.fechaFutura(fechaIso) || Fechas.edad(fechaIso) > 120) {
            errores["fechaNacimiento"] = "Selecciona una fecha de nacimiento válida."
        }
        if (correo.isNotBlank() && !Validadores.correoValido(correo)) {
            errores["correo"] = "El correo no es válido."
        }
        if (!Validadores.telefonoValido(telefono)) {
            errores["telefonoPrincipal"] = "Ingresa un celular chileno válido."
        }
        if (telefonoSecundario.isNotBlank() && !Validadores.telefonoValido(telefonoSecundario)) {
            errores["telefonoSecundario"] = "El teléfono secundario no es válido."
        }
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(editor = editor.copy(errores = errores)) }
            return
        }

        _uiState.update { it.copy(guardando = true, editor = editor.copy(errores = emptyMap())) }
        viewModelScope.launch {
            val paciente = Paciente(
                id = editor.id,
                rut = rut,
                nombreCompleto = editor.nombreCompleto.trim(),
                fechaNacimiento = fechaIso.orEmpty(),
                correo = correo,
                telefonoPrincipal = telefono,
                telefonoSecundario = telefonoSecundario.ifBlank { null },
                prevision = editor.prevision
            )
            pacienteRepository.guardar(paciente)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            editor = null,
                            mensaje = "Paciente guardado correctamente."
                        )
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            editor = editor.copy(
                                errorGeneral = fallo.message ?: "No se pudo guardar el paciente."
                            )
                        )
                    }
                }
        }
    }

    fun mensajeAtendido() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
