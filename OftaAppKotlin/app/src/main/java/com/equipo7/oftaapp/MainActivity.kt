package com.equipo7.oftaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equipo7.oftaapp.ui.OftaAppRoot
import com.equipo7.oftaapp.ui.theme.OftaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OftaAppTheme {
                OftaAppRoot()
            }
        }
    }
}
