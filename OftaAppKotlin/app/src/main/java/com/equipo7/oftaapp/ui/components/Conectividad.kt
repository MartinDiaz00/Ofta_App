package com.equipo7.oftaapp.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import com.equipo7.oftaapp.ui.theme.EstadoPendiente
import com.equipo7.oftaapp.ui.theme.TextoPrincipal

@Composable
fun AvisoSinConexion(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var conectado by remember { mutableStateOf(estaConectado(context)) }

    DisposableEffect(context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val handler = Handler(Looper.getMainLooper())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handler.post { conectado = true }
            }

            override fun onLost(network: Network) {
                handler.post { conectado = estaConectado(context) }
            }

            override fun onUnavailable() {
                handler.post { conectado = false }
            }
        }
        manager?.registerDefaultNetworkCallback(callback)
        onDispose {
            runCatching { manager?.unregisterNetworkCallback(callback) }
        }
    }

    if (!conectado) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(EstadoPendiente.copy(alpha = 0.14f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = null,
                tint = EstadoPendiente
            )
            Text(
                text = "Sin conexión. La app funciona con los datos locales guardados.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextoPrincipal,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun estaConectado(context: Context): Boolean {
    val manager = context.getSystemService(ConnectivityManager::class.java) ?: return true
    val red = manager.activeNetwork ?: return false
    val capacidades = manager.getNetworkCapabilities(red) ?: return false
    return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
