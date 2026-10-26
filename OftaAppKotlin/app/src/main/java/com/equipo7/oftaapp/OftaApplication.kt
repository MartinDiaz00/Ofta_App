package com.equipo7.oftaapp

import android.app.Application
import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.repository.AutenticacionRepository
import com.equipo7.oftaapp.repository.DatosDemo
import com.equipo7.oftaapp.repository.DocumentoRepository
import com.equipo7.oftaapp.repository.ExamenRepository
import com.equipo7.oftaapp.repository.PacienteRepository
import com.equipo7.oftaapp.repository.PreferenciasRepository
import com.equipo7.oftaapp.repository.UsuarioRepository

class OftaApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.obtener(this) }
    val documentoRepository: DocumentoRepository by lazy { DocumentoRepository(this) }
    val autenticacionRepository: AutenticacionRepository by lazy {
        AutenticacionRepository(database)
    }
    val pacienteRepository: PacienteRepository by lazy { PacienteRepository(database) }
    val usuarioRepository: UsuarioRepository by lazy { UsuarioRepository(database) }
    val examenRepository: ExamenRepository by lazy {
        ExamenRepository(database, documentoRepository, this)
    }
    val preferenciasRepository: PreferenciasRepository by lazy { PreferenciasRepository(this) }
    val datosDemo: DatosDemo by lazy { DatosDemo(database, this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OftaApplication
            private set
    }
}
