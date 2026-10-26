package com.equipo7.oftaapp.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo7.oftaapp.OftaApplication
import com.equipo7.oftaapp.model.EstadoExamen
import com.equipo7.oftaapp.model.Usuario
import com.equipo7.oftaapp.ui.components.AvisoSinConexion
import com.equipo7.oftaapp.ui.components.IconosDeBarra
import com.equipo7.oftaapp.ui.screens.DetalleExamenScreen
import com.equipo7.oftaapp.ui.screens.ExamenesScreen
import com.equipo7.oftaapp.ui.screens.FormularioExamenScreen
import com.equipo7.oftaapp.ui.screens.InicioScreen
import com.equipo7.oftaapp.ui.screens.PacienteDetalleScreen
import com.equipo7.oftaapp.ui.screens.PacientesScreen
import com.equipo7.oftaapp.ui.screens.PerfilScreen
import com.equipo7.oftaapp.ui.theme.FondoClaro
import com.equipo7.oftaapp.ui.theme.TarjetaBlanca
import com.equipo7.oftaapp.ui.theme.TextoPrincipal
import com.equipo7.oftaapp.viewmodel.DetalleExamenViewModel
import com.equipo7.oftaapp.viewmodel.ExamenesViewModel
import com.equipo7.oftaapp.viewmodel.FabricasViewModel
import com.equipo7.oftaapp.viewmodel.FormularioExamenViewModel
import com.equipo7.oftaapp.viewmodel.InicioViewModel
import com.equipo7.oftaapp.viewmodel.PacienteDetalleViewModel
import com.equipo7.oftaapp.viewmodel.PacientesViewModel
import kotlinx.coroutines.launch

private const val RUTA_EXAMENES = "${Rutas.EXAMENES}?estado={estado}"

