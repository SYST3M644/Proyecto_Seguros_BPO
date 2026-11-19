package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onSesionIniciada: (UsuarioResponse) -> Unit,
    onCrearCuenta: () -> Unit = {},
    onOlvideContrasena: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.titulo_login), destacada = true) }
    ) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Image(
                painter = painterResource(R.drawable.mi_siniestro_logo),
                contentDescription = stringResource(R.string.cd_logo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(Dimens.altoLogoLogin)
            )
            Text(
                stringResource(R.string.login_bienvenida),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                stringResource(R.string.login_ayuda),
                style = MaterialTheme.typography.bodyMedium
            )

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = viewModel::onCorreoCambia,
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                error = uiState.errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = viewModel::onContrasenaCambia,
                etiqueta = stringResource(R.string.campo_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = viewModel::onAlternarVisibilidad,
                error = uiState.errorContrasena
            )

            uiState.errorGeneral?.let { mensaje ->
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        mensaje,
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_ingresar),
                    onClick = { viewModel.iniciarSesion(onSesionIniciada) },
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
                BotonSecundario(
                    texto = stringResource(R.string.btn_ir_crear_cuenta),
                    onClick = onCrearCuenta,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = onOlvideContrasena,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(R.string.btn_olvide_contrasena)) }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
internal fun LoginScreenPreview() {
    SegurosBpoTheme {
        LoginScreen(onSesionIniciada = {})
    }
}
