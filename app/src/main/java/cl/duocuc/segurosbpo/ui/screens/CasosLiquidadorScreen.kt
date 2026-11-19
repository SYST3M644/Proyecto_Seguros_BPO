package cl.duocuc.segurosbpo.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.data.VistaPreviaArchivo
import cl.duocuc.segurosbpo.model.OrdenSiniestros
import cl.duocuc.segurosbpo.model.OrdenSiniestros.CERRADO
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.ActualizarDeslizando
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.BuscadorCasos
import cl.duocuc.segurosbpo.ui.components.CampoDesplegable
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.components.EtiquetaInteres
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel
import kotlinx.coroutines.launch

private val RESULTADOS = listOf("Aprobado", "Aprobado con descuento", "Rechazado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasosLiquidadorScreen(
    liquidadorId: String,
    responsable: String,
    viewModel: SiniestrosViewModel
) {
    val casos by viewModel.siniestros.collectAsStateWithLifecycle()
    val gestiones by viewModel.gestiones.collectAsStateWithLifecycle()
    val solicitudes by viewModel.solicitudes.collectAsStateWithLifecycle()
    val evidencias by viewModel.evidencias.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val mensaje by viewModel.aviso.collectAsStateWithLifecycle()
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var busqueda by rememberSaveable { mutableStateOf("") }
    var verCerrados by rememberSaveable { mutableStateOf(false) }
    var abierto by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var visibleAsegurado by rememberSaveable { mutableStateOf(true) }
    var resultado by rememberSaveable { mutableStateOf(RESULTADOS.first()) }
    var mensajeCierre by rememberSaveable { mutableStateOf("") }
    var documentoPedido by rememberSaveable { mutableStateOf("") }
    var imagenVista by remember { mutableStateOf<Bitmap?>(null) }

    val contexto = LocalContext.current
    val abrirArchivo: (String) -> Unit = { url ->
        runCatching {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }.onFailure {
            scope.launch { aviso.showSnackbar("No se pudo abrir el documento") }
        }
    }

    LaunchedEffect(liquidadorId) {
        if (liquidadorId.isNotBlank()) viewModel.cargarDeLiquidador(liquidadorId)
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            aviso.showSnackbar(it)
            viewModel.limpiarAviso()
        }
    }

    val visibles = remember(casos, busqueda) {
        val texto = busqueda.trim().lowercase()
        val encontrados = if (texto.isEmpty()) casos
        else casos.filter { caso ->
            listOfNotNull(caso.numero, caso.tipoNombre, caso.comuna, caso.aseguradoNombre)
                .any { it.lowercase().contains(texto) }
        }
        OrdenSiniestros.paraLiquidador(encontrados)
    }

    val abiertos = remember(visibles) { visibles.filter { it.etapa != CERRADO } }
    val cerrados = remember(visibles) { visibles.filter { it.etapa == CERRADO } }
    val mostrados = if (verCerrados) cerrados else abiertos

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_casos_liquidador)) }
    ) { margen ->
        ActualizarDeslizando(
            cargando = cargando,
            onActualizar = { viewModel.cargarDeLiquidador(liquidadorId) },
            modifier = Modifier.fillMaxSize().padding(margen).imePadding()
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                if (cargando) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }

                error?.let { texto ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            Text(texto, color = MaterialTheme.colorScheme.error)
                            BotonSecundario(
                                texto = stringResource(R.string.btn_reintentar),
                                onClick = {
                                    viewModel.limpiarError()
                                    viewModel.cargarDeLiquidador(liquidadorId)
                                }
                            )
                        }
                    }
                }

                BuscadorCasos(busqueda) { busqueda = it }

                if (visibles.isNotEmpty()) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !verCerrados,
                            onClick = {
                                verCerrados = false
                                abierto = ""
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(stringResource(R.string.tab_casos_abiertos, abiertos.size))
                        }
                        SegmentedButton(
                            selected = verCerrados,
                            onClick = {
                                verCerrados = true
                                abierto = ""
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(stringResource(R.string.tab_casos_cerrados, cerrados.size))
                        }
                    }
                }

                if (!cargando && error == null && mostrados.isEmpty()) {
                    Text(
                        stringResource(
                            when {
                                visibles.isEmpty() -> R.string.vacio_casos_liquidador
                                verCerrados -> R.string.vacio_casos_cerrados
                                else -> R.string.vacio_casos_abiertos
                            }
                        )
                    )
                }

                mostrados.forEach { caso ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            CodigoSiniestro(caso.numero)
                            Text(caso.tipoNombre ?: caso.tipoCodigo)
                            Text(
                                caso.aseguradoNombre.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                caso.comuna + " · " + caso.fechaOcurrencia,
                                style = MaterialTheme.typography.bodySmall
                            )
                            EtiquetaInteres(texto = etiquetaDeEtapa(caso.etapa))

                            BotonSecundario(
                                texto = when {
                                    abierto == caso.id ->
                                        stringResource(R.string.btn_cerrar_gestion)
                                    caso.etapa == CERRADO ->
                                        stringResource(R.string.btn_ver_archivado)
                                    else -> stringResource(R.string.btn_gestionar_caso)
                                },
                                onClick = {
                                    abierto = if (abierto == caso.id) "" else caso.id
                                    descripcion = ""
                                    mensajeCierre = ""
                                    if (abierto.isNotBlank()) {
                                        viewModel.cargarGestiones(caso.id)
                                        viewModel.cargarSolicitudes(caso.id)
                                        viewModel.cargarEvidencias(caso.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (abierto == caso.id) {
                                HorizontalDivider()

                                Text(
                                    stringResource(R.string.etiqueta_historial),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (gestiones.isEmpty()) {
                                    Text(
                                        stringResource(R.string.vacio_historial),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                gestiones.forEach { gestion ->
                                    Text(
                                        gestion.fecha + " · " + gestion.responsable,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        gestion.descripcion,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                HorizontalDivider()

                                if (caso.etapa != CERRADO) {
                                    OutlinedTextField(
                                        value = descripcion,
                                        onValueChange = { descripcion = it },
                                        label = {
                                            Text(stringResource(R.string.campo_gestion))
                                        },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = visibleAsegurado,
                                            onCheckedChange = { visibleAsegurado = it }
                                        )
                                        Text(stringResource(R.string.opcion_visible_asegurado))
                                    }
                                    BotonSecundario(
                                        texto = stringResource(R.string.btn_registrar_gestion),
                                        onClick = {
                                            viewModel.agregarGestion(
                                                caso.id, responsable, descripcion, visibleAsegurado
                                            )
                                            descripcion = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                HorizontalDivider()

                                Text(
                                    stringResource(R.string.etiqueta_evidencias_recibidas),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (evidencias.isEmpty()) {
                                    Text(
                                        stringResource(R.string.vacio_evidencias),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                evidencias.forEach { evidencia ->
                                    FilaDocumento(
                                        tipo = evidencia.tipoDocumento,
                                        nombre = evidencia.nombreArchivo + " · " +
                                            (evidencia.tamanoBytes / 1024) + " KB",
                                        detalle = evidencia.fechaRegistro + " · " +
                                            evidencia.subidoPor,
                                        esImagen = previsualizable(evidencia.mime),
                                        cargarMiniatura = {
                                            viewModel.vistaPreviaEvidencia(
                                                evidencia.id,
                                                VistaPreviaArchivo.LADO_MINIATURA
                                            )
                                        },
                                        onAbrir = {
                                            scope.launch {
                                                val vista = viewModel.vistaPreviaEvidencia(
                                                    evidencia.id,
                                                    VistaPreviaArchivo.LADO_COMPLETO
                                                )
                                                if (vista == null) {
                                                    abrirArchivo(evidencia.archivoUrl)
                                                } else {
                                                    imagenVista = vista
                                                }
                                            }
                                        },
                                        onDescargar = { abrirArchivo(evidencia.archivoUrl) },
                                        onEliminar = null
                                    )
                                }

                                HorizontalDivider()

                                Text(
                                    stringResource(R.string.etiqueta_documentos_pedidos),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                solicitudes.forEach { solicitud ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(Dimens.espacioChico)
                                    ) {
                                        Text(
                                            solicitud.tipoDocumento + " · " + solicitud.estado,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (solicitud.estado == "PENDIENTE") {
                                            BotonSecundario(
                                                texto = stringResource(R.string.btn_anular),
                                                onClick = {
                                                    viewModel.anularSolicitud(
                                                        solicitud.id, caso.id
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                                if (caso.etapa != CERRADO) {
                                    OutlinedTextField(
                                        value = documentoPedido,
                                        onValueChange = { documentoPedido = it },
                                        label = {
                                            Text(stringResource(R.string.campo_documento_pedido))
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    BotonSecundario(
                                        texto = stringResource(R.string.btn_pedir_documento),
                                        onClick = {
                                            viewModel.solicitarDocumento(
                                                caso.id, documentoPedido, "", responsable
                                            )
                                            documentoPedido = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                HorizontalDivider()

                                if (caso.etapa == "LIQUIDACION") {
                                    CampoDesplegable(
                                        etiqueta = stringResource(R.string.campo_resultado),
                                        seleccionado = resultado,
                                        opciones = RESULTADOS,
                                        onElegir = { resultado = it }
                                    )
                                    OutlinedTextField(
                                        value = mensajeCierre,
                                        onValueChange = { mensajeCierre = it },
                                        label = {
                                            Text(stringResource(R.string.campo_mensaje_cierre))
                                        },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (caso.etapa != CERRADO) {
                                    BotonPrincipal(
                                        texto = textoAvanzar(caso.etapa),
                                        onClick = {
                                            val archivando = caso.etapa == "LIQUIDACION"
                                            viewModel.avanzar(caso.id, resultado, mensajeCierre)
                                            if (archivando) {
                                                abierto = ""
                                                descripcion = ""
                                                mensajeCierre = ""
                                            }
                                        },
                                        icono = R.drawable.ic_seguir,
                                        habilitado = caso.etapa != "LIQUIDACION" ||
                                            mensajeCierre.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            imagenVista?.let { imagen ->
                AlertDialog(
                    onDismissRequest = { imagenVista = null },
                    confirmButton = {
                        TextButton(onClick = { imagenVista = null }) {
                            Text(stringResource(R.string.btn_cerrar))
                        }
                    },
                    text = {
                        Image(
                            bitmap = imagen.asImageBitmap(),
                            contentDescription = stringResource(
                                R.string.etiqueta_evidencias_recibidas
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                )
            }

            AvisoSuperior(aviso)
        }
    }
}

private fun etiquetaDeEtapa(etapa: String): String = when (etapa) {
    "RECIBIDO" -> "Recibido"
    "EVALUACION" -> "En evaluación"
    "LIQUIDACION" -> "En liquidación"
    "CERRADO" -> "Archivado"
    else -> etapa
}

private fun textoAvanzar(etapa: String): String = when (etapa) {
    "RECIBIDO" -> "Pasar a evaluación"
    "EVALUACION" -> "Pasar a liquidación"
    "LIQUIDACION" -> "Cerrar el caso"
    else -> "Avanzar"
}
