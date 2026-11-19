package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import androidx.compose.ui.tooling.preview.Preview

data class FilaCatalogo(
    val titulo: String,
    val detalle: String = "",
    val extras: List<String> = emptyList()
)

private const val INICIALES = 3
private const val PASO = 5

@Composable
fun ListaCatalogo(
    titulo: String,
    filas: List<FilaCatalogo>,
    pista: String
) {
    var busqueda by rememberSaveable(titulo) { mutableStateOf("") }
    var visibles by rememberSaveable(titulo) { mutableIntStateOf(INICIALES) }

    val encontradas = remember(filas, busqueda) {
        val texto = busqueda.trim().lowercase()
        val ordenadas = filas.sortedBy { it.titulo.lowercase() }
        if (texto.isEmpty()) ordenadas
        else ordenadas.filter { fila ->
            fila.titulo.lowercase().contains(texto) || fila.detalle.lowercase().contains(texto)
        }
    }
    val mostradas = encontradas.take(visibles)

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                "$titulo (${encontradas.size})",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                    visibles = INICIALES
                },
                label = { Text("Buscar") },
                placeholder = { Text(pista) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (encontradas.isEmpty()) {
                Text("Sin resultados", style = MaterialTheme.typography.bodySmall)
            }

            mostradas.forEachIndexed { indice, fila ->
                if (indice > 0) HorizontalDivider()
                Text(fila.titulo, style = MaterialTheme.typography.bodyLarge)
                if (fila.detalle.isNotBlank()) {
                    Text(fila.detalle, style = MaterialTheme.typography.bodySmall)
                }
                fila.extras.forEach {
                    Text("· $it", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (encontradas.size > visibles) {
                BotonSecundario(
                    texto = "Ver más (${encontradas.size - visibles} restantes)",
                    onClick = { visibles += PASO },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (visibles > INICIALES) {
                BotonSecundario(
                    texto = "Ver menos",
                    onClick = { visibles = INICIALES },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun ListaCatalogoPreview() {
    SegurosBpoTheme {
        ListaCatalogo(
            titulo = "Compañías registradas",
            pista = "Nombre de la compañía",
            filas = listOf(
                FilaCatalogo("Aseguradora Patagonia Sur", "12 días hábiles"),
                FilaCatalogo("Protege Seguros", "12 días hábiles"),
                FilaCatalogo("Seguros Cordillera", "10 días hábiles")
            )
        )
    }
}
