package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.preview.Muestras
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSiniestroScreen(
    siniestro: Siniestro?,
    gestiones: List<Gestion>,
    onCargar: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(siniestro?.id) { if (siniestro != null) onCargar() }
    Scaffold(topBar = { BarraSuperior("Historial del siniestro") }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            if (siniestro == null) {
                Text("No se encontró un siniestro de este asegurado")
            } else {
                CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.titleLarge)
                Text("Gestiones visibles para el asegurado")
                if (gestiones.isEmpty()) Text("Todavía no existen movimientos registrados.")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioJusto)) {
                    items(gestiones, key = { it.id }) { gestion ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(Dimens.espacioMedio),
                                verticalArrangement = Arrangement.spacedBy(Dimens.espacioApretado)
                            ) {
                                Text(gestion.fecha, style = MaterialTheme.typography.labelMedium)
                                Text(gestion.descripcion, style = MaterialTheme.typography.bodyLarge)
                                Text("Responsable: "+gestion.responsable,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
internal fun HistorialSiniestroScreenPreview() {
    SegurosBpoTheme {
        HistorialSiniestroScreen(
            siniestro = Muestras.siniestroEnEvaluacion,
            gestiones = Muestras.gestiones,
            onCargar = {},
            onVolver = {}
        )
    }
}
