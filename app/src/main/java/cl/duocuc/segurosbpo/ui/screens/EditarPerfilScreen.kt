package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.ValidadorContacto
import cl.duocuc.segurosbpo.model.ValidadorNombre
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.preview.Muestras
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    cuenta: CuentaLocal,
    onGuardar: (String, String, String, String, String, (String?) -> Unit) -> Unit,
    onCambiarContrasena: (String, String, (String?) -> Unit) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.nombre) }
    var rut by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.rut) }
    var correo by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.correo) }
    var telefono by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.telefono) }
    var direccion by rememberSaveable(cuenta.id) {
        mutableStateOf(cuenta.direccion.orEmpty())
    }
    var tocoNombre by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var tocoRut by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var tocoCorreo by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var tocoTelefono by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var guardado by remember { mutableStateOf(false) }

    var actual by rememberSaveable(cuenta.id) { mutableStateOf("") }
    var nueva by rememberSaveable(cuenta.id) { mutableStateOf("") }
    var mensajeClave by remember { mutableStateOf<String?>(null) }
    var claveCambiada by remember { mutableStateOf(false) }
    var clavesVisibles by rememberSaveable(cuenta.id) { mutableStateOf(false) }

    val errorNombre = ValidadorNombre.error(nombre)
    val errorRut = if (rut.isBlank()) null else ValidadorRut.error(rut)
    val errorCorreo = ValidadorContacto.errorCorreo(correo)
    val errorTelefono = ValidadorContacto.errorTelefono(telefono)
    val hayCambios = nombre.trim() != cuenta.nombre ||
        rut.trim() != cuenta.rut ||
        ValidadorContacto.normalizarCorreo(correo) != cuenta.correo ||
        ValidadorContacto.normalizarTelefono(telefono) != cuenta.telefono ||
        direccion.trim() != cuenta.direccion.orEmpty()
    val puedeGuardar = hayCambios && errorNombre == null && errorRut == null &&
        errorCorreo == null && errorTelefono == null
    val puedeCambiarClave = actual.isNotBlank() && nueva.length >= 6

    Scaffold(topBar = { BarraSuperior("Editar perfil", onVolver = onVolver) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).imePadding().verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioTarjeta)
        ) {
            Text("Datos de contacto", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    tocoNombre = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = tocoNombre && errorNombre != null,
                supportingText = { errorNombre?.takeIf { tocoNombre }?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rut,
                onValueChange = {
                    rut = it
                    tocoRut = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("RUT") },
                singleLine = true,
                isError = tocoRut && errorRut != null,
                supportingText = {
                    Text(
                        if (tocoRut && errorRut != null) errorRut!!
                        else "Ejemplo: 15.842.371-5"
                    )
                },
                modifier = Modifier.fillMaxWidth().onFocusChanged { foco ->
                    if (!foco.isFocused && rut.isNotBlank()) {
                        rut = ValidadorRut.formatear(rut)
                    }
                }
            )
            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    tocoCorreo = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = tocoCorreo && errorCorreo != null,
                supportingText = {
                    Text(
                        if (tocoCorreo && errorCorreo != null) errorCorreo
                        else "Ejemplo: nombre@correo.cl"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    tocoTelefono = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("Teléfono de contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = tocoTelefono && errorTelefono != null,
                supportingText = {
                    Text(
                        if (tocoTelefono && errorTelefono != null) errorTelefono
                        else "Solo números, entre 8 y 11 dígitos"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                singleLine = true,
                supportingText = { Text("Calle, número y comuna") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioJusto)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = hayCambios,
                    onClick = {
                        nombre = cuenta.nombre
                        rut = cuenta.rut
                        correo = cuenta.correo
                        telefono = cuenta.telefono
                        direccion = cuenta.direccion.orEmpty()
                        tocoNombre = false
                        tocoRut = false
                        tocoCorreo = false
                        tocoTelefono = false
                        mensaje = null
                        guardado = false
                    }
                ) { Text("Descartar") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = puedeGuardar,
                    onClick = {
                        onGuardar(nombre, rut, correo, telefono, direccion) { error ->
                            guardado = error == null
                            mensaje = error ?: "Datos actualizados"
                        }
                    }
                ) { Text("Guardar") }
            }

            if (!hayCambios && mensaje == null) {
                Text(
                    "No hay cambios por guardar.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            mensaje?.let {
                Text(
                    it,
                    color = if (guardado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()

            Text("Contraseña", style = MaterialTheme.typography.titleMedium)

            CampoContrasena(
                valor = actual,
                onValorCambia = {
                    actual = it
                    mensajeClave = null
                    claveCambiada = false
                },
                etiqueta = "Contraseña actual",
                visible = clavesVisibles,
                onAlternarVisible = { clavesVisibles = !clavesVisibles },
                modifier = Modifier.fillMaxWidth()
            )
            CampoContrasena(
                valor = nueva,
                onValorCambia = {
                    nueva = it
                    mensajeClave = null
                    claveCambiada = false
                },
                etiqueta = "Contraseña nueva",
                visible = clavesVisibles,
                onAlternarVisible = { clavesVisibles = !clavesVisibles },
                error = if (nueva.isNotEmpty() && nueva.length < 6)
                    "Mínimo 6 caracteres" else null,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = puedeCambiarClave,
                onClick = {
                    onCambiarContrasena(actual, nueva) { error ->
                        claveCambiada = error == null
                        mensajeClave = error ?: "Contraseña actualizada"
                        if (error == null) {
                            actual = ""
                            nueva = ""
                        }
                    }
                }
            ) { Text("Cambiar contraseña") }

            mensajeClave?.let {
                Text(
                    it,
                    color = if (claveCambiada) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun EditarPerfilScreenPreview() {
    SegurosBpoTheme {
        EditarPerfilScreen(
            cuenta = Muestras.asegurada,
            onGuardar = { _, _, _, _, _, listo -> listo(null) },
            onCambiarContrasena = { _, _, listo -> listo(null) },
            onVolver = {}
        )
    }
}
