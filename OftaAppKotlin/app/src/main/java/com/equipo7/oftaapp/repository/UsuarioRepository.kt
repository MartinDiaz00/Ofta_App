package com.equipo7.oftaapp.repository

import com.equipo7.oftaapp.data.local.AppDatabase
import com.equipo7.oftaapp.data.local.aDominio
import com.equipo7.oftaapp.model.Rol
import com.equipo7.oftaapp.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsuarioRepository(private val database: AppDatabase) {
    fun observarPorRol(rol: Rol): Flow<List<Usuario>> =
        database.usuarioDao().observarPorRol(rol.name).map { usuarios ->
            usuarios.map { it.aDominio() }
        }

    suspend fun listar(): List<Usuario> =
        database.usuarioDao().listar().map { it.aDominio() }
}
