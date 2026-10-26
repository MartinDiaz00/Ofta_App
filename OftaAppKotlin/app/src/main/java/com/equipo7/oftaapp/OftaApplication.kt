package com.equipo7.oftaapp

import android.app.Application
import com.equipo7.oftaapp.data.local.AppDatabase

class OftaApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.obtener(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OftaApplication
            private set
    }
}
