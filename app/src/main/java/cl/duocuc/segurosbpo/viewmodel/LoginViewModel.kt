package cl.duocuc.segurosbpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.segurosbpo.data.SesionRepository
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import cl.duocuc.segurosbpo.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repositorio: SesionRepository = SesionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGeneral = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, errorGeneral = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onLimpiar() {
        _uiState.value = LoginUiState()
    }

    fun validar(): Boolean {
        val estado = _uiState.value
        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa tu correo"
            !estado.correo.contains("@") -> "El correo no es válido"
            else -> null
        }
        val errorContrasena = if (estado.contrasena.isBlank()) "Ingresa tu contraseña" else null
        _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
        return errorCorreo == null && errorContrasena == null
    }

    fun iniciarSesion(onExito: (UsuarioResponse) -> Unit) {
        if (!validar()) return
        val estado = _uiState.value
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            val resultado = runCatching {
                repositorio.iniciarSesion(estado.correo, estado.contrasena)
            }
            _uiState.update { it.copy(cargando = false) }
            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.value = LoginUiState()
                    onExito(usuario)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorGeneral = error.message ?: "No se pudo iniciar sesión")
                    }
                }
            )
        }
    }
}
