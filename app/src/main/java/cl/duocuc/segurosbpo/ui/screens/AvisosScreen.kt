package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.data.remote.AvisoResponse
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.ui.components.ActualizarDeslizando
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.preview.Muestras
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(
    avisos: List<AvisoResponse>,
    onCargar: () -> Unit,
    onMarcarLeido: (String) -> Unit,
    onAbrirCaso: (String) -> Unit,
    onAdjuntar: (String) -> Unit,
    onVolver: () -> Unit,
    cargando: Boolean = false,
    pendientes: List<UsuarioResponse> = emptyList(),
    onAprobar: (String) -> Unit = {},
    onRechazar: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) { onCargar() }
    Scaffold(topBar = { BarraSuperior(stringResource(R.string.titulo_avisos)) }) { margen ->
        ActualizarDeslizando(
            cargando = cargando,
            onActualizar = onCargar,
            modifier = Modifier.fillMaxSize().padding(margen)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().imePadding().padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                if (avisos.isEmpty() && pendientes.isEmpty()) {
                    Text(stringResource(R.string.vacio_sin_avisos))
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    if (pendientes.isNotEmpty()) {
                        item(key = "titulo-pendientes") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimens.espacioCorto)
                            ) {
                                Text(
                                    stringResource(R.string.titulo_asegurados_pendientes),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.ayuda_asegurados_pendientes),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    items(pendientes, key = { "pendiente-" + it.id }) { asegurado ->
                        TarjetaAseguradoPendiente(
                            asegurado = asegurado,
                            onAprobar = { onAprobar(asegurado.id) },
                            onRechazar = { onRechazar(asegurado.id) }
                        )
                    }
                    items(avisos, key = { "aviso-" + it.id }) { aviso ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(Dimens.espacioMedio),
                                verticalArrangement = Arrangement.spacedBy(Dimens.espacioCorto)
                            ) {
                                CodigoSiniestro(aviso.numero)
                                Text(aviso.mensaje)
                                Text(
                                    stringResource(
                                        R.string.etiqueta_fecha_estado,
                                        aviso.fecha,
                                        stringResource(
                                            if (aviso.leido) R.string.estado_leido
                                            else R.string.estado_no_leido
                                        )
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (aviso.mensaje.startsWith(PIDEN_DOCUMENTO)) {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            onMarcarLeido(aviso.id)
                                            onAdjuntar(aviso.siniestroId)
                                        }
                                    ) {
                                        Text(
                                            stringResource(
                                                R.string.btn_enviar_documento,
                                                aviso.mensaje.removePrefix(PIDEN_DOCUMENTO)
                                            )
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(Dimens.espacioMini)
                                ) {
                                    TextButton(onClick = {
                                        onMarcarLeido(aviso.id)
                                        onAbrirCaso(aviso.siniestroId)
                                    }) { Text(stringResource(R.string.btn_ver_siniestro)) }
                                    if (!aviso.leido) {
                                        TextButton(onClick = { onMarcarLeido(aviso.id) }) {
                                            Text(stringResource(R.string.btn_marcar_leido))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAseguradoPendiente(
    asegurado: UsuarioResponse,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(asegurado.nombre, style = MaterialTheme.typography.titleSmall)
            Text(asegurado.correo, style = MaterialTheme.typography.bodySmall)
            if (!asegurado.rut.isNullOrBlank()) {
                Text(
                    stringResource(
                        R.string.etiqueta_rut,
                        ValidadorRut.formatear(asegurado.rut)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_aprobar_asegurado),
                    onClick = onAprobar,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.weight(1f)
                )
                BotonSecundario(
                    texto = stringResource(R.string.btn_rechazar_asegurado),
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AvisosScreenPreview() {
    SegurosBpoTheme {
        AvisosScreen(
            avisos = Muestras.avisosApi,
            onCargar = {},
            onMarcarLeido = {},
            onAbrirCaso = {},
            onAdjuntar = {},
            onVolver = {}
        )
    }
}

private const val PIDEN_DOCUMENTO = "Te piden un documento: "
