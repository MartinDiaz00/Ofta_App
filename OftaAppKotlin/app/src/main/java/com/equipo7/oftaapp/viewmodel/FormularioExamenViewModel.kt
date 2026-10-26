package com.equipo7.oftaapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo7.oftaapp.model.Catalogos
import com.equipo7.oftaapp.model.DocumentoAdjunto
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.ReglasExamen
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.repository.DocumentoRepository
import com.equipo7.oftaapp.repository.DocumentoSeleccionado
import com.equipo7.oftaapp.repository.EntradaExamen
import com.equipo7.oftaapp.repository.ExamenRepository
import com.equipo7.oftaapp.repository.PacienteRepository
import com.equipo7.oftaapp.repository.UsuarioRepository
import com.equipo7.oftaapp.util.Fechas
import com.equipo7.oftaapp.util.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioExamenUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val preparandoDocumento: Boolean = false,
    val modoEdicion: Boolean = false,
    val pacientes: List<Paciente> = emptyList(),
    val pacienteId: String = "",
    val fecha: String = Fechas.aVisible(Fechas.hoyIso()),
    val hora: String = Fechas.ahoraHora(),
    val sucursal: String = Catalogos.sucursales.first(),
    val profesionales: List<String> = emptyList(),
    val profesional: String = "",
    val tipoExamen: String = Catalogos.tiposExamen.first(),
    val ojoEvaluado: String = Catalogos.ojos.first(),
    val estado: EstadoExamen = EstadoExamen.PENDIENTE,
    val estadoOriginal: EstadoExamen? = null,
    val estadosDisponibles: List<EstadoExamen> = listOf(EstadoExamen.PENDIENTE),
    val observaciones: String = "",
    val documentoSeleccionado: DocumentoSeleccionado? = null,
    val documentoExistente: DocumentoAdjunto? = null,
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardadoId: String? = null,
    val accesoDenegado: Boolean = false
)

