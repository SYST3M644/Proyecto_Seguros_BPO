package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.preview.Muestras
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.components.ActualizarDeslizando
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.components.EtiquetaInteres
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.model.OrdenSiniestros.CERRADO
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import cl.duocuc.segurosbpo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    nombreAsegurado: String,
    rutAsegurado: String,
    fotoCuenta: ByteArray?,
    avatarUrl: String? = null,
    actualizadoEn: String,
    siniestros: List<SiniestroResponse>,
    busqueda: String,
    onBuscar: (String) -> Unit,
    onVerDetalle: (String) -> Unit,
    onNuevoReporte: () -> Unit,
    cargando: Boolean = false,
    onActualizar: () -> Unit = {}
) {
    var verCerrados by rememberSaveable { mutableStateOf(false) }
    val abiertos = remember(siniestros) { siniestros.filter { it.etapa != CERRADO } }
    val cerrados = remember(siniestros) { siniestros.filter { it.etapa == CERRADO } }
    val mostrados = if (verCerrados) cerrados else abiertos

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_mis_siniestros)) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoReporte) {
                Icon(
                    painter = painterResource(R.drawable.ic_mas),
                    contentDescription = stringResource(R.string.cd_nuevo_reporte)
                )
            }
        }
    ) { margen ->
        ActualizarDeslizando(
            cargando = cargando,
            onActualizar = onActualizar,
            modifier = Modifier.fillMaxSize().padding(margen)
        ) {
            Column(
                Modifier.fillMaxSize().imePadding().padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                Text(
                    stringResource(R.string.titulo_seguimiento),
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
                ) {
                    AvatarCuenta(
                        foto = fotoCuenta,
                        descripcion = stringResource(R.string.cd_foto_asegurado),
                        tamano = Dimens.tamanoAdjunto,
                        avatarUrl = avatarUrl
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                        Text(stringResource(R.string.etiqueta_asegurado_actual, nombreAsegurado))
                        Text(
                            stringResource(
                                R.string.etiqueta_rut, ValidadorRut.ocultar(rutAsegurado)
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            stringResource(R.string.etiqueta_datos_al, actualizadoEn),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = onBuscar,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.campo_buscar_siniestro)) },
                    placeholder = { Text(stringResource(R.string.pista_buscar_siniestro)) },
                    singleLine = true
                )
                AnimatedVisibility(
                    visible = siniestros.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.espacioPantalla)) {
                            Text(stringResource(
                                if (busqueda.isBlank()) R.string.vacio_sin_siniestros
                                else R.string.vacio_sin_coincidencias
                            ))
                            Text(stringResource(
                                if (busqueda.isBlank()) R.string.ayuda_sin_siniestros
                                else R.string.ayuda_sin_coincidencias
                            ))
                        }
                    }
                }
                if (siniestros.isNotEmpty()) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !verCerrados,
                            onClick = { verCerrados = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(stringResource(R.string.tab_casos_abiertos, abiertos.size))
                        }
                        SegmentedButton(
                            selected = verCerrados,
                            onClick = { verCerrados = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(stringResource(R.string.tab_casos_cerrados, cerrados.size))
                        }
                    }
                }
                if (siniestros.isNotEmpty() && mostrados.isEmpty()) {
                    Text(
                        stringResource(
                            if (verCerrados) R.string.vacio_casos_cerrados
                            else R.string.vacio_casos_abiertos
                        )
                    )
                }
                if (mostrados.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioJusto)
                    ) {
                        items(mostrados, key = { it.id }) { siniestro ->
                            Card(
                                onClick = { onVerDetalle(siniestro.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.padding(Dimens.espacioMedio),
                                    verticalArrangement =
                                        Arrangement.spacedBy(Dimens.espacioApretado)
                                ) {
                                    CodigoSiniestro(siniestro.numero)
                                    Text(siniestro.companiaNombre ?: siniestro.companiaCodigo)
                                    Text(siniestro.tipoNombre ?: siniestro.tipoCodigo)
                                    EtiquetaInteres(
                                        texto = stringResource(
                                            R.string.etiqueta_estado,
                                            etiquetaEtapa(siniestro.etapa)
                                        )
                                    )
                                    if (ValidadorSiniestro.reporteFueraDePlazo(
                                            siniestro.fechaOcurrencia, siniestro.fechaReporte
                                        )) {
                                        Text(
                                            stringResource(R.string.aviso_fuera_de_plazo),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        stringResource(
                                            R.string.etiqueta_actualizado,
                                            siniestro.ultimaActualizacion
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
internal fun MisSiniestrosScreenPreview() {
    SegurosBpoTheme {
        MisSiniestrosScreen(
            nombreAsegurado = "Camila Rojas Fuentes",
            rutAsegurado = "15.842.371-5",
            fotoCuenta = null,
            actualizadoEn = Muestras.FECHA_HORA,
            siniestros = Muestras.siniestrosApi,
            busqueda = "",
            onBuscar = {},
            onVerDetalle = {},
            onNuevoReporte = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
internal fun MisSiniestrosVacioPreview() {
    SegurosBpoTheme {
        MisSiniestrosScreen(
            nombreAsegurado = "Camila Rojas Fuentes",
            rutAsegurado = "15.842.371-5",
            fotoCuenta = null,
            actualizadoEn = Muestras.FECHA_HORA,
            siniestros = emptyList(),
            busqueda = "",
            onBuscar = {},
            onVerDetalle = {},
            onNuevoReporte = {}
        )
    }
}

private fun etiquetaEtapa(etapa: String): String = when (etapa) {
    "RECIBIDO" -> "Recibido"
    "EVALUACION" -> "En evaluación"
    "LIQUIDACION" -> "En liquidación"
    "CERRADO" -> "Cerrado"
    else -> etapa
}
