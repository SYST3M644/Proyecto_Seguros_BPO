package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarDeslizando(
    cargando: Boolean,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        isRefreshing = cargando,
        onRefresh = onActualizar,
        modifier = modifier,
        content = contenido
    )
}
