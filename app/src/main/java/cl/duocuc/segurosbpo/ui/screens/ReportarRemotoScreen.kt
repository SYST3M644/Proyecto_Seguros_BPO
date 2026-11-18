package cl.duocuc.segurosbpo.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.viewmodel.SiniestrosRemotosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarRemotoScreen(
    aseguradoId: String,
    viewModel: SiniestrosRemotosViewModel,
    onRegistrado: () -> Unit
) {
    val contexto = LocalContext.current
    val companias by viewModel.companias.collectAsStateWithLifecycle()
    val tipos by viewModel.tipos.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val aviso = remember { SnackbarHostState() }

    var companiaCodigo by rememberSaveable { mutableStateOf("") }
    var tipoCodigo by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var comuna by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var terceros by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarCatalogos() }

    val activas = companias.filter { it.activa }
    val puedeEnviar = companiaCodigo.isNotBlank() && tipoCodigo.isNotBlank() &&
        fecha.isNotBlank() && comuna.isNotBlank() && descripcion.isNotBlank() && !cargando

    val abrirFecha = {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            contexto,
            { _, anio, mes, dia ->
                fecha = String.format(Locale.ROOT, "%02d-%02d-%04d", dia, mes + 1, anio)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_reportar_api)) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                Text(
                    stringResource(R.string.ayuda_reportar_api),
                    style = MaterialTheme.typography.bodySmall
                )

                error?.let { mensaje ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            mensaje,
                            modifier = Modifier.padding(Dimens.espacioMedio),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Text(
                    stringResource(R.string.etiqueta_compania),
                    style = MaterialTheme.typography.titleSmall
                )
                activas.forEach { compania ->
                    FilterChip(
                        selected = companiaCodigo == compania.codigo,
                        onClick = { companiaCodigo = compania.codigo },
                        label = { Text(compania.nombre) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    stringResource(R.string.etiqueta_tipo),
                    style = MaterialTheme.typography.titleSmall
                )
                tipos.forEach { tipo ->
                    FilterChip(
                        selected = tipoCodigo == tipo.codigo,
                        onClick = { tipoCodigo = tipo.codigo },
                        label = { Text(tipo.nombre) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.campo_fecha)) },
                    modifier = Modifier.fillMaxWidth()
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_elegir_fecha),
                    onClick = abrirFecha,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = { Text(stringResource(R.string.campo_comuna)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(R.string.campo_descripcion)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = terceros, onCheckedChange = { terceros = it })
                    Text(stringResource(R.string.opcion_terceros))
                }

                if (cargando) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                } else {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_registrar),
                        onClick = {
                            viewModel.registrar(
                                aseguradoId, companiaCodigo, tipoCodigo,
                                fecha, comuna, descripcion, terceros
                            ) { nuevo ->
                                if (nuevo != null) {
                                    comuna = ""
                                    descripcion = ""
                                    fecha = ""
                                    terceros = false
                                    onRegistrado()
                                }
                            }
                        },
                        icono = R.drawable.ic_check,
                        habilitado = puedeEnviar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            AvisoSuperior(aviso)
        }
    }
}
