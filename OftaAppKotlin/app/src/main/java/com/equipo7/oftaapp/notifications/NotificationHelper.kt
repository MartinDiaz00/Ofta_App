package com.equipo7.oftaapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.equipo7.oftaapp.R

object NotificationHelper {
    const val CHANNEL_ID = "ofta_recordatorios"
    const val CHANNEL_CITAS = "ofta_citas"

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

            val canalCitas = NotificationChannel(
                CHANNEL_CITAS,
                "Citas y exámenes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Confirmación de exámenes agendados y recordatorios de cita"
                enableVibration(true)
            }
            manager?.createNotificationChannel(canalCitas)
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

    /**
     * Notificación estilo "confirmación de cita médica": canal de alta prioridad,
     * texto extendido con los datos del examen y acción para abrir la aplicación.
     */
    fun notificarExamenAgendado(
        contexto: Context,
        examenId: String,
        paciente: String,
        tipoExamen: String,
        fechaHora: String,
        sucursal: String,
        profesional: String
    ): Boolean {
        if (!puedeNotificar(contexto)) return false
        asegurarCanal(contexto)

        val intencion = Intent(contexto, com.equipo7.oftaapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("examenId", examenId)
        }
        val pendiente = PendingIntent.getActivity(
            contexto,
            examenId.hashCode(),
            intencion,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mensaje = buildString {
            appendLine("Paciente: $paciente")
            appendLine("Examen: $tipoExamen")
            appendLine("Fecha y hora: $fechaHora")
            appendLine("Sucursal: $sucursal")
            append("Profesional: $profesional")
        }
        val notificacion = NotificationCompat.Builder(contexto, CHANNEL_CITAS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Examen agendado correctamente")
            .setContentText("$tipoExamen · $fechaHora")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(mensaje)
                .setBigContentTitle("Confirmación de cita")
                .setSummaryText("OftaApp · datos ficticios"))
            .setContentIntent(pendiente)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(android.graphics.Color.rgb(21, 122, 99))
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setGroup("ofta_examenes")
            .setAutoCancel(true)
            .build()
        val manager = contexto.getSystemService(NotificationManager::class.java)
        manager?.notify(examenId.hashCode(), notificacion)
        return manager != null
    }
}