package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme

@Composable
fun BuscadorCasos(texto: String, onTexto: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTexto,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Buscar caso") },
        placeholder = { Text("Número, tipo, comuna o asegurado") },
        singleLine = true,
        trailingIcon = {
            if (texto.isNotEmpty()) {
                TextButton(onClick = { onTexto("") }) { Text("Limpiar") }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
internal fun BuscadorCasosPreview() {
    SegurosBpoTheme {
        Column(Modifier.padding(Dimens.espacioMedio)) {
            BuscadorCasos(texto = "choque", onTexto = {})
        }
    }
}
