package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import androidx.compose.ui.tooling.preview.Preview

data class AccionesBarra(
    val temaOscuro: Boolean = false,
    val onAlternarTema: () -> Unit = {},
    val onAbrirMenu: () -> Unit = {},
    val onVolver: () -> Unit = {},
    val puedeVolver: Boolean = false,
    val hayMenu: Boolean = false
)

val LocalAccionesBarra = compositionLocalOf { AccionesBarra() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    destacada: Boolean = false,
    conLogo: Boolean = true
) {
    val acciones = LocalAccionesBarra.current
    val volver = onVolver ?: if (acciones.puedeVolver) acciones.onVolver else null

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                if (conLogo) {
                    Image(
                        painter = painterResource(R.drawable.mi_siniestro_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(Dimens.tamanoLogoBarra)
                    )
                }
                Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (volver != null) {
                IconButton(onClick = volver) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volver),
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = acciones.onAlternarTema) {
                Icon(
                    painter = painterResource(
                        if (acciones.temaOscuro) R.drawable.ic_sol else R.drawable.ic_luna
                    ),
                    contentDescription = if (acciones.temaOscuro) "Activar modo claro"
                        else "Activar modo oscuro"
                )
            }
        },
        colors = if (destacada) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else TopAppBarDefaults.topAppBarColors()
    )
}

@Preview(showBackground = true)
@Composable
internal fun BarraSuperiorPreview() {
    SegurosBpoTheme {
        BarraSuperior(titulo = "Detalle del siniestro", onVolver = {})
    }
}

@Preview(showBackground = true)
@Composable
internal fun BarraSuperiorDestacadaPreview() {
    SegurosBpoTheme {
        BarraSuperior(titulo = "Reportar un siniestro", destacada = true)
    }
}
