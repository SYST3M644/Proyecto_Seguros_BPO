package cl.duocuc.segurosbpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel()
    }

    @Test
    fun validar_conCamposVacios_devuelveFalseYMuestraErrores() {
        val resultado = viewModel.validar()

        assertFalse(resultado)
        assertEquals("Ingresa tu correo", viewModel.uiState.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun validar_conDatosCompletos_devuelveTrueSinErrores() {
        viewModel.onCorreoCambia("crojas@correo.cl")
        viewModel.onContrasenaCambia("123456")

        val resultado = viewModel.validar()

        assertTrue(resultado)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun validar_conCorreoSinArroba_avisaQueNoEsValido() {
        viewModel.onCorreoCambia("crojas")
        viewModel.onContrasenaCambia("123456")

        assertFalse(viewModel.validar())
        assertEquals("El correo no es válido", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun escribirDespuesDeUnError_borraElMensajeDeError() {
        viewModel.validar()

        viewModel.onCorreoCambia("c")

        assertNull(viewModel.uiState.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }
}
