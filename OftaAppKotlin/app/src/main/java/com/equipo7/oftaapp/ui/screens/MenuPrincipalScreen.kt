package com.equipo7.oftaapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo7.oftaapp.R
import com.equipo7.oftaapp.model.ExamenOftalmologico
import com.equipo7.oftaapp.model.Paciente
import com.equipo7.oftaapp.model.SeccionApp
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.ExamenCard
import com.equipo7.oftaapp.ui.components.IconosDeBarra
import com.equipo7.oftaapp.ui.components.OpcionCard
import com.equipo7.oftaapp.ui.components.PacienteCard
import com.equipo7.oftaapp.ui.components.ResumenCard
import com.equipo7.oftaapp.ui.theme.DivisorSuave
import com.equipo7.oftaapp.ui.theme.ErrorFormulario
import com.equipo7.oftaapp.ui.theme.FondoClaro
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.ui.theme.TextoSecundario
import com.equipo7.oftaapp.ui.theme.VerdeOfta
import com.equipo7.oftaapp.viewmodel.ExamenViewModel
import com.equipo7.oftaapp.viewmodel.MenuUiState
import com.equipo7.oftaapp.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipalScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    menuViewModel: MenuViewModel = viewModel(),
    examenesViewModel: ExamenViewModel = viewModel()
) {
    val menuState by menuViewModel.uiState.collectAsState()
    val examenesState by examenesViewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    var examenDetalle by remember { mutableStateOf<ExamenOftalmologico?>(null) }

    var estadoListado by remember { mutableStateOf<String?>(null) }

    var confirmarSalida by remember { mutableStateOf(false) }

    IconosDeBarra(fondoClaro = false)

    LaunchedEffect(Unit) {
        menuViewModel.seleccionar(SeccionApp.INICIO)
    }

    fun navegarA(seccion: SeccionApp) {
        examenDetalle = null
        estadoListado = null
        menuViewModel.seleccionar(seccion)
    }

    val contexto = LocalContext.current

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            containerColor = TarjetaBlanca,
            title = {
                Text(
                    text = "Salir de OftaApp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
            },
            text = {
                Text(
                    text = "¿Quieres salir de la aplicación?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarSalida = false
                        (contexto as? Activity)?.finish()
                    }
                ) {
                    Text(
                        text = "Salir",
                        fontWeight = FontWeight.Bold,
                        color = ErrorFormulario
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.Bold,
                        color = VerdeOfta
                    )
                }
            }
        )
    }

    BackHandler {
        when {
            confirmarSalida -> confirmarSalida = false

            drawerState.isOpen -> scope.launch { drawerState.close() }

            examenDetalle != null -> examenDetalle = null
            estadoListado != null -> estadoListado = null

            menuState.seccionActiva != SeccionApp.INICIO ->
                navegarA(SeccionApp.INICIO)

            else -> confirmarSalida = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = TarjetaBlanca) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de OftaApp",
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OftaApp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Menú Principal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SeccionApp.entries.forEach { seccion ->
                    NavigationDrawerItem(
                        label = { Text(seccion.titulo) },
                        selected = menuState.seccionActiva == seccion && examenDetalle == null,
                        onClick = {
                            navegarA(seccion)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DivisorSuave)
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${usuario.rol} · ${usuario.usuario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = FondoClaro,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                examenDetalle != null -> "Detalle de Examen"
                                estadoListado != null -> estadoListado.orEmpty() + "s"
                                menuState.seccionActiva == SeccionApp.INICIO ->
                                    "Hola, ${usuario.nombreCompleto}"

                                else -> menuState.seccionActiva.titulo
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        if (examenDetalle != null || estadoListado != null) {
                            IconButton(
                                onClick = {
                                    if (examenDetalle != null) examenDetalle = null
                                    else estadoListado = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Abrir menú lateral",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdeOfta,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->

            if (examenDetalle != null) {
                ContenidoDetalle(
                    examen = examenDetalle!!,
                    modifier = Modifier.padding(paddingValues)
                )
                return@Scaffold
            }

            if (estadoListado != null) {
                ContenidoPorEstado(
                    estado = estadoListado.orEmpty(),
                    examenes = examenesState.todos,
                    onExamenClick = { examenDetalle = it },
                    modifier = Modifier.padding(paddingValues)
                )
                return@Scaffold
            }

            when (menuState.seccionActiva) {
                SeccionApp.INICIO -> ContenidoInicio(
                    state = menuState,
                    onNavegar = { navegarA(it) },
                    onEstado = { estadoListado = it },
                    modifier = Modifier.padding(paddingValues)
                )

                SeccionApp.EXAMENES -> ListaExamenes(
                    state = examenesState,
                    onFiltroCambiado = examenesViewModel::onFiltroCambiado,
                    onExamenClick = { examenDetalle = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                )

                SeccionApp.PACIENTES -> ContenidoPacientes(
                    pacientes = menuState.pacientes,
                    modifier = Modifier.padding(paddingValues)
                )

                SeccionApp.HISTORIAL -> ContenidoHistorial(
                    pacientes = menuState.pacientes,
                    examenes = examenesState.todos,
                    modifier = Modifier.padding(paddingValues)
                )

                SeccionApp.REGISTRAR -> ContenidoRegistrar(
                    pacientes = menuState.pacientes,
                    onGuardar = examenesViewModel::registrarExamen,
                    onGuardado = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Examen registrado correctamente")
                        }
                        navegarA(SeccionApp.EXAMENES)
                    },
                    modifier = Modifier.padding(paddingValues)
                )

                SeccionApp.PERFIL -> ContenidoPerfil(
                    usuario = usuario,
                    onCerrarSesion = onCerrarSesion,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ContenidoInicio(
    state: MenuUiState,
    onNavegar: (SeccionApp) -> Unit,
    onEstado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        OpcionCard(
            titulo = "Ver Exámenes",
            subtitulo = "Listado general",
            onClick = { onNavegar(SeccionApp.EXAMENES) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        OpcionCard(
            titulo = "Buscar / Filtrar",
            subtitulo = "Por fecha, tipo o estado",
            onClick = { onNavegar(SeccionApp.EXAMENES) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        OpcionCard(
            titulo = "Historial",
            subtitulo = "Antecedentes del paciente",
            onClick = { onNavegar(SeccionApp.HISTORIAL) }
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Resumen de Actividad",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResumenCard(
                titulo = "Exámenes",
                valor = state.totalExamenes.toString(),
                onClick = { onNavegar(SeccionApp.EXAMENES) },
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                titulo = "Pacientes",
                valor = state.pacientes.size.toString(),
                onClick = { onNavegar(SeccionApp.PACIENTES) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResumenCard(
                titulo = "Completados",
                valor = state.examenesCompletados.toString(),
                onClick = { onEstado("Completado") },
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                titulo = "Pendientes",
                valor = state.examenesPendientes.toString(),
                onClick = { onEstado("Pendiente") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ContenidoPorEstado(
    estado: String,
    examenes: List<ExamenOftalmologico>,
    onExamenClick: (ExamenOftalmologico) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtrados = examenes.filter { it.estado == estado }

    if (filtrados.isEmpty()) {
        Text(
            text = "No hay exámenes con estado \"$estado\".",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(filtrados) { examen ->
                ExamenCard(
                    examen = examen,
                    modifier = Modifier.padding(vertical = 5.dp),
                    onClick = { onExamenClick(examen) }
                )
            }
        }
    }
}

@Composable
private fun ContenidoPacientes(
    pacientes: List<Paciente>,
    modifier: Modifier = Modifier
) {
    if (pacientes.isEmpty()) {
        Text(
            text = "No hay pacientes registrados.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            modifier = modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(pacientes) { paciente ->
                PacienteCard(
                    paciente = paciente,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun ContenidoPerfil(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        OpcionCard(
            titulo = usuario.nombreCompleto,
            subtitulo = usuario.rol,
            colorChevron = TextoSecundario
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DatoPerfil(etiqueta = "Cuenta", valor = usuario.usuario)
                HorizontalDivider(color = DivisorSuave, modifier = Modifier.padding(vertical = 8.dp))
                DatoPerfil(etiqueta = "Correo", valor = usuario.correo)
                HorizontalDivider(color = DivisorSuave, modifier = Modifier.padding(vertical = 8.dp))
                DatoPerfil(etiqueta = "Rol", valor = usuario.rol)
                HorizontalDivider(color = DivisorSuave, modifier = Modifier.padding(vertical = 8.dp))
                DatoPerfil(etiqueta = "Equipo", valor = "Equipo 07 - DuocUC")
                HorizontalDivider(color = DivisorSuave, modifier = Modifier.padding(vertical = 8.dp))
                DatoPerfil(etiqueta = "Aplicación", valor = "OftaApp v1.0")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, ErrorFormulario)
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = null,
                tint = ErrorFormulario,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar sesión",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ErrorFormulario
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = TextoSecundario
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = TextoPrincipal
        )
    }
}
