package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.viewmodel.ExamenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamenesScreen(
    viewModel: ExamenViewModel = ExamenViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OftaApp - Exámenes Registrados") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.filtroBusqueda,
                onValueChange = { viewModel.onFiltroCambiado(it) },
                label = { Text("Filtrar por examen o paciente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.examenes) { examen ->
                    ExamenCard(examen = examen)
                }
            }
        }
    }
}