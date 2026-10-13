package com.equipo7.oftaapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RemindNowWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {
    override suspend fun doWork(): Result {
        NotificationHelper.mostrar(
            applicationContext,
            inputData.getString("titulo") ?: "Ofta App",
            inputData.getString("mensaje") ?: "Tienes un recordatorio."
        )
        return Result.success()
    }
}