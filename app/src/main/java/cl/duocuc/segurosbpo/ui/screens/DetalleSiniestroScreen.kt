package cl.duocuc.segurosbpo.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.EvaluacionCierre
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorDesistimiento
import cl.duocuc.segurosbpo.model.ValidadorEvaluacion
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.preview.Muestras
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSiniestroScreen(
    siniestro: Siniestro?,
    solicitudes: List<SolicitudDocumento>,
    consultasPendientes: Int,
    evaluacion: EvaluacionCierre?,
    onCargarSolicitudes: () -> Unit,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEvidencias: () -> Unit,
    onHistorial: () -> Unit,
    onConsultas: () -> Unit,
    onDesistir: (String) -> String?,
    onGuardarEvaluacion: (Int, String) -> String?
) {
    val textos = LocalContext.current
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var motivoDesistir by rememberSaveable(siniestro?.id) { mutableStateOf("") }
    var pidiendoDesistir by remember(siniestro?.id) { mutableStateOf(false) }
    var nota by rememberSaveable(siniestro?.id) { mutableStateOf(0) }
    var comentario by rememberSaveable(siniestro?.id) { mutableStateOf("") }

    LaunchedEffect(siniestro?.id) {
        if (siniestro != null) onCargarSolicitudes()
    }

    if (pidiendoDesistir && siniestro != null) {
        val error = ValidadorDesistimiento.error(siniestro.etapa, motivoDesistir)
        AlertDialog(
            onDismissRequest = { pidiendoDesistir = false },
            title = { Text(stringResource(R.string.titulo_desistir)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    Text(stringResource(R.string.aviso_desistir))
                    OutlinedTextField(
                        value = motivoDesistir,
                        onValueChange = { motivoDesistir = it },
                        label = { Text(stringResource(R.string.campo_motivo)) },
                        minLines = 2,
                        isError = motivoDesistir.isNotEmpty() && error != null,
                        supportingText = { error?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = error == null,
                    onClick = {
                        val resultado = onDesistir(motivoDesistir)
                        pidiendoDesistir = false
                        if (resultado == null) motivoDesistir = ""
                        scope.launch {
                            aviso.showSnackbar(
                                resultado ?: textos.getString(R.string.msg_desistimiento_ok)
                            )
                        }
                    }
                ) { Text(stringResource(R.string.btn_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { pidiendoDesistir = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_detalle), onVolver = onVolver) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            if (siniestro == null) {
                Text(stringResource(R.string.vacio_siniestro_no_encontrado))
                return@Column
            }

            val pendientes = solicitudes.filter { it.pendiente }
            val cerrado = siniestro.etapa == EtapaSiniestro.CERRADO
            val plazo = ControlPlazos.resumenEtapa(
                siniestro.ultimaActualizacion,
                siniestro.compania.plazoDiasHabiles
            )

            CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.headlineSmall)

            BloqueDetalle(stringResource(R.string.seccion_estado_caso)) {
                DatoDetalle(stringResource(R.string.dato_etapa), siniestro.etapa.etiqueta)
                DatoDetalle(stringResource(R.string.dato_compania), siniestro.compania.etiqueta)
                DatoDetalle(stringResource(R.string.dato_tipo), siniestro.tipo.etiqueta)
                DatoDetalle(
                    stringResource(R.string.dato_liquidador),
                    siniestro.liquidadorNombre
                        ?: siniestro.liquidadorAsignado?.let {
                            stringResource(R.string.estado_equipo_por_asignar, it.equipo)
                        }
                        ?: stringResource(R.string.estado_pendiente_asignacion)
                )
                DatoDetalle(
                    stringResource(R.string.dato_ultima_actualizacion),
                    siniestro.ultimaActualizacion
                )
            }

            if (!cerrado) {
                BloqueDetalle(stringResource(R.string.seccion_tiempo_etapa)) {
                    LinearProgressIndicator(
                        progress = {
                            if (plazo.plazoDiasHabiles == 0) 0f
                            else minOf(
                                1f,
                                plazo.diasHabiles.toFloat() / plazo.plazoDiasHabiles
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DatoDetalle(
                        stringResource(R.string.dato_dias_habiles),
                        stringResource(
                            R.string.valor_de_total,
                            plazo.diasHabiles.toInt(), plazo.plazoDiasHabiles
                        )
                    )
                    Text(
                        if (plazo.excedido)
                            stringResource(
                                R.string.aviso_plazo_excedido, siniestro.compania.etiqueta
                            )
                        else stringResource(
                            R.string.aviso_plazo_restante, plazo.diasRestantes
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (plazo.excedido) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BloqueDetalle(stringResource(R.string.seccion_datos_siniestro)) {
                DatoDetalle(stringResource(R.string.dato_ocurrencia), siniestro.fechaOcurrencia)
                DatoDetalle(stringResource(R.string.dato_reporte), siniestro.fechaReporte)
                DatoDetalle(stringResource(R.string.dato_comuna), siniestro.comuna)
                DatoDetalle(
                    stringResource(R.string.dato_terceros),
                    stringResource(
                        if (siniestro.huboTerceros) R.string.valor_si else R.string.valor_no
                    )
                )
                Text(siniestro.descripcion, style = MaterialTheme.typography.bodyMedium)
                if (ValidadorSiniestro.reporteFueraDePlazo(
                        siniestro.fechaOcurrencia, siniestro.fechaReporte
                    )
                ) {
                    Text(
                        stringResource(R.string.aviso_fuera_de_plazo_detalle),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            BloqueDetalle(stringResource(R.string.seccion_que_sigue)) {
                if (pendientes.isNotEmpty()) {
                    Text(
                        if (pendientes.size == 1)
                            stringResource(R.string.aviso_un_pendiente)
                        else stringResource(R.string.aviso_varios_pendientes, pendientes.size),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                    pendientes.forEach { solicitud ->
                        Text(
                            stringResource(R.string.item_lista, solicitud.tipo),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    if (pendientes.isNotEmpty())
                        stringResource(R.string.paso_adjunta_documentos)
                    else when (siniestro.etapa) {
                        EtapaSiniestro.RECIBIDO ->
                            stringResource(R.string.proximo_paso_recibido)
                        EtapaSiniestro.EVALUACION ->
                            stringResource(R.string.paso_evaluacion)
                        EtapaSiniestro.LIQUIDACION ->
                            stringResource(R.string.paso_liquidacion)
                        EtapaSiniestro.CERRADO ->
                            stringResource(R.string.paso_cerrado)
                    }
                )
            }

            BloqueDetalle(stringResource(R.string.seccion_documentos_habituales)) {
                siniestro.tipo.documentosHabituales.forEach { documento ->
                    Text(
                        stringResource(R.string.item_lista, documento),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    stringResource(R.string.ayuda_otros_documentos),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (cerrado) {
                BloqueDetalle(stringResource(R.string.seccion_resultado)) {
                    DatoDetalle(
                        stringResource(R.string.seccion_resultado),
                        siniestro.resultadoCierre
                            ?: stringResource(R.string.valor_no_informado)
                    )
                    Text(
                        siniestro.mensajeCierre ?: stringResource(R.string.valor_sin_mensaje_cierre),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BloqueDetalle(stringResource(R.string.seccion_evaluacion)) {
                    if (evaluacion != null) {
                        DatoDetalle(
                            stringResource(R.string.dato_nota),
                            stringResource(R.string.valor_nota, evaluacion.nota)
                        )
                        if (!evaluacion.comentario.isNullOrBlank()) {
                            Text(evaluacion.comentario, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            stringResource(R.string.etiqueta_evaluado_el, evaluacion.fecha),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            stringResource(R.string.pregunta_evaluacion),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioCorto),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { valor ->
                                FilterChip(
                                    selected = nota == valor,
                                    onClick = { nota = valor },
                                    label = { Text(valor.toString()) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            label = { Text(stringResource(R.string.campo_comentario_opcional)) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ValidadorEvaluacion.error(nota, comentario) == null,
                            onClick = {
                                val error = onGuardarEvaluacion(nota, comentario)
                                scope.launch {
                                    aviso.showSnackbar(
                                        error
                                            ?: textos.getString(
                                                R.string.msg_gracias_evaluacion
                                            )
                                    )
                                }
                            }
                        ) { Text(stringResource(R.string.btn_enviar_evaluacion)) }
                    }
                }
            }

            BloqueDetalle(stringResource(R.string.seccion_acciones)) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onEvidencias
                ) { Text(stringResource(R.string.btn_documentos_evidencias)) }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConsultas
                ) {
                    Text(
                        if (consultasPendientes > 0)
                            stringResource(
                                R.string.btn_consultas_pendientes, consultasPendientes
                            )
                        else stringResource(R.string.btn_consultas_caso)
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onHistorial
                ) { Text(stringResource(R.string.btn_historial)) }
                if (siniestro.etapa == EtapaSiniestro.RECIBIDO) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onEditar
                    ) { Text(stringResource(R.string.btn_corregir_descripcion)) }
                }
                if (!cerrado) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { pidiendoDesistir = true }
                    ) {
                        Text(
                            stringResource(R.string.titulo_desistir),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}

@Composable
private fun BloqueDetalle(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun DatoDetalle(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
internal fun DetalleSiniestroScreenPreview() {
    SegurosBpoTheme {
        DetalleSiniestroScreen(
            siniestro = Muestras.siniestroEnEvaluacion,
            solicitudes = Muestras.solicitudes,
            consultasPendientes = 1,
            evaluacion = null,
            onCargarSolicitudes = {},
            onVolver = {},
            onEditar = {},
            onEvidencias = {},
            onHistorial = {},
            onConsultas = {},
            onDesistir = { null },
            onGuardarEvaluacion = { _, _ -> null }
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
internal fun DetalleSiniestroCerradoPreview() {
    SegurosBpoTheme {
        DetalleSiniestroScreen(
            siniestro = Muestras.siniestroCerrado,
            solicitudes = emptyList(),
            consultasPendientes = 0,
            evaluacion = Muestras.evaluacion,
            onCargarSolicitudes = {},
            onVolver = {},
            onEditar = {},
            onEvidencias = {},
            onHistorial = {},
            onConsultas = {},
            onDesistir = { null },
            onGuardarEvaluacion = { _, _ -> null }
        )
    }
}
