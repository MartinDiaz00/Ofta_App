package com.equipo7.oftaapp.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
fun IconosDeBarra(fondoClaro: Boolean) {
    val context = LocalContext.current

    SideEffect {
        val activity = context.actividad() ?: return@SideEffect
        val controlador = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controlador.isAppearanceLightStatusBars = fondoClaro
        controlador.isAppearanceLightNavigationBars = true
    }
}

private fun Context.actividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.actividad()
    else -> null
}
