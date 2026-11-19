package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.ValidadorContacto
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.CuentaNuevaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(
    onListo: () -> Unit,
    onVolver: () -> Unit,
    viewModel: CuentaNuevaViewModel = viewModel()
) {
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val pregunta by viewModel.pregunta.collectAsStateWithLifecycle()

    var correo by rememberSaveable { mutableStateOf("") }
    var respuesta by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }

    val errorCorreo = ValidadorContacto.errorCorreo(correo)

    Scaffold(
        topBar = {
            BarraSuperior(stringResource(R.string.titulo_recuperar), onVolver = onVolver)
        }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            Text(
                stringResource(R.string.ayuda_recuperar),
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
                valor = correo,
                onValorCambia = { correo = it; viewModel.limpiarError() },
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                tipoTeclado = KeyboardType.Email,
                error = errorCorreo.takeIf { correo.isNotBlank() }
            )

            if (pregunta == null) {
                if (cargando) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                } else {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_buscar_cuenta),
                        onClick = { viewModel.buscarPregunta(correo) },
                        habilitado = errorCorreo == null && correo.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        pregunta.orEmpty(),
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                CampoTexto(
                    valor = respuesta,
                    onValorCambia = { respuesta = it; viewModel.limpiarError() },
                    etiqueta = stringResource(R.string.campo_respuesta),
                    icono = R.drawable.ic_candado
                )
                CampoContrasena(
                    valor = nueva,
                    onValorCambia = { nueva = it; viewModel.limpiarError() },
                    etiqueta = stringResource(R.string.campo_contrasena_nueva),
                    visible = visible,
                    onAlternarVisible = { visible = !visible },
                    error = if (nueva.isNotEmpty() && nueva.length < 6) {
                        stringResource(R.string.error_clave_corta)
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (cargando) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                } else {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_cambiar_contrasena),
                        onClick = { viewModel.recuperar(correo, respuesta, nueva, onListo) },
                        icono = R.drawable.ic_check,
                        habilitado = respuesta.isNotBlank() && nueva.length >= 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
internal fun RecuperarContrasenaScreenPreview() {
    SegurosBpoTheme {
        RecuperarContrasenaScreen(onListo = {}, onVolver = {})
    }
}
