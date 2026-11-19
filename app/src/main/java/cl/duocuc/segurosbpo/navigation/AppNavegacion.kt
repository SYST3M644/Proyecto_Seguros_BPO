package cl.duocuc.segurosbpo.navigation

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.annotation.DrawableRes
import cl.duocuc.segurosbpo.R
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.duocuc.segurosbpo.ui.components.AccionesBarra
import cl.duocuc.segurosbpo.ui.components.LocalAccionesBarra
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.segurosbpo.ui.screens.LoginScreen
import cl.duocuc.segurosbpo.ui.screens.AsignacionCasosScreen
import cl.duocuc.segurosbpo.ui.screens.RecuperarContrasenaScreen
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.SiniestrosScreen
import cl.duocuc.segurosbpo.ui.screens.AvisosScreen
import cl.duocuc.segurosbpo.ui.screens.CargaEquiposScreen
import cl.duocuc.segurosbpo.ui.screens.CatalogoScreen
import cl.duocuc.segurosbpo.ui.screens.CrearCuentaScreen
import cl.duocuc.segurosbpo.ui.screens.LiquidadoresScreen
import cl.duocuc.segurosbpo.ui.screens.ConsultasScreen
import cl.duocuc.segurosbpo.ui.screens.EstadisticasAseguradoScreen
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DisponiblesLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.EstadisticasLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import cl.duocuc.segurosbpo.ui.screens.HistorialSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import cl.duocuc.segurosbpo.ui.screens.EditarPerfilScreen
import cl.duocuc.segurosbpo.ui.screens.PerfilCuentaScreen
import cl.duocuc.segurosbpo.data.VistaPreviaArchivo
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.comoAsegurado
import cl.duocuc.segurosbpo.ui.theme.PrimarioClaro
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel

fun idDesdeArgumentos(argumentos: Bundle?): String = argumentos?.getString("id").orEmpty()
@Composable
private fun BarraInferior(
    rol: RolCuenta,
    rutaActual: String?,
    cantidadAvisos: Int,
    cantidadDisponibles: Int,
    cantidadPorAsignar: Int,
    onNavegar: (String) -> Unit,
    onPerfil: () -> Unit,
    onVerSiniestros: () -> Unit,
    onCambiarUsuario: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    NavigationBar {
        when (rol) {
            RolCuenta.ASEGURADO -> {
                OpcionInferior(
                    texto = "Inicio",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.MisSiniestros.ruta,
                    onClick = { onNavegar(Ruta.MisSiniestros.ruta) }
                )
                OpcionInferior(
                    texto = "Reportar",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Reportar.ruta,
                    onClick = { onNavegar(Ruta.Reportar.ruta) }
                )
                OpcionInferior(
                    texto = "Estadísticas",
                    icono = R.drawable.ic_estadisticas,
                    seleccionada = rutaActual == Ruta.Estadisticas.ruta,
                    onClick = { onNavegar(Ruta.Estadisticas.ruta) }
                )
                OpcionInferior(
                    texto = "Avisos",
                    icono = R.drawable.ic_avisos,
                    seleccionada = rutaActual == Ruta.Avisos.ruta,
                    cantidad = cantidadAvisos,
                    onClick = { onNavegar(Ruta.Avisos.ruta) }
                )
            }
            RolCuenta.LIQUIDADOR -> {
                OpcionInferior(
                    texto = "Mis casos",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.Liquidador.ruta,
                    onClick = { onNavegar(Ruta.Liquidador.ruta) }
                )
                OpcionInferior(
                    texto = "Disponibles",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Disponibles.ruta,
                    cantidad = cantidadDisponibles,
                    onClick = { onNavegar(Ruta.Disponibles.ruta) }
                )
                OpcionInferior(
                    texto = "Avisos",
                    icono = R.drawable.ic_avisos,
                    seleccionada = rutaActual == Ruta.Avisos.ruta,
                    cantidad = cantidadAvisos,
                    onClick = { onNavegar(Ruta.Avisos.ruta) }
                )
                OpcionInferior(
                    texto = "Estadísticas",
                    icono = R.drawable.ic_estadisticas,
                    seleccionada = rutaActual == Ruta.EstadisticasLiquidador.ruta,
                    onClick = { onNavegar(Ruta.EstadisticasLiquidador.ruta) }
                )
            }
            RolCuenta.ANALISTA -> {
                OpcionInferior(
                    texto = "Por asignar",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.Analista.ruta,
                    cantidad = cantidadPorAsignar,
                    onClick = { onNavegar(Ruta.Analista.ruta) }
                )
                OpcionInferior(
                    texto = "Equipo",
                    icono = R.drawable.ic_persona,
                    seleccionada = rutaActual == Ruta.Liquidadores.ruta,
                    onClick = { onNavegar(Ruta.Liquidadores.ruta) }
                )
                OpcionInferior(
                    texto = "Catálogos",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Catalogo.ruta,
                    onClick = { onNavegar(Ruta.Catalogo.ruta) }
                )
                OpcionInferior(
                    texto = "Avisos",
                    icono = R.drawable.ic_avisos,
                    seleccionada = rutaActual == Ruta.Avisos.ruta,
                    cantidad = cantidadAvisos,
                    onClick = { onNavegar(Ruta.Avisos.ruta) }
                )
            }
        }
        NavigationBarItem(
            selected = false,
            onClick = { menuAbierto = true },
            icon = {
                Icon(painterResource(R.drawable.ic_menu), contentDescription = null)
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mi perfil") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_persona),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuAbierto = false
                            onPerfil()
                        }
                    )
                    if (rol == RolCuenta.ANALISTA) {
                        DropdownMenuItem(
                            text = { Text("Carga de equipos") },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.ic_estadisticas),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuAbierto = false
                                onNavegar(Ruta.CargaEquipos.ruta)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_cambiar_usuario),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuAbierto = false
                            onCambiarUsuario()
                        }
                    )
                }
            },
            label = { Text("Menú", maxLines = 1) }
        )
    }
}

