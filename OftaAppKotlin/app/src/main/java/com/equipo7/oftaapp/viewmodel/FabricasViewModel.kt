package com.equipo7.oftaapp.viewmodel

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.equipo7.oftaapp.OftaApplication
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Usuario

object FabricasViewModel {
    fun sesion(app: OftaApplication) = viewModelFactory {
        initializer {
            SesionViewModel(app.autenticacionRepository, app.datosDemo)
        }
    }

    fun inicio(app: OftaApplication, usuario: Usuario) = viewModelFactory {
        initializer {
            InicioViewModel(app.examenRepository, app.pacienteRepository, usuario)
        }
    }

    fun examenes(
        app: OftaApplication,
        usuario: Usuario,
        estadoInicial: EstadoExamen?
    ) = viewModelFactory {
        initializer {
            ExamenesViewModel(app.examenRepository, usuario, estadoInicial)
        }
    }

    fun pacientes(app: OftaApplication, usuario: Usuario) = viewModelFactory {
        initializer {
            PacientesViewModel(app.pacienteRepository, usuario)
        }
    }

    fun pacienteDetalle(
        app: OftaApplication,
        usuario: Usuario,
        pacienteId: String
    ) = viewModelFactory {
        initializer {
            PacienteDetalleViewModel(
                app.pacienteRepository,
                app.examenRepository,
                usuario,
                pacienteId
            )
        }
    }

    fun formulario(
        app: OftaApplication,
        usuario: Usuario,
        examenId: String
    ) = viewModelFactory {
        initializer {
            FormularioExamenViewModel(
                app.examenRepository,
                app.pacienteRepository,
                app.usuarioRepository,
                app.documentoRepository,
                usuario,
                examenId
            )
        }
    }

    fun detalle(
        app: OftaApplication,
        usuario: Usuario,
        examenId: String
    ) = viewModelFactory {
        initializer {
            DetalleExamenViewModel(app.examenRepository, usuario, examenId)
        }
    }
}
