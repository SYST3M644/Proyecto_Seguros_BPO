package cl.duocuc.segurosbpo.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.data.ArchivoEvidencia
import cl.duocuc.segurosbpo.data.CapturaFoto
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.permisos.Permisos
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CampoDesplegable
import cl.duocuc.segurosbpo.ui.components.LocalAccionesBarra
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    aseguradoId: String,
    viewModel: SiniestrosViewModel,
    onRegistrado: () -> Unit
) {
    val contexto = LocalContext.current
    val companias by viewModel.companias.collectAsStateWithLifecycle()
    val tipos by viewModel.tipos.collectAsStateWithLifecycle()
    val regiones by viewModel.regiones.collectAsStateWithLifecycle()
    val comunas by viewModel.comunas.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var companiaCodigo by rememberSaveable { mutableStateOf("") }
    var tipoCodigo by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var regionCodigo by rememberSaveable { mutableStateOf("") }
    var comuna by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var terceros by rememberSaveable { mutableStateOf(false) }
    var evidencias by remember { mutableStateOf<List<ArchivoEvidencia>>(emptyList()) }

    val documentosPedidos = tipos.firstOrNull { it.codigo == tipoCodigo }?.documentos.orEmpty()

    val seleccionarEvidencias = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { elegidas ->
        val leidas = elegidas.mapNotNull {
            runCatching { ProcesadorEvidencia.desdeSelector(contexto, it) }.getOrNull()
        }
        if (leidas.size < elegidas.size) {
            scope.launch { aviso.showSnackbar("Algunos archivos no se pudieron adjuntar") }
        }
        evidencias = (evidencias + leidas).take(15)
    }

    var destinoFoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var nombreFoto by rememberSaveable { mutableStateOf("") }
    var pidiendoNombre by rememberSaveable { mutableStateOf(false) }
    var renombrando by rememberSaveable { mutableStateOf(-1) }
    var nombreEnEdicion by rememberSaveable { mutableStateOf("") }

    val tomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { tomada ->
        val destino = destinoFoto
        destinoFoto = null
        if (destino == null) return@rememberLauncherForActivityResult
        if (!tomada) {
            CapturaFoto.borrar(contexto, destino)
            scope.launch { aviso.showSnackbar("Captura cancelada") }
            return@rememberLauncherForActivityResult
        }
        val foto = runCatching { ProcesadorEvidencia.desdeCaptura(contexto, destino) }
        CapturaFoto.borrar(contexto, destino)
        val elegido = nombreFoto.trim()
        foto.fold(
            onSuccess = { archivo ->
                val conNombre =
                    if (elegido.isEmpty()) archivo else archivo.copy(nombre = elegido)
                evidencias = (evidencias + conNombre).take(15)
            },
            onFailure = {
                scope.launch {
                    aviso.showSnackbar(it.message ?: "No se pudo adjuntar la fotografía")
                }
            }
        )
    }

    fun abrirCamara() {
        val destino = runCatching { CapturaFoto.nuevoDestino(contexto) }.getOrNull()
        if (destino == null) {
            scope.launch { aviso.showSnackbar("No se pudo preparar la cámara") }
            return
        }
        destinoFoto = destino
        tomarFoto.launch(destino)
    }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            scope.launch {
                val accion = aviso.showSnackbar(
                    message = "Sin permiso de cámara no se puede tomar la fotografía",
                    actionLabel = "Ajustes",
                    duration = SnackbarDuration.Long
                )
                if (accion == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(contexto)
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.cargarCatalogos() }

    val activas = companias.filter { it.activa }
    val puedeEnviar = companiaCodigo.isNotBlank() && tipoCodigo.isNotBlank() &&
        fecha.isNotBlank() && comuna.isNotBlank() && descripcion.isNotBlank() && !cargando

    val temaOscuro = LocalAccionesBarra.current.temaOscuro

    val abrirFecha = {
        val calendario = Calendar.getInstance()
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply { isLenient = false }
        runCatching { formato.parse(fecha) }.getOrNull()?.let { calendario.time = it }
        DatePickerDialog(
            contexto,
            if (temaOscuro) R.style.SelectorFechaRuedaOscuro else R.style.SelectorFechaRueda,
            { _, anio, mes, dia ->
                fecha = String.format(Locale.ROOT, "%02d-%02d-%04d", dia, mes + 1, anio)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_reportar)) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
            ) {
                Text(
                    stringResource(R.string.ayuda_reportar),
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

                CampoDesplegable(
                    etiqueta = stringResource(R.string.etiqueta_compania),
                    seleccionado = activas.firstOrNull { it.codigo == companiaCodigo }?.nombre ?: "",
                    opciones = activas.map { it.nombre },
                    onElegir = { nombre ->
                        companiaCodigo = activas.first { it.nombre == nombre }.codigo
                    }
                )

                CampoDesplegable(
                    etiqueta = stringResource(R.string.etiqueta_tipo),
                    seleccionado = tipos.firstOrNull { it.codigo == tipoCodigo }?.nombre ?: "",
                    opciones = tipos.map { it.nombre },
                    onElegir = { nombre ->
                        tipoCodigo = tipos.first { it.nombre == nombre }.codigo
                    }
                )

                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.campo_fecha)) },
                    placeholder = { Text(stringResource(R.string.pista_elegir_fecha)) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendario),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) abrirFecha() }
                )

                CampoDesplegable(
                    etiqueta = stringResource(R.string.campo_region),
                    seleccionado = regiones.firstOrNull { it.codigo == regionCodigo }?.nombre ?: "",
                    opciones = regiones.map { it.nombre },
                    onElegir = { nombre ->
                        regionCodigo = regiones.first { it.nombre == nombre }.codigo
                        comuna = ""
                        viewModel.cargarComunas(regionCodigo)
                    }
                )

                CampoDesplegable(
                    etiqueta = stringResource(R.string.campo_comuna),
                    seleccionado = comuna,
                    opciones = comunas.map { it.nombre },
                    onElegir = { comuna = it },
                    habilitado = regionCodigo.isNotBlank()
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

                Text(
                    stringResource(R.string.etiqueta_evidencias),
                    style = MaterialTheme.typography.titleSmall
                )

                documentosPedidos.takeIf { it.isNotEmpty() }?.let { pedidos ->
                    Text(
                        stringResource(R.string.ayuda_documentos_pedidos, pedidos.joinToString(", ")),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                BotonSecundario(
                    texto = stringResource(R.string.btn_tomar_foto),
                    onClick = {
                        nombreEnEdicion = documentosPedidos.getOrNull(evidencias.size)
                            ?: ("Fotografía " + (evidencias.size + 1))
                        pidiendoNombre = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonSecundario(
                    texto = stringResource(R.string.btn_elegir_galeria),
                    onClick = { seleccionarEvidencias.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (pidiendoNombre) {
                    AlertDialog(
                        onDismissRequest = { pidiendoNombre = false },
                        title = { Text(stringResource(R.string.titulo_nombre_evidencia)) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                            ) {
                                Text(
                                    stringResource(R.string.ayuda_nombre_evidencia),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = nombreEnEdicion,
                                    onValueChange = { nombreEnEdicion = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = nombreEnEdicion.isNotBlank(),
                                onClick = {
                                    pidiendoNombre = false
                                    nombreFoto = nombreEnEdicion.trim()
                                    if (Permisos.tiene(contexto, Permisos.CAMARA)) {
                                        abrirCamara()
                                    } else {
                                        permisoCamara.launch(Permisos.CAMARA)
                                    }
                                }
                            ) { Text(stringResource(R.string.btn_tomar_foto)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { pidiendoNombre = false }) {
                                Text(stringResource(R.string.btn_cancelar))
                            }
                        }
                    )
                }

                if (renombrando >= 0) {
                    AlertDialog(
                        onDismissRequest = { renombrando = -1 },
                        title = { Text(stringResource(R.string.titulo_renombrar_evidencia)) },
                        text = {
                            OutlinedTextField(
                                value = nombreEnEdicion,
                                onValueChange = { nombreEnEdicion = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(
                                enabled = nombreEnEdicion.isNotBlank(),
                                onClick = {
                                    val posicion = renombrando
                                    val nuevo = nombreEnEdicion.trim()
                                    renombrando = -1
                                    evidencias = evidencias.mapIndexed { indice, archivo ->
                                        if (indice == posicion) archivo.copy(nombre = nuevo)
                                        else archivo
                                    }
                                }
                            ) { Text(stringResource(R.string.btn_guardar_nombre)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { renombrando = -1 }) {
                                Text(stringResource(R.string.btn_cancelar))
                            }
                        }
                    )
                }

                if (evidencias.isNotEmpty()) {
                    Text(
                        stringResource(R.string.ayuda_tocar_renombrar),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                evidencias.forEachIndexed { posicion, archivo ->
                    Card(
                        onClick = {
                            nombreEnEdicion = archivo.nombre
                            renombrando = posicion
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(Dimens.espacioChico),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            Text(
                                archivo.nombre,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            BotonSecundario(
                                texto = stringResource(R.string.btn_quitar),
                                onClick = {
                                    evidencias = evidencias.filterIndexed { indice, _ ->
                                        indice != posicion
                                    }
                                }
                            )
                        }
                    }
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
                                    evidencias.forEach { archivo ->
                                        viewModel.subirEvidencia(
                                            nuevo.id,
                                            archivo.nombre,
                                            nuevo.aseguradoNombre.orEmpty(),
                                            archivo.nombre,
                                            archivo.mime,
                                            archivo.bytes
                                        )
                                    }
                                    regionCodigo = ""
                                    comuna = ""
                                    descripcion = ""
                                    fecha = ""
                                    terceros = false
                                    evidencias = emptyList()
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
