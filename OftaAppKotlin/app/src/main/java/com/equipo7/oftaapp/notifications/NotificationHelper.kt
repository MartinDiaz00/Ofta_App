package com.equipo7.oftaapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.equipo7.oftaapp.R

object NotificationHelper {
    const val CHANNEL_ID = "ofta_recordatorios"

    fun asegurarCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios Ofta",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de citas y exámenes"
            }
            val manager = contexto.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }
    }

    fun puedeNotificar(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    fun mostrar(contexto: Context, titulo: String, mensaje: String): Boolean {
        if (!puedeNotificar(contexto)) return false
        asegurarCanal(contexto)
        val notificacion = NotificationCompat.Builder(contexto, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setAutoCancel(true)
            .build()
        val manager = contexto.getSystemService(NotificationManager::class.java)
        manager?.notify(System.currentTimeMillis().toInt(), notificacion)
        return manager != null
    }
}