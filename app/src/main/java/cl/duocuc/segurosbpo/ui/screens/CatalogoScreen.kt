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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import cl.duocuc.segurosbpo.data.remote.CompaniaResponse
import cl.duocuc.segurosbpo.data.remote.TipoSiniestroResponse
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.FilaCatalogo
import cl.duocuc.segurosbpo.ui.components.ListaCatalogo
import kotlinx.coroutines.launch
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    companias: List<CompaniaResponse>,
    tipos: List<TipoSiniestroResponse>,
    onCrearCompania: (String, Int) -> Unit,
    onCrearTipo: (String, Boolean, List<String>) -> Unit
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var formulario by rememberSaveable { mutableStateOf("") }
    var nombreCompania by rememberSaveable { mutableStateOf("") }
    var plazo by rememberSaveable { mutableStateOf("10") }
    var nombreTipo by rememberSaveable { mutableStateOf("") }
    var esVehiculo by rememberSaveable { mutableStateOf(true) }
    var documentos by rememberSaveable { mutableStateOf("") }

    val plazoValido = plazo.toIntOrNull()?.let { it in 1..60 } == true

    Scaffold(topBar = { BarraSuperior("Catálogos del sistema") }) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            formulario = if (formulario == "compania") "" else "compania"
                        }
                    ) { Text("Nueva compañía") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { formulario = if (formulario == "tipo") "" else "tipo" }
                    ) { Text("Nuevo tipo") }
                }

                if (formulario == "compania") Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(Dimens.espacioMedio),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioJusto)
                    ) {
                        Text(
                            "Agregar compañía de seguros",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nombreCompania,
                            onValueChange = { nombreCompania = it },
                            label = { Text("Nombre de la compañía") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = plazo,
                            onValueChange = { plazo = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Plazo de respuesta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = plazo.isNotEmpty() && !plazoValido,
                            supportingText = { Text("Días hábiles comprometidos, de 1 a 60") }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nombreCompania.trim().length >= 3 && plazoValido,
                            onClick = {
                                onCrearCompania(nombreCompania, plazo.toIntOrNull() ?: 10)
                                nombreCompania = ""
                                plazo = "10"
                            }
                        ) { Text("Agregar compañía") }
                    }
                }

                ListaCatalogo(
                    titulo = "Compañías registradas",
                    pista = "Nombre de la compañía",
                    filas = companias.map { compania ->
                        FilaCatalogo(
                            titulo = compania.nombre,
                            detalle = compania.plazoDiasHabiles.toString() + " días hábiles" +
                                if (compania.activa) "" else " · no disponible"
                        )
                    }
                )

                if (formulario == "tipo") Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(Dimens.espacioMedio),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioJusto)
                    ) {
                        Text(
                            "Agregar tipo de siniestro",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nombreTipo,
                            onValueChange = { nombreTipo = it },
                            label = { Text("Nombre del tipo") },
                            placeholder = { Text("Ej.: Hogar · rotura de cañería") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text("Área responsable", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            FilterChip(
                                selected = esVehiculo,
                                onClick = { esVehiculo = true },
                                label = { Text("Vehículos") }
                            )
                            FilterChip(
                                selected = !esVehiculo,
                                onClick = { esVehiculo = false },
                                label = { Text("Hogar") }
                            )
                        }
                        OutlinedTextField(
                            value = documentos,
                            onValueChange = { documentos = it },
                            label = { Text("Documentos habituales") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            supportingText = { Text("Uno por línea") }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nombreTipo.trim().length >= 3,
                            onClick = {
                                onCrearTipo(
                                    nombreTipo,
                                    esVehiculo,
                                    documentos.split("\n")
                                )
                                nombreTipo = ""
                                documentos = ""
                            }
                        ) { Text("Agregar tipo") }
                    }
                }

                ListaCatalogo(
                    titulo = "Tipos registrados",
                    pista = "Nombre del tipo",
                    filas = tipos.map { tipo ->
                        FilaCatalogo(
                            titulo = tipo.nombre,
                            detalle = if (tipo.esVehiculo) "Equipo Vehículos" else "Equipo Hogar",
                            extras = tipo.documentos
                        )
                    }
                )
            }
            AvisoSuperior(aviso)
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
internal fun CatalogoScreenPreview() {
    SegurosBpoTheme {
        CatalogoScreen(
            companias = emptyList(),
            tipos = emptyList(),
            onCrearCompania = { _, _ -> },
            onCrearTipo = { _, _, _ -> }
        )
    }
}
