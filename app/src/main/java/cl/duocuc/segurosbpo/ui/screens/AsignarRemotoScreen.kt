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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.viewmodel.SiniestrosRemotosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarRemotoScreen(viewModel: SiniestrosRemotosViewModel) {
    val pendientes by viewModel.siniestros.collectAsStateWithLifecycle()
    val liquidadores by viewModel.liquidadores.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val mensaje by viewModel.aviso.collectAsStateWithLifecycle()
    val aviso = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarSinAsignar() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            aviso.showSnackbar(it)
            viewModel.limpiarAviso()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_asignar_api)) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
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
                            OutlinedButton(onClick = {
                                viewModel.limpiarError()
                                viewModel.cargarSinAsignar()
                            }) { Text(stringResource(R.string.btn_reintentar)) }
                        }
                    }
                }

                if (!cargando && error == null && pendientes.isEmpty()) {
                    Text(stringResource(R.string.vacio_sin_asignar_api))
                }

                pendientes.forEach { caso ->
                    val esVehiculo = caso.tipoCodigo.startsWith("CHOQUE") ||
                        caso.tipoCodigo.startsWith("ROBO_VEHICULO") ||
                        caso.tipoCodigo.startsWith("CRISTALES")
                    val equipo = if (esVehiculo) "VEHICULOS" else "HOGAR"
                    val candidatos = liquidadores.filter { it.equipo == equipo }

                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            CodigoSiniestro(caso.numero)
                            Text(caso.tipoNombre ?: caso.tipoCodigo)
                            Text(
                                caso.companiaNombre ?: caso.companiaCodigo,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                stringResource(
                                    R.string.etiqueta_asegurado_actual,
                                    caso.aseguradoNombre.orEmpty()
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (candidatos.isEmpty()) {
                                Text(
                                    stringResource(R.string.etiqueta_sin_liquidadores),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            candidatos.forEach { persona ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        Dimens.espacioChico
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(persona.nombre, modifier = Modifier.weight(1f))
                                    Button(
                                        enabled = !cargando,
                                        onClick = {
                                            viewModel.asignar(
                                                caso.id, persona.id, persona.nombre
                                            )
                                        }
                                    ) { Text(stringResource(R.string.btn_asignar)) }
                                }
                            }
                        }
                    }
                }
            }
            AvisoSuperior(aviso)
        }
    }
}
