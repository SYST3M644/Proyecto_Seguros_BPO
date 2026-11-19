package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.model.coincideCon
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.ActualizarDeslizando
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BuscadorCasos
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.preview.Muestras
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponiblesLiquidadorScreen(
    equipo: String,
    disponibles: List<SiniestroResponse>,
    onTomarCaso: (String) -> Unit,
    cargando: Boolean = false,
    onActualizar: () -> Unit = {}
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var busqueda by rememberSaveable(equipo) { mutableStateOf("") }

    Scaffold(
        topBar = { BarraSuperior("Casos disponibles") }
    ) { margen ->
        ActualizarDeslizando(
            cargando = cargando,
            onActualizar = { onActualizar() },
            modifier = Modifier.fillMaxSize().padding(margen).imePadding()
        ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            Text(
                "Sin asignar en " + equipo,
                style = MaterialTheme.typography.titleLarge
            )
            if (disponibles.isNotEmpty()) {
                BuscadorCasos(texto = busqueda, onTexto = { busqueda = it })
            }

            val texto = busqueda.trim().lowercase()
            val visibles = disponibles.filter { caso ->
                texto.isEmpty() || listOfNotNull(
                    caso.numero, caso.tipoNombre, caso.comuna, caso.aseguradoNombre
                ).any { it.lowercase().contains(texto) }
            }

            if (disponibles.isEmpty()) {
                Text("No hay siniestros pendientes de asignación en tu área.")
            } else if (visibles.isEmpty()) {
                Text("Ningún caso disponible coincide con la búsqueda.")
            } else {
                Text(
                    "Mostrando " + visibles.size + " de " + disponibles.size + " caso(s)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            visibles.forEach { disponible ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(Dimens.espacioTarjeta),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        CodigoSiniestro(disponible.numero)
                        Text(disponible.tipoNombre ?: disponible.tipoCodigo)
                        Text("Recibido · Sin liquidador asignado")
                        Button(onClick = { onTomarCaso(disponible.id) }) {
                            Text("Tomar caso")
                        }
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
internal fun DisponiblesLiquidadorScreenPreview() {
    SegurosBpoTheme {
        DisponiblesLiquidadorScreen(
            equipo = "VEHICULOS",
            disponibles = Muestras.siniestrosApi,
            onTomarCaso = {}
        )
    }
}