@Composable
private fun RowScope.OpcionInferior(
    texto: String,
    @DrawableRes icono: Int,
    seleccionada: Boolean,
    cantidad: Int = 0,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionada,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = PrimarioClaro,
            unselectedIconColor = PrimarioClaro,
            unselectedTextColor = PrimarioClaro
        ),
        icon = {
            if (cantidad > 0) {
                BadgedBox(badge = { Badge { Text(cantidad.toString()) } }) {
                    Icon(painterResource(icono), contentDescription = null)
                }
            } else {
                Icon(painterResource(icono), contentDescription = null)
            }
        },
        label = { Text(texto, maxLines = 1) }
    )
}

fun volverDelDetalle(navController: NavController) {
    if (!navController.popBackStack()) {
        navController.navigate(Ruta.MisSiniestros.ruta) {
            launchSingleTop = true
        }
    }
}

@Composable
fun AppNavegacion(
    temaOscuro: Boolean = false,
    onAlternarTema: () -> Unit = {}
) {
    val cuentaVm: CuentaSesionViewModel = viewModel()
    val sesion = cuentaVm.cuenta.collectAsStateWithLifecycle().value
    val usuarios = cuentaVm.usuarios.collectAsStateWithLifecycle().value
    var pantallaSesion by rememberSaveable { mutableStateOf("login") }
    if (sesion == null) {
        CompositionLocalProvider(
            LocalAccionesBarra provides AccionesBarra(
                temaOscuro = temaOscuro,
                onAlternarTema = onAlternarTema
            )
        ) {
            when (pantallaSesion) {
                "crear" -> CrearCuentaScreen(
                    onCuentaCreada = { pantallaSesion = "login" },
                    onVolver = { pantallaSesion = "login" }
                )

                "recuperar" -> RecuperarContrasenaScreen(
                    onListo = { pantallaSesion = "login" },
                    onVolver = { pantallaSesion = "login" }
                )

                else -> LoginScreen(
                    onSesionIniciada = cuentaVm::iniciarSesionRemota,
                    onCrearCuenta = { pantallaSesion = "crear" },
                    onOlvideContrasena = { pantallaSesion = "recuperar" }
                )
            }
        }
        return
    }
    val navController = rememberNavController()
    val siniestrosVm: SiniestrosViewModel = viewModel()
    val casos by siniestrosVm.siniestros.collectAsStateWithLifecycle()
    val cargando by siniestrosVm.cargando.collectAsStateWithLifecycle()
    val avisos by siniestrosVm.avisos.collectAsStateWithLifecycle()
    val avisosSinLeer by siniestrosVm.sinLeer.collectAsStateWithLifecycle()
    val pendientes by siniestrosVm.pendientes.collectAsStateWithLifecycle()
    val consultas by siniestrosVm.consultas.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var busqueda by rememberSaveable { mutableStateOf("") }
    val solicitudes by siniestrosVm.solicitudes.collectAsStateWithLifecycle()
    val evidencias by siniestrosVm.evidencias.collectAsStateWithLifecycle()
    val gestiones by siniestrosVm.gestiones.collectAsStateWithLifecycle()
    val evaluacion by siniestrosVm.evaluacion.collectAsStateWithLifecycle()
    val disponibles by siniestrosVm.disponibles.collectAsStateWithLifecycle()
    val companias by siniestrosVm.companias.collectAsStateWithLifecycle()
    val tipos by siniestrosVm.tipos.collectAsStateWithLifecycle()
    val casosDelServidor = remember(casos, busqueda) {
        val texto = busqueda.trim().lowercase()
        if (texto.isEmpty()) casos
        else casos.filter { caso ->
            listOfNotNull(caso.numero, caso.tipoNombre, caso.comuna, caso.companiaNombre)
                .any { it.lowercase().contains(texto) }
        }
    }
    val carga by siniestrosVm.cargaLiquidadores.collectAsStateWithLifecycle()
    val cargaDelServidor = remember(carga) {
        carga.associate { it.liquidadorNombre to it.abiertos }
    }

    val usuarioId = remember(sesion.id) {
        sesion.id.removePrefix("API_")
    }

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotBlank()) siniestrosVm.cargarAvisos(usuarioId)
        if (sesion.rol == RolCuenta.ANALISTA) siniestrosVm.cargarPendientes()
    }

    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    val inicio = when (sesion.rol) {
        RolCuenta.ASEGURADO -> Ruta.MisSiniestros.ruta
        RolCuenta.LIQUIDADOR -> Ruta.Liquidador.ruta
        RolCuenta.ANALISTA -> Ruta.Analista.ruta
    }
    CompositionLocalProvider(
        LocalAccionesBarra provides AccionesBarra(
            temaOscuro = temaOscuro,
            onAlternarTema = onAlternarTema,
            onVolver = { navController.navigateUp() },
            puedeVolver = rutaActual != null && rutaActual != inicio
        )
    ) {
        Column(Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = inicio
            ) {
                composable(Ruta.Perfil.ruta) {
                    PerfilCuentaScreen(
                        cuenta = sesion,
                        onGuardarFoto = cuentaVm::subirFotoAlServidor,
                        onEditar = { navController.navigate(Ruta.EditarPerfil.ruta) },
                        onVolver = { navController.navigateUp() },
                        onCerrarSesion = cuentaVm::cerrarSesion
                    )
                }
                composable(Ruta.Siniestros.ruta) {
                    SiniestrosScreen(
                        usuarioId = usuarioId,
                        rol = sesion.rol,
                        viewModel = siniestrosVm
                    )
                }
                composable(Ruta.Avisos.ruta) {
                    AvisosScreen(
                        avisos = avisos,
                        onCargar = {
                            siniestrosVm.cargarAvisos(usuarioId)
                            if (sesion.rol == RolCuenta.ANALISTA) {
                                siniestrosVm.cargarPendientes()
                            }
                        },
                        onMarcarLeido = { siniestrosVm.marcarAvisoLeido(it, usuarioId) },
                        onAbrirCaso = {
                            when (sesion.rol) {
                                RolCuenta.ASEGURADO ->
                                    navController.navigate(Ruta.Detalle.crear(it))
                                RolCuenta.LIQUIDADOR ->
                                    navController.navigate(Ruta.Liquidador.ruta)
                                RolCuenta.ANALISTA ->
                                    navController.navigate(Ruta.Analista.ruta)
                            }
                        },
                        onAdjuntar = {
                            if (sesion.rol == RolCuenta.ASEGURADO) {
                                navController.navigate(Ruta.Evidencias.crear(it))
                            } else {
                                navController.navigate(Ruta.Liquidador.ruta)
                            }
                        },
                        onVolver = { navController.navigateUp() },
                        cargando = cargando,
                        pendientes = pendientes,
                        onAprobar = { siniestrosVm.aprobarAsegurado(it) },
                        onRechazar = { siniestrosVm.rechazarAsegurado(it) }
                    )
                }
                composable(Ruta.EditarPerfil.ruta) {
                    EditarPerfilScreen(
                        cuenta = sesion,
                        onGuardar = { nombre, rut, correo, telefono, direccion, listo ->
                            cuentaVm.actualizarContacto(correo, telefono)
                            cuentaVm.guardarPerfilEnServidor(
                                nombre = nombre,
                                correo = correo,
                                rut = rut,
                                direccion = direccion,
                                onListo = listo
                            )
                        },
                        onCambiarContrasena = cuentaVm::cambiarContrasena,
                        onVolver = { navController.navigateUp() }
                    )
                }

                if (sesion.rol == RolCuenta.ANALISTA) {
                    composable(Ruta.Analista.ruta) {
                        AsignacionCasosScreen(viewModel = siniestrosVm)
                    }
                    composable(Ruta.CargaEquipos.ruta) {
                        CargaEquiposScreen(
                            carga = carga,
                            pendientes = casos,
                            actualizadoEn = "",
                            onCargar = {
                                siniestrosVm.cargarCarga()
                                siniestrosVm.cargarSinAsignar()
                            }
                        )
                    }
                    composable(Ruta.Liquidadores.ruta) {
                        LaunchedEffect(Unit) { siniestrosVm.cargarCarga() }
                        LiquidadoresScreen(
                            liquidadores = usuarios.filter {
                                it.rol == RolCuenta.LIQUIDADOR
                            },
                            carga = cargaDelServidor,
                            onCrear = cuentaVm::crearLiquidadorEnServidor
                        )
                    }
                    composable(Ruta.Catalogo.ruta) {
                        CatalogoScreen(
                            companias = companias,
                            tipos = tipos,
                            onCrearCompania = { nombre, plazo ->
                                siniestrosVm.crearCompania(nombre, plazo)
                            },
                            onCrearTipo = { nombre, esVehiculo, documentos ->
                                siniestrosVm.crearTipo(nombre, esVehiculo, documentos)
                            }
                        )
                    }
                }

                if (sesion.rol == RolCuenta.ASEGURADO) {
                    composable(Ruta.MisSiniestros.ruta) {
                        LaunchedEffect(usuarioId) {
                            if (usuarioId.isNotBlank()) siniestrosVm.cargarDeAsegurado(usuarioId)
                        }
                        MisSiniestrosScreen(
                            nombreAsegurado = sesion.nombre,
                            rutAsegurado = sesion.rut,
                            fotoCuenta = sesion.foto,
                            avatarUrl = sesion.avatarUrl,
                            actualizadoEn = "",
                            siniestros = casosDelServidor,
                            busqueda = busqueda,
                            onBuscar = { busqueda = it },
                            onVerDetalle = { navController.navigate(Ruta.Detalle.crear(it)) },
                            onNuevoReporte = { navController.navigate(Ruta.Reportar.ruta) },
                            cargando = cargando,
                            onActualizar = { siniestrosVm.cargarDeAsegurado(usuarioId) }
                        )
                    }
                    composable(Ruta.Estadisticas.ruta) {
                        EstadisticasAseguradoScreen(
                            siniestros = casos,
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    composable(Ruta.Reportar.ruta) {
                        FormularioScreen(
                            aseguradoId = usuarioId,
                            viewModel = siniestrosVm,
                            onRegistrado = {
                                navController.navigate(Ruta.Siniestros.ruta) {
                                    popUpTo(Ruta.MisSiniestros.ruta)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(
                        route = Ruta.Detalle.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        val caso = casos.firstOrNull { it.id == id }
                        DetalleSiniestroScreen(
                            siniestro = caso,
                            solicitudes = solicitudes,
                            consultasPendientes = consultas.count {
                                it.respuesta.isNullOrBlank()
                            },
                            evaluacion = evaluacion,
                            documentosDelTipo = tipos
                                .firstOrNull { it.codigo == caso?.tipoCodigo }
                                ?.documentos.orEmpty(),
                            onCargarSolicitudes = {
                                siniestrosVm.cargarSolicitudes(id)
                                siniestrosVm.cargarConsultas(id)
                                siniestrosVm.cargarEvaluacion(id)
                            },
                            onVolver = { volverDelDetalle(navController) },
                            onEditar = { navController.navigate(Ruta.Editar.crear(id)) },
                            onEvidencias = { navController.navigate(Ruta.Evidencias.crear(id)) },
                            onHistorial = { navController.navigate(Ruta.Historial.crear(id)) },
                            onConsultas = { navController.navigate(Ruta.Consultas.crear(id)) },
                            onDesistir = { motivo ->
                                siniestrosVm.desistir(id, motivo, usuarioId)
                            },
                            onGuardarEvaluacion = { nota, comentario ->
                                siniestrosVm.evaluar(id, nota, comentario)
                            }
                        )
                    }
                    composable(
                        route = Ruta.Consultas.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        ConsultasScreen(
                            siniestro = casos.firstOrNull { it.id == id },
                            consultas = consultas,
                            puedeResponder = sesion.rol == RolCuenta.LIQUIDADOR,
                            onCargar = { siniestrosVm.cargarConsultas(id) },
                            onEnviar = { pregunta -> siniestrosVm.preguntar(id, pregunta) },
                            onResponder = { consultaId, texto ->
                                siniestrosVm.responderConsulta(consultaId, id, texto)
                            },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    composable(
                        route = Ruta.Historial.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        HistorialSiniestroScreen(
                            siniestro = casos.firstOrNull { it.id == id },
                            gestiones = gestiones,
                            onCargar = { siniestrosVm.cargarGestiones(id) },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    composable(
                        route = Ruta.Evidencias.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        val caso = casos.firstOrNull { it.id == id }
                        EvidenciasScreen(
                            siniestro = caso,
                            documentosDelTipo = tipos
                                .firstOrNull { it.codigo == caso?.tipoCodigo }
                                ?.documentos.orEmpty(),
                            evidencias = evidencias,
                            solicitudes = solicitudes,
                            onVolver = { navController.navigateUp() },
                            onCargar = {
                                siniestrosVm.cargarEvidencias(id)
                                siniestrosVm.cargarSolicitudes(id)
                            },
                            onCargarImagen = { evidenciaId ->
                                siniestrosVm.vistaPreviaEvidencia(
                                    evidenciaId, VistaPreviaArchivo.LADO_COMPLETO
                                )
                            },
                            onCargarMiniatura = { evidenciaId ->
                                siniestrosVm.vistaPreviaEvidencia(
                                    evidenciaId, VistaPreviaArchivo.LADO_MINIATURA
                                )
                            },
                            onGuardarFoto = { tipo, bitmap ->
                                siniestrosVm.subirFoto(id, tipo, sesion.nombre, bitmap)
                                null
                            },
                            onGuardarArchivo = { tipo, uri ->
                                siniestrosVm.subirArchivo(contexto, id, tipo, sesion.nombre, uri)
                                null
                            },
                            onEliminar = { evidenciaId ->
                                siniestrosVm.eliminarEvidencia(id, evidenciaId)
                            }
                        )
                    }
                    composable(
                        route = Ruta.Editar.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        EditarSiniestroScreen(
                            siniestro = casos.firstOrNull { it.id == id },
                            onVolver = { navController.navigateUp() },
                            onGuardar = { descripcion ->
                                siniestrosVm.actualizarDescripcion(id, descripcion, usuarioId)
                                navController.popBackStack(Ruta.MisSiniestros.ruta, false)
                            }
                        )
                    }
                }

                if (sesion.rol == RolCuenta.LIQUIDADOR) {
                    composable(Ruta.Liquidador.ruta) {
                        CasosLiquidadorScreen(
                            liquidadorId = usuarioId,
                            responsable = sesion.nombre,
                            viewModel = siniestrosVm
                        )
                    }
                    composable(Ruta.Disponibles.ruta) {
                        LaunchedEffect(usuarioId) {
                            if (usuarioId.isNotBlank()) siniestrosVm.cargarDisponibles(usuarioId)
                        }
                        DisponiblesLiquidadorScreen(
                            equipo = sesion.equipo.orEmpty(),
                            disponibles = disponibles,
                            onTomarCaso = { casoId -> siniestrosVm.tomarCaso(casoId, usuarioId) },
                            cargando = cargando,
                            onActualizar = { siniestrosVm.cargarDisponibles(usuarioId) }
                        )
                    }
                    composable(Ruta.EstadisticasLiquidador.ruta) {
                        EstadisticasLiquidadorScreen(
                            equipo = sesion.equipo.orEmpty(),
                            responsable = sesion.nombre,
                            casos = casos,
                            disponibles = disponibles,
                            seguimientosVencidos = emptySet()
                        )
                    }
                }
            }
            BarraInferior(
                rol = sesion.rol,
                rutaActual = rutaActual,
                cantidadAvisos = avisosSinLeer.toInt() + pendientes.size,
                cantidadDisponibles = disponibles.size,
                cantidadPorAsignar = casos.size,
                onNavegar = { ruta ->
                    navController.navigate(ruta) {
                        popUpTo(inicio)
                        launchSingleTop = true
                    }
                },
                onPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onVerSiniestros = { navController.navigate(Ruta.Siniestros.ruta) },
                onCambiarUsuario = cuentaVm::cerrarSesion
            )
        }
    }
}
