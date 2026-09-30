package com.equipo7.oftaapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.viewmodel.ExamenUiState
import com.equipo7.oftaapp.viewmodel.ExamenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamenesScreen(
    examenesViewModel: ExamenViewModel = viewModel()
) {
    val state by examenesViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OftaApp - Exámenes Registrados") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeOfta,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->
        ListaExamenes(
            state = state,
            onFiltroCambiado = examenesViewModel::onFiltroCambiado,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }
}

@Composable
fun ListaExamenes(
    state: ExamenUiState,
    onFiltroCambiado: (String) -> Unit,
    modifier: Modifier = Modifier,
    onExamenClick: (ExamenOftalmologico) -> Unit = {}
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.filtroBusqueda,
            onValueChange = onFiltroCambiado,
            label = { Text("Filtrar por examen o paciente") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TarjetaBlanca,
                unfocusedContainerColor = TarjetaBlanca,
                focusedBorderColor = VerdeOfta,
                unfocusedBorderColor = DivisorSuave,
                focusedTextColor = TextoPrincipal,
                unfocusedTextColor = TextoPrincipal,
                focusedLabelColor = VerdeOfta,
                unfocusedLabelColor = TextoSecundario,
                cursorColor = VerdeOfta
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.examenes.isEmpty()) {
            Text(
                text = "No hay exámenes que coincidan con la búsqueda.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(state.examenes) { examen ->
                    ExamenCard(
                        examen = examen,
                        modifier = Modifier.padding(bottom = 8.dp),
                        onClick = { onExamenClick(examen) }
                    )
                }
            }
        }
    }
}
