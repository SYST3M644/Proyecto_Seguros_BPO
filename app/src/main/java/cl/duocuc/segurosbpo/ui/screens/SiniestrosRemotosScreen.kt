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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.SiniestrosRemotosViewModel
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiniestrosRemotosScreen(
    usuarioId: String,
    rol: RolCuenta,
    viewModel: SiniestrosRemotosViewModel = viewModel()
) {
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val mensaje by viewModel.aviso.collectAsStateWithLifecycle()
    val aviso = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            aviso.showSnackbar(it)
            viewModel.limpiarAviso()
        }
    }

    LaunchedEffect(usuarioId, rol) {
        when (rol) {
            RolCuenta.ASEGURADO -> viewModel.cargarDeAsegurado(usuarioId)
            RolCuenta.LIQUIDADOR -> viewModel.cargarDeLiquidador(usuarioId)
            RolCuenta.ANALISTA -> viewModel.cargarSinAsignar()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_siniestros_api)) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                Text(
                    stringResource(R.string.ayuda_siniestros_api),
                    style = MaterialTheme.typography.bodySmall
                )

                if (cargando) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }

                error?.let { mensaje ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            Text(mensaje, color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = {
                                viewModel.limpiarError()
                                when (rol) {
                                    RolCuenta.ASEGURADO -> viewModel.cargarDeAsegurado(usuarioId)
                                    RolCuenta.LIQUIDADOR -> viewModel.cargarDeLiquidador(usuarioId)
                                    RolCuenta.ANALISTA -> viewModel.cargarSinAsignar()
                                }
                            }) { Text(stringResource(R.string.btn_reintentar)) }
                        }
                    }
                }

                if (!cargando && error == null && siniestros.isEmpty()) {
                    Text(stringResource(R.string.vacio_siniestros_api))
                }

                siniestros.forEach { siniestro ->
                    TarjetaSiniestroRemoto(siniestro)
                }
            }
            AvisoSuperior(aviso)
        }
    }
}

@Composable
private fun TarjetaSiniestroRemoto(siniestro: SiniestroResponse) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            CodigoSiniestro(siniestro.numero)
            Text(siniestro.tipoNombre ?: siniestro.tipoCodigo)
            Text(
                siniestro.companiaNombre ?: siniestro.companiaCodigo,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.dato_etapa) + ": " + siniestro.etapa,
                    style = MaterialTheme.typography.bodySmall
                )
                siniestro.plazoDiasHabiles?.let { plazo ->
                    Text(
                        stringResource(R.string.etiqueta_plazo_dias, plazo),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                stringResource(R.string.dato_liquidador) + ": " +
                    (siniestro.liquidadorNombre
                        ?: stringResource(R.string.estado_pendiente_asignacion)),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
internal fun SiniestrosRemotosScreenPreview() {
    SegurosBpoTheme {
        SiniestrosRemotosScreen(usuarioId = "1", rol = RolCuenta.ASEGURADO)
    }
}
