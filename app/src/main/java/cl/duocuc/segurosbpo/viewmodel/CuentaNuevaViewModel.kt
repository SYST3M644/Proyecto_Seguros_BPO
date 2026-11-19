package cl.duocuc.segurosbpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.segurosbpo.data.SesionRepository
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CuentaNuevaViewModel(
    private val repositorio: SesionRepository = SesionRepository()
) : ViewModel() {

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _pregunta = MutableStateFlow<String?>(null)
    val pregunta = _pregunta.asStateFlow()

    fun limpiarError() {
        _error.value = null
    }

    fun crearCuenta(
        nombre: String,
        correo: String,
        rut: String,
        direccion: String,
        contrasena: String,
        pregunta: String,
        respuesta: String,
        onCreada: (UsuarioResponse) -> Unit
    ) {
        _cargando.value = true
        _error.value = null
        viewModelScope.launch {
            val resultado = runCatching {
                repositorio.registrar(
                    nombre = nombre,
                    correo = correo,
                    contrasena = contrasena,
                    rut = rut,
                    direccion = direccion,
                    pregunta = pregunta,
                    respuesta = respuesta
                )
            }
            _cargando.value = false
            resultado.fold(
                onSuccess = { onCreada(it) },
                onFailure = { _error.value = it.message ?: "No se pudo crear la cuenta" }
            )
        }
    }

    fun buscarPregunta(correo: String) {
        _cargando.value = true
        _error.value = null
        _pregunta.value = null
        viewModelScope.launch {
            val resultado = runCatching { repositorio.preguntaDe(correo) }
            _cargando.value = false
            resultado.fold(
                onSuccess = { _pregunta.value = it.pregunta },
                onFailure = { _error.value = it.message ?: "No se pudo buscar la cuenta" }
            )
        }
    }

    fun recuperar(correo: String, respuesta: String, nueva: String, onListo: () -> Unit) {
        if (nueva.length < 6) {
            _error.value = "La contraseña debe tener mínimo 6 caracteres"
            return
        }
        _cargando.value = true
        _error.value = null
        viewModelScope.launch {
            val resultado = runCatching { repositorio.recuperar(correo, respuesta, nueva) }
            _cargando.value = false
            resultado.fold(
                onSuccess = { onListo() },
                onFailure = { _error.value = it.message ?: "No se pudo cambiar la contraseña" }
            )
        }
    }
}
