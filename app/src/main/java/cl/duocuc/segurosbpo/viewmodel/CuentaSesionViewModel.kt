package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.segurosbpo.data.ArchivoRepository
import cl.duocuc.segurosbpo.data.SesionRepository
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CuentaSesionViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val ROL_LIQUIDADOR = "2"
        const val CLAVE_INICIAL = "123456"
        const val ACENTOS = "áéíóúñü"
        const val ACENTOS_SIN = "aeiounu"
    }

    private val archivos = ArchivoRepository()
    private val sesion = SesionRepository()

    private val _usuarios = MutableStateFlow<List<CuentaLocal>>(emptyList())
    val usuarios = _usuarios.asStateFlow()

    private val _cuenta = MutableStateFlow<CuentaLocal?>(null)
    val cuenta = _cuenta.asStateFlow()

    fun iniciarSesionRemota(usuario: UsuarioResponse) {
        _cuenta.value = aCuenta(usuario)
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            runCatching { sesion.listarUsuarios() }
                .onSuccess { lista -> _usuarios.value = lista.map { aCuenta(it) } }
        }
    }

    fun actualizarContacto(correo: String, telefono: String) {
        _cuenta.value = _cuenta.value?.copy(correo = correo, telefono = telefono)
    }

    fun guardarPerfilEnServidor(
        nombre: String,
        correo: String,
        rut: String,
        onListo: (String?) -> Unit
    ) {
        val actual = _cuenta.value
        val id = actual?.id?.removePrefix("API_")
        if (actual == null || id.isNullOrBlank()) {
            onListo("Inicia sesión para editar tu perfil")
            return
        }
        viewModelScope.launch {
            runCatching { sesion.actualizarPerfil(id, nombre, correo, rut, actual.equipo) }.fold(
                onSuccess = { usuario ->
                    _cuenta.value = actual.copy(
                        nombre = usuario.nombre,
                        correo = usuario.correo,
                        rut = usuario.rut.orEmpty()
                    )
                    onListo(null)
                },
                onFailure = { onListo(it.message ?: "No se pudo guardar el perfil") }
            )
        }
    }

    fun cambiarContrasena(actual: String, nueva: String, onListo: (String?) -> Unit) {
        val id = _cuenta.value?.id?.removePrefix("API_")
        if (id.isNullOrBlank()) {
            onListo("Inicia sesión para cambiar tu contraseña")
            return
        }
        if (nueva.length < 6) {
            onListo("La nueva contraseña debe tener mínimo 6 caracteres")
            return
        }
        viewModelScope.launch {
            val resultado = runCatching { sesion.cambiarContrasena(id, actual, nueva) }
            onListo(resultado.exceptionOrNull()?.message)
        }
    }

    fun subirFotoAlServidor(bitmap: Bitmap, onListo: (String?) -> Unit) {
        val actual = _cuenta.value
        val id = actual?.id?.removePrefix("API_")
        if (actual == null || id.isNullOrBlank()) {
            onListo("Inicia sesión para cambiar tu foto")
            return
        }
        val salida = ByteArrayOutputStream()
        if (!runCatching { bitmap.compress(Bitmap.CompressFormat.JPEG, 82, salida) }
                .getOrDefault(false)
        ) {
            onListo("No se pudo procesar la fotografía")
            return
        }
        viewModelScope.launch {
            runCatching { archivos.subirAvatar(id, salida.toByteArray()) }.fold(
                onSuccess = { usuario ->
                    _cuenta.value = actual.copy(avatarUrl = usuario.avatarUrl, foto = null)
                    onListo(null)
                },
                onFailure = { onListo(it.message ?: "No se pudo subir la foto") }
            )
        }
    }

    fun crearLiquidadorEnServidor(
        nombre: String,
        equipo: Liquidador,
        telefono: String,
        onListo: (String?, String) -> Unit
    ) {
        val correo = correoCorporativo(nombre)
        viewModelScope.launch {
            runCatching {
                sesion.registrar(nombre, correo, CLAVE_INICIAL, "", equipo.name, ROL_LIQUIDADOR)
            }.fold(
                onSuccess = {
                    cargarUsuarios()
                    onListo(null, "$correo / $CLAVE_INICIAL")
                },
                onFailure = { onListo(it.message ?: "No se pudo crear el liquidador", "") }
            )
        }
    }

    fun cerrarSesion() {
        _cuenta.value = null
        _usuarios.value = emptyList()
    }

    private fun aCuenta(usuario: UsuarioResponse) = CuentaLocal(
        id = "API_" + usuario.id,
        nombre = usuario.nombre,
        rut = usuario.rut.orEmpty(),
        correo = usuario.correo,
        telefono = "",
        rol = when (usuario.rolId) {
            "2" -> RolCuenta.LIQUIDADOR
            "3" -> RolCuenta.ANALISTA
            else -> RolCuenta.ASEGURADO
        },
        equipo = usuario.equipo,
        avatarUrl = usuario.avatarUrl
    )

    private fun correoCorporativo(nombre: String): String {
        val partes = nombre.trim().lowercase().split(" ").filter { it.isNotBlank() }
        val inicial = partes.firstOrNull()?.take(1).orEmpty()
        val apellido = partes.lastOrNull().orEmpty()
        val limpio = (inicial + apellido)
            .map { if (it in ACENTOS) ACENTOS_SIN[ACENTOS.indexOf(it)] else it }
            .joinToString("")
            .filter { it.isLetterOrDigit() }
        return "$limpio@segurosbpo.cl"
    }
}
