package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.PreguntasSeguridad
import cl.duocuc.segurosbpo.model.ValidadorContacto
import cl.duocuc.segurosbpo.model.ValidadorNombre
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.components.CampoDesplegable
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.CuentaNuevaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCuentaScreen(
    onCuentaCreada: () -> Unit,
    onVolver: () -> Unit,
    viewModel: CuentaNuevaViewModel = viewModel()
) {
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var creada by rememberSaveable { mutableStateOf(false) }

    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var repetida by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    var pregunta by rememberSaveable { mutableStateOf(PreguntasSeguridad.opciones.first()) }
    var respuesta by rememberSaveable { mutableStateOf("") }

    val errorNombre = ValidadorNombre.error(nombre)
    val errorCorreo = ValidadorContacto.errorCorreo(correo)
    val errorRut = if (rut.isBlank()) stringResource(R.string.error_rut_vacio)
        else ValidadorRut.error(rut)
    val errorClave = if (contrasena.length < 6) stringResource(R.string.error_clave_corta) else null
    val errorRepetida = if (repetida.isNotEmpty() && repetida != contrasena) {
        stringResource(R.string.error_claves_distintas)
    } else null
    val errorRespuesta = if (respuesta.isBlank()) stringResource(R.string.error_respuesta_vacia)
        else null

    val puedeCrear = errorNombre == null && errorCorreo == null && errorRut == null &&
        errorClave == null && errorRepetida == null && repetida == contrasena &&
        errorRespuesta == null && !cargando

    Scaffold(
        topBar = {
            BarraSuperior(stringResource(R.string.titulo_crear_cuenta), onVolver = onVolver)
        }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            Text(
                stringResource(R.string.ayuda_crear_cuenta),
                style = MaterialTheme.typography.bodyMedium
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

            CampoTexto(
                valor = nombre,
                onValorCambia = { nombre = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_nombre),
                icono = R.drawable.ic_persona,
                error = errorNombre.takeIf { nombre.isNotBlank() }
            )
            CampoTexto(
                valor = correo,
                onValorCambia = { correo = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email,
                error = errorCorreo.takeIf { correo.isNotBlank() }
            )
            CampoTexto(
                valor = rut,
                onValorCambia = { rut = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_rut),
                icono = R.drawable.ic_cambiar_usuario,
                error = errorRut.takeIf { rut.isNotBlank() },
                modifier = Modifier.onFocusChanged { foco ->
                    if (!foco.isFocused && rut.isNotBlank()) {
                        rut = ValidadorRut.formatear(rut)
                    }
                }
            )
            CampoTexto(
                valor = direccion,
                onValorCambia = { direccion = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_direccion),
                icono = R.drawable.ic_reportar
            )
            CampoContrasena(
                valor = contrasena,
                onValorCambia = { contrasena = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_contrasena),
                visible = visible,
                onAlternarVisible = { visible = !visible },
                error = errorClave.takeIf { contrasena.isNotBlank() },
                modifier = Modifier.fillMaxWidth()
            )

            CampoContrasena(
                valor = repetida,
                onValorCambia = { repetida = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_repetir_contrasena),
                visible = visible,
                onAlternarVisible = { visible = !visible },
                error = errorRepetida,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                stringResource(R.string.etiqueta_pregunta_seguridad),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                stringResource(R.string.ayuda_pregunta_seguridad),
                style = MaterialTheme.typography.bodySmall
            )
            CampoDesplegable(
                etiqueta = stringResource(R.string.campo_pregunta),
                seleccionado = pregunta,
                opciones = PreguntasSeguridad.opciones,
                onElegir = { pregunta = it }
            )
            CampoTexto(
                valor = respuesta,
                onValorCambia = { respuesta = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_respuesta),
                icono = R.drawable.ic_candado
            )

            if (cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_crear_cuenta),
                    onClick = {
                        viewModel.crearCuenta(
                            nombre, correo, rut, direccion, contrasena, pregunta, respuesta
                        ) { creada = true }
                    },
                    icono = R.drawable.ic_check,
                    habilitado = puedeCrear,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (creada) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.titulo_cuenta_pendiente)) },
            text = { Text(stringResource(R.string.ayuda_cuenta_pendiente)) },
            confirmButton = {
                TextButton(onClick = {
                    creada = false
                    onCuentaCreada()
                }) { Text(stringResource(R.string.btn_entendido)) }
            }
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun CrearCuentaScreenPreview() {
    SegurosBpoTheme {
        CrearCuentaScreen(onCuentaCreada = {}, onVolver = {})
    }
}
