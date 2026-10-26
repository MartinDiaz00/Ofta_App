package com.equipo7.oftaapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.equipo7.oftaapp.data.local.dao.DocumentoDao
import com.equipo7.oftaapp.data.local.dao.ExamenDao
import com.equipo7.oftaapp.data.local.dao.PacienteDao
import com.equipo7.oftaapp.data.local.dao.UsuarioDao
import com.equipo7.oftaapp.data.local.entity.DocumentoExamenEntity
import com.equipo7.oftaapp.data.local.entity.ExamenEntity
import com.equipo7.oftaapp.data.local.entity.PacienteEntity
import com.equipo7.oftaapp.data.local.entity.UsuarioEntity

@Database(
    entities = [PacienteEntity::class, UsuarioEntity::class, ExamenEntity::class, DocumentoExamenEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun examenDao(): ExamenDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun obtener(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ofta_app.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