private data class DestinoPrincipal(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OftaNavHost(
    app: OftaApplication,
    usuario: Usuario,
    textoGrande: Boolean,
    onCambiarTextoGrande: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    var dialogoPermisoNotificaciones by remember { mutableStateOf(false) }

    val launcherPermisoNotificaciones = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Sin permiso de notificaciones la app sigue funcionando, pero no avisará resultados."
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        val requiereDialogo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        dialogoPermisoNotificaciones = requiereDialogo
    }

    if (dialogoPermisoNotificaciones) {
        AlertDialog(
            onDismissRequest = { dialogoPermisoNotificaciones = false },
            title = { Text(text = "Activar notificaciones") },
            text = {
                Text(
                    text = "OftaApp puede avisarte cuando un examen quede validado o entregado. " +
                        "El aviso es local y no reemplaza al profesional de salud."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogoPermisoNotificaciones = false
                        launcherPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text(text = "Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoPermisoNotificaciones = false }) {
                    Text(text = "Ahora no")
                }
            }
        )
    }

    val inicioViewModel: InicioViewModel = viewModel(
        key = "inicio-${usuario.id}",
        factory = FabricasViewModel.inicio(app, usuario)
    )
    val examenesViewModel: ExamenesViewModel = viewModel(
        key = "examenes-${usuario.id}",
        factory = FabricasViewModel.examenes(app, usuario, null)
    )
    val pacientesViewModel: PacientesViewModel = viewModel(
        key = "pacientes-${usuario.id}",
        factory = FabricasViewModel.pacientes(app, usuario)
    )

    val inicioEstado by inicioViewModel.uiState.collectAsStateWithLifecycle()
    val examenesEstado by examenesViewModel.uiState.collectAsStateWithLifecycle()
    val pacientesEstado by pacientesViewModel.uiState.collectAsStateWithLifecycle()

    val mostrarMensaje: (String) -> Unit = { mensaje ->
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    val entry by navController.currentBackStackEntryAsState()
    val rutaActual = entry?.destination?.route
    val esPrincipal = rutaActual == Rutas.INICIO ||
        rutaActual == RUTA_EXAMENES ||
        rutaActual == Rutas.PACIENTES ||
        rutaActual == Rutas.PERFIL

    val destinos = buildList {
        add(DestinoPrincipal(Rutas.INICIO, "Inicio", Icons.Filled.Home))
        add(DestinoPrincipal(RUTA_EXAMENES, "Exámenes", Icons.Filled.Description))
        if (usuario.rol.puedeVerTodosLosPacientes()) {
            add(DestinoPrincipal(Rutas.PACIENTES, "Pacientes", Icons.Filled.People))
        }
        add(DestinoPrincipal(Rutas.PERFIL, "Perfil", Icons.Filled.Person))
    }

    val puedeCrearExamen = usuario.rol.puedeRegistrarExamen() &&
        (rutaActual == Rutas.INICIO || rutaActual == RUTA_EXAMENES)
    val enPacientes = rutaActual == Rutas.PACIENTES && usuario.rol.puedeGestionarPacientes()

    fun navegarPrincipal(destino: DestinoPrincipal) {
        if (destino.ruta == RUTA_EXAMENES) {
            examenesViewModel.setEstado(null)
        }
        navController.navigate(destino.ruta) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    IconosDeBarra(fondoClaro = true)

    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tituloDeRuta(rutaActual),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!esPrincipal) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoClaro,
                    titleContentColor = TextoPrincipal,
                    navigationIconContentColor = TextoPrincipal
                )
            )
        },
        bottomBar = {
            if (esPrincipal) {
                NavigationBar(containerColor = TarjetaBlanca) {
                    destinos.forEach { destino ->
                        val seleccionado = when (destino.ruta) {
                            Rutas.INICIO -> rutaActual == Rutas.INICIO
                            RUTA_EXAMENES -> rutaActual == RUTA_EXAMENES
                            else -> rutaActual == destino.ruta
                        }
                        NavigationBarItem(
                            selected = seleccionado,
                            onClick = { navegarPrincipal(destino) },
                            icon = {
                                Icon(
                                    imageVector = destino.icono,
                                    contentDescription = destino.etiqueta
                                )
                            },
                            label = { Text(destino.etiqueta) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (puedeCrearExamen) {
                FloatingActionButton(onClick = { navController.navigate(Rutas.NUEVO_EXAMEN) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Registrar nuevo examen"
                    )
                }
            } else if (enPacientes) {
                FloatingActionButton(onClick = { pacientesViewModel.abrirNuevo() }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Registrar nuevo paciente"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AvisoSinConexion()
            NavHost(
                navController = navController,
                startDestination = Rutas.INICIO,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Rutas.INICIO) {
                    InicioScreen(
                        uiState = inicioEstado,
                        usuario = usuario,
                        onExamenClick = { navController.navigate(Rutas.detalleExamen(it)) },
                        onVerTodos = {
                            examenesViewModel.setEstado(null)
                            navController.navigate(Rutas.EXAMENES)
                        },
                        onFiltrarEstado = { estado ->
                            examenesViewModel.setEstado(estado)
                            navController.navigate(if (estado == null) {
                                Rutas.EXAMENES
                            } else {
                                Rutas.examenes(estado)
                            })
                        }
                    )
                }

                composable(
                    route = RUTA_EXAMENES,
                    arguments = listOf(
                        navArgument("estado") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val estadoRuta = backStackEntry.arguments
                        ?.getString("estado")
                        ?.let { valor -> runCatching { EstadoExamen.valueOf(valor) }.getOrNull() }
                    LaunchedEffect(estadoRuta) {
                        if (estadoRuta != null) examenesViewModel.setEstado(estadoRuta)
                    }
                    ExamenesScreen(
                        uiState = examenesEstado,
                        onTexto = examenesViewModel::setTexto,
                        onEstado = examenesViewModel::setEstado,
                        onTipo = examenesViewModel::setTipo,
                        onSucursal = examenesViewModel::setSucursal,
                        onDesde = examenesViewModel::setDesde,
                        onHasta = examenesViewModel::setHasta,
                        onLimpiar = examenesViewModel::limpiar,
                        onExamenClick = { navController.navigate(Rutas.detalleExamen(it)) }
                    )
                }

                composable(Rutas.NUEVO_EXAMEN) {
                    val formularioViewModel: FormularioExamenViewModel = viewModel(
                        key = "formulario-nuevo-${usuario.id}",
                        factory = FabricasViewModel.formulario(app, usuario, "")
                    )
                    val formularioEstado by formularioViewModel.uiState.collectAsStateWithLifecycle()
                    FormularioExamenScreen(
                        uiState = formularioEstado,
                        usuario = usuario,
                        onPaciente = formularioViewModel::setPaciente,
                        onFecha = formularioViewModel::setFecha,
                        onHora = formularioViewModel::setHora,
                        onSucursal = formularioViewModel::setSucursal,
                        onProfesional = formularioViewModel::setProfesional,
                        onTipoExamen = formularioViewModel::setTipoExamen,
                        onOjoEvaluado = formularioViewModel::setOjoEvaluado,
                        onEstado = formularioViewModel::setEstado,
                        onObservaciones = formularioViewModel::setObservaciones,
                        onDocumentoSeleccionado = formularioViewModel::seleccionarDocumento,
                        onQuitarDocumento = formularioViewModel::quitarDocumentoSeleccionado,
                        onGuardar = formularioViewModel::guardar,
                        onCancelar = { navController.popBackStack() },
                        onGuardado = { id ->
                            mostrarMensaje("Examen registrado correctamente.")
                            navController.navigate(Rutas.detalleExamen(id)) {
                                popUpTo(Rutas.NUEVO_EXAMEN) { inclusive = true }
                            }
                        },
                        onIrPacientes = {
                            navController.navigate(Rutas.PACIENTES) {
                                popUpTo(Rutas.INICIO)
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = Rutas.EDITAR_EXAMEN,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val examenId = backStackEntry.arguments?.getString("id").orEmpty()
                    val formularioViewModel: FormularioExamenViewModel = viewModel(
                        key = "formulario-editar-$examenId",
                        factory = FabricasViewModel.formulario(app, usuario, examenId)
                    )
                    val formularioEstado by formularioViewModel.uiState.collectAsStateWithLifecycle()
                    FormularioExamenScreen(
                        uiState = formularioEstado,
                        usuario = usuario,
                        onPaciente = formularioViewModel::setPaciente,
                        onFecha = formularioViewModel::setFecha,
                        onHora = formularioViewModel::setHora,
                        onSucursal = formularioViewModel::setSucursal,
                        onProfesional = formularioViewModel::setProfesional,
                        onTipoExamen = formularioViewModel::setTipoExamen,
                        onOjoEvaluado = formularioViewModel::setOjoEvaluado,
                        onEstado = formularioViewModel::setEstado,
                        onObservaciones = formularioViewModel::setObservaciones,
                        onDocumentoSeleccionado = formularioViewModel::seleccionarDocumento,
                        onQuitarDocumento = formularioViewModel::quitarDocumentoSeleccionado,
                        onGuardar = formularioViewModel::guardar,
                        onCancelar = { navController.popBackStack() },
                        onGuardado = { id ->
                            mostrarMensaje("Examen actualizado correctamente.")
                            if (!navController.popBackStack()) {
                                navController.navigate(Rutas.detalleExamen(id))
                            }
                        },
                        onIrPacientes = {
                            navController.navigate(Rutas.PACIENTES) {
                                popUpTo(Rutas.INICIO)
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = Rutas.DETALLE_EXAMEN,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val examenId = backStackEntry.arguments?.getString("id").orEmpty()
                    val detalleViewModel: DetalleExamenViewModel = viewModel(
                        key = "detalle-$examenId",
                        factory = FabricasViewModel.detalle(app, usuario, examenId)
                    )
                    val detalleEstado by detalleViewModel.uiState.collectAsStateWithLifecycle()
                    DetalleExamenScreen(
                        uiState = detalleEstado,
                        usuario = usuario,
                        documentoRepository = app.documentoRepository,
                        onEditar = { navController.navigate(Rutas.editarExamen(it)) },
                        onCambiarEstado = detalleViewModel::cambiarEstado,
                        onEliminarDocumento = detalleViewModel::eliminarDocumento,
                        onMensaje = mostrarMensaje,
                        onMensajeAtendido = detalleViewModel::mensajeAtendido,
                        onErrorAtendido = detalleViewModel::errorAtendido
                    )
                }

                composable(Rutas.PACIENTES) {
                    PacientesScreen(
                        uiState = pacientesEstado,
                        usuario = usuario,
                        onBusqueda = pacientesViewModel::setBusqueda,
                        onNuevo = pacientesViewModel::abrirNuevo,
                        onEditar = pacientesViewModel::abrirEditar,
                        onAbrirFicha = { navController.navigate(Rutas.detallePaciente(it)) },
                        onActualizarEditor = pacientesViewModel::actualizarEditor,
                        onGuardar = pacientesViewModel::guardar,
                        onCerrarEditor = pacientesViewModel::cerrarEditor,
                        onMensaje = mostrarMensaje,
                        onMensajeAtendido = pacientesViewModel::mensajeAtendido
                    )
                }

                composable(
                    route = Rutas.DETALLE_PACIENTE,
                    arguments = listOf(navArgument("pacienteId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val pacienteId = backStackEntry.arguments?.getString("pacienteId").orEmpty()
                    val pacienteViewModel: PacienteDetalleViewModel = viewModel(
                        key = "paciente-$pacienteId",
                        factory = FabricasViewModel.pacienteDetalle(app, usuario, pacienteId)
                    )
                    val pacienteEstado by pacienteViewModel.uiState.collectAsStateWithLifecycle()
                    PacienteDetalleScreen(
                        uiState = pacienteEstado,
                        usuario = usuario,
                        onExamenClick = { navController.navigate(Rutas.detalleExamen(it)) },
                        onFiltrarTipo = pacienteViewModel::setTipoExamen,
                        onEditarPaciente = { paciente ->
                            pacientesViewModel.abrirEditar(paciente)
                            navController.navigate(Rutas.PACIENTES) {
                                popUpTo(Rutas.INICIO)
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Rutas.PERFIL) {
                    PerfilScreen(
                        usuario = usuario,
                        textoGrande = textoGrande,
                        onCambiarTextoGrande = onCambiarTextoGrande,
                        onCerrarSesion = onCerrarSesion
                    )
                }
            }
        }
    }
}

private fun tituloDeRuta(ruta: String?): String = when (ruta) {
    Rutas.INICIO -> "Inicio"
    RUTA_EXAMENES -> "Exámenes"
    Rutas.PACIENTES -> "Pacientes"
    Rutas.PERFIL -> "Mi perfil"
    Rutas.NUEVO_EXAMEN -> "Nuevo examen"
    Rutas.EDITAR_EXAMEN -> "Editar examen"
    Rutas.DETALLE_EXAMEN -> "Detalle del examen"
    Rutas.DETALLE_PACIENTE -> "Ficha del paciente"
    else -> "Ofta App"
}