class FormularioExamenViewModel(
    private val examenRepository: ExamenRepository,
    pacienteRepository: PacienteRepository,
    usuarioRepository: UsuarioRepository,
    private val documentoRepository: DocumentoRepository,
    val usuario: Usuario,
    private val examenId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        FormularioExamenUiState(
            estadosDisponibles = ReglasExamen.estadosPermitidos(usuario.rol)
        )
    )
    val uiState: StateFlow<FormularioExamenUiState> = _uiState.asStateFlow()

    private var pacientesCargados = false
    private var examenCargado = examenId.isBlank()

    init {
        if (examenId.isBlank() && !usuario.rol.puedeRegistrarExamen()) {
            _uiState.update {
                it.copy(
                    cargando = false,
                    accesoDenegado = true,
                    errorGeneral = "Tu rol no puede registrar exámenes."
                )
            }
        } else {
            cargarPacientes(pacienteRepository)
            cargarProfesionales(usuarioRepository)
            if (examenId.isNotBlank()) cargarExamen()
            actualizarCargando()
        }
    }

    private fun cargarProfesionales(usuarioRepository: UsuarioRepository) {
        viewModelScope.launch {
            usuarioRepository.observarPorRol(Rol.OFTALMOLOGO)
                .catch {
                    _uiState.update {
                        it.copy(errorGeneral = "No se pudo cargar la lista de profesionales.")
                    }
                }
                .collect { usuarios ->
                    val nombres = usuarios.map { it.nombreCompleto }
                    _uiState.update { estado ->
                        val seleccionado = when {
                            estado.profesional.isNotBlank() -> estado.profesional
                            usuario.rol == Rol.OFTALMOLOGO &&
                                usuario.nombreCompleto in nombres -> usuario.nombreCompleto
                            else -> nombres.firstOrNull().orEmpty()
                        }
                        estado.copy(profesionales = nombres, profesional = seleccionado)
                    }
                }
        }
    }

    private fun cargarPacientes(pacienteRepository: PacienteRepository) {
        viewModelScope.launch {
            pacienteRepository.observarTodos()
                .catch {
                    _uiState.update {
                        it.copy(errorGeneral = "No se pudieron cargar los pacientes.")
                    }
                }
                .collect { pacientes ->
                    pacientesCargados = true
                    _uiState.update {
                        it.copy(
                            pacientes = pacientes,
                            pacienteId = it.pacienteId.ifBlank {
                                pacientes.firstOrNull()?.id.orEmpty()
                            }
                        )
                    }
                    actualizarCargando()
                }
        }
    }

    private fun cargarExamen() {
        viewModelScope.launch {
            val examen = examenRepository.obtenerPorId(examenId)
            if (examen == null) {
                _uiState.update {
                    it.copy(
                        accesoDenegado = true,
                        errorGeneral = "El examen que intentas editar no existe."
                    )
                }
            } else if (!usuario.rol.puedeEditarExamen(examen.estado)) {
                _uiState.update {
                    it.copy(
                        accesoDenegado = true,
                        errorGeneral = "Tu rol no puede editar este examen."
                    )
                }
            } else {
                val documentos = examenRepository.obtenerDocumentos(examenId)
                val correccionTens = usuario.rol == Rol.TENS &&
                    examen.estado == EstadoExamen.OBSERVADO
                _uiState.update {
                    it.copy(
                        modoEdicion = true,
                        pacienteId = examen.pacienteId,
                        fecha = examen.fechaVisible,
                        hora = examen.hora,
                        sucursal = examen.sucursal,
                        profesional = examen.profesional,
                        tipoExamen = examen.tipoExamen,
                        ojoEvaluado = examen.ojoEvaluado,
                        estado = if (correccionTens) EstadoExamen.PENDIENTE else examen.estado,
                        estadoOriginal = examen.estado,
                        observaciones = examen.observaciones,
                        documentoExistente = documentos.firstOrNull(),
                        estadosDisponibles = ReglasExamen.estadosPermitidos(usuario.rol)
                    )
                }
            }
            examenCargado = true
            actualizarCargando()
        }
    }

    private fun actualizarCargando() {
        _uiState.update {
            if (it.accesoDenegado) it.copy(cargando = false)
            else it.copy(cargando = !(pacientesCargados && examenCargado))
        }
    }

    fun setPaciente(valor: String) = actualizar { it.copy(pacienteId = valor) }

    fun setFecha(valor: String) = actualizar { it.copy(fecha = valor) }

    fun setHora(valor: String) = actualizar { it.copy(hora = valor) }

    fun setSucursal(valor: String) = actualizar { it.copy(sucursal = valor) }

    fun setProfesional(valor: String) = actualizar { it.copy(profesional = valor) }

    fun setTipoExamen(valor: String) = actualizar { it.copy(tipoExamen = valor) }

    fun setOjoEvaluado(valor: String) = actualizar { it.copy(ojoEvaluado = valor) }

    fun setEstado(valor: EstadoExamen) {
        if (valor in _uiState.value.estadosDisponibles) {
            actualizar { it.copy(estado = valor) }
        }
    }

    fun setObservaciones(valor: String) =
        actualizar { it.copy(observaciones = valor.take(500)) }

    fun seleccionarDocumento(uri: Uri) {
        _uiState.update { it.copy(preparandoDocumento = true, errorGeneral = null) }
        viewModelScope.launch {
            documentoRepository.prepararDocumento(uri)
                .onSuccess { documento ->
                    _uiState.update {
                        it.copy(
                            preparandoDocumento = false,
                            documentoSeleccionado = documento,
                            errores = it.errores - "documento"
                        )
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            preparandoDocumento = false,
                            errores = it.errores + (
                                "documento" to (
                                    fallo.message ?: "No se pudo leer el documento."
                                    )
                                )
                        )
                    }
                }
        }
    }

    fun quitarDocumentoSeleccionado() {
        _uiState.update { it.copy(documentoSeleccionado = null) }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.cargando || estado.guardando || estado.accesoDenegado) return
        val errores = linkedMapOf<String, String>()
        val fechaIso = Fechas.aIso(estado.fecha)
        if (estado.pacienteId.isBlank()) errores["paciente"] = "Selecciona un paciente."
        if (fechaIso == null || Fechas.fechaFutura(fechaIso)) {
            errores["fecha"] = "Selecciona una fecha válida que no sea futura."
        }
        if (!Validadores.horaValida(estado.hora)) errores["hora"] = "Selecciona una hora válida."
        if (!Validadores.textoValido(estado.tipoExamen)) {
            errores["tipoExamen"] = "Selecciona el tipo de examen."
        }
        if (!Validadores.textoValido(estado.sucursal)) errores["sucursal"] = "Selecciona la sucursal."
        if (!Validadores.textoValido(estado.profesional)) {
            errores["profesional"] = "Selecciona el profesional."
        }
        if (!Validadores.textoValido(estado.ojoEvaluado)) {
            errores["ojoEvaluado"] = "Selecciona el ojo evaluado."
        }
        if (!Validadores.observacionValida(estado.observaciones)) {
            errores["observaciones"] = "Máximo 500 caracteres."
        }
        if (estado.estado == EstadoExamen.OBSERVADO && estado.observaciones.isBlank()) {
            errores["observaciones"] = "Describe el motivo de la observación."
        }
        val requiereDocumento = estado.estado == EstadoExamen.VALIDADO ||
            estado.estado == EstadoExamen.ENTREGADO
        val tieneDocumento = estado.documentoSeleccionado != null ||
            estado.documentoExistente != null
        if (requiereDocumento && !tieneDocumento) {
            errores["documento"] = "Adjunta un documento antes de validar o entregar."
        }
        val errorEstado = ReglasExamen.validarEstado(
            estado = estado.estado,
            rol = usuario.rol,
            observaciones = estado.observaciones,
            tieneDocumento = tieneDocumento
        )
        if (errorEstado != null) errores["estado"] = errorEstado

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(guardando = true, errores = emptyMap(), errorGeneral = null) }
        viewModelScope.launch {
            val entrada = EntradaExamen(
                id = examenId,
                pacienteId = estado.pacienteId,
                tipoExamen = estado.tipoExamen,
                fecha = fechaIso.orEmpty(),
                hora = estado.hora,
                sucursal = estado.sucursal,
                profesional = estado.profesional,
                ojoEvaluado = estado.ojoEvaluado,
                estado = estado.estado,
                observaciones = estado.observaciones
            )
            examenRepository.guardar(entrada, estado.documentoSeleccionado, usuario)
                .onSuccess { idGuardado ->
                    _uiState.update { it.copy(guardando = false, guardadoId = idGuardado) }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorGeneral = fallo.message ?: "No se pudo guardar el examen."
                        )
                    }
                }
        }
    }

    fun errorAtendido() {
        _uiState.update { it.copy(errorGeneral = null) }
    }

    private fun actualizar(transformacion: (FormularioExamenUiState) -> FormularioExamenUiState) {
        _uiState.update {
            transformacion(it).copy(errores = emptyMap(), errorGeneral = null)
        }
    }
}
