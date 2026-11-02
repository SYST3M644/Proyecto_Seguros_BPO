package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.etiquetaEquipo
import cl.duocuc.segurosbpo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionUsuarioScreen(
    usuarios: List<CuentaLocal>,
    onSeleccionar: (String) -> Unit
) {
    val asegurados = usuarios.filter { it.rol == RolCuenta.ASEGURADO }
    val liquidadores = usuarios.filter { it.rol == RolCuenta.LIQUIDADOR }
    val analistas = usuarios.filter { it.rol == RolCuenta.ANALISTA }

    Scaffold(topBar = { BarraSuperior(stringResource(R.string.titulo_seleccion_usuario)) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).imePadding()
                .verticalScroll(rememberScrollState()).padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioRegular)
        ) {
            Text(
                stringResource(R.string.titulo_elige_quien_eres),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.ayuda_elige_cuenta),
                style = MaterialTheme.typography.bodySmall
            )

            if (asegurados.isNotEmpty()) {
                Text(
                    stringResource(R.string.grupo_asegurados),
                    style = MaterialTheme.typography.titleMedium
                )
                asegurados.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = stringResource(R.string.etiqueta_rut, rutOculto(cuenta)),
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (liquidadores.isNotEmpty()) {
                Text(
                    stringResource(R.string.grupo_liquidadores),
                    style = MaterialTheme.typography.titleMedium
                )
                liquidadores.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = stringResource(
                            R.string.etiqueta_equipo_de, etiquetaEquipo(cuenta.equipo)
                        ),
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (analistas.isNotEmpty()) {
                Text(
                    stringResource(R.string.grupo_administracion),
                    style = MaterialTheme.typography.titleMedium
                )
                analistas.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = stringResource(R.string.etiqueta_analista),
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (usuarios.isEmpty()) {
                Text(
                    stringResource(R.string.vacio_sin_usuarios),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun rutOculto(cuenta: CuentaLocal): String = ValidadorRut.ocultar(cuenta.rut)

@Composable
private fun TarjetaUsuario(
    nombre: String,
    detalle: String,
    foto: ByteArray? = null,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.espacioMedio),
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioRegular),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCuenta(foto = foto, descripcion = nombre, tamano = Dimens.tamanoAvatarLista)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
