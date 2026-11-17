package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.data.remote.CasoServidor
import cl.duocuc.segurosbpo.data.remote.GestionServidor
import cl.duocuc.segurosbpo.data.remote.ServidorSiniestros
import cl.duocuc.segurosbpo.model.Asegurado

@Composable
fun SiniestrosServidorScreen(
    asegurado: Asegurado,
    estadoSincronizacion: String,
    onVolver: () -> Unit
) {
    var casos by remember(asegurado.rut) { mutableStateOf<List<CasoServidor>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var recargas by remember { mutableIntStateOf(0) }
    var seleccionado by remember { mutableStateOf<String?>(null) }
    var gestiones by remember { mutableStateOf<List<GestionServidor>>(emptyList()) }
    var detalle by remember { mutableStateOf("") }

    LaunchedEffect(asegurado.rut, recargas, estadoSincronizacion) {
        cargando = true
        val consulta = runCatching { ServidorSiniestros.listar(asegurado) }
        consulta.onSuccess {
            casos = it
            mensaje = if (it.isEmpty()) "No tienes siniestros en el servidor" else ""
        }.onFailure {
            mensaje = "Servidor no disponible. Los datos locales se mantienen en el teléfono."
        }
        cargando = false
    }

    LaunchedEffect(asegurado.rut, seleccionado) {
        val numero = seleccionado ?: return@LaunchedEffect
        detalle = "Consultando historial..."
        runCatching { ServidorSiniestros.historial(asegurado, numero) }
            .onSuccess { gestiones = it; detalle = "" }
            .onFailure { detalle = "No fue posible consultar el historial remoto"; gestiones = emptyList() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Siniestros en servidor")
        Text("Información compartida por el backend Java. Solo se muestran casos del asegurado seleccionado.")
        if (estadoSincronizacion.isNotBlank()) Text(estadoSincronizacion)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onVolver) { Text("Volver") }
            Button(onClick = { recargas++ }, enabled = !cargando) { Text("Actualizar") }
        }
        if (cargando) CircularProgressIndicator()
        if (mensaje.isNotBlank()) Text(mensaje)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(casos, key = { it.numero }) { caso ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        seleccionado = if (seleccionado == caso.numero) null else caso.numero
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(caso.numero)
                        Text("Estado: " + caso.estado)
                        Text("Tipo: " + caso.tipo)
                        Text("Compañía: " + caso.compania)
                        Text(caso.proximoPaso.orEmpty())
                        if (seleccionado == caso.numero) {
                            if (detalle.isNotBlank()) Text(detalle)
                            if (gestiones.isEmpty() && detalle.isBlank()) Text("Sin gestiones visibles")
                            gestiones.forEach { gestion ->
                                Text(gestion.fechaHora + " · " + gestion.descripcion)
                            }
                        }
                    }
                }
            }
        }
    }
}
