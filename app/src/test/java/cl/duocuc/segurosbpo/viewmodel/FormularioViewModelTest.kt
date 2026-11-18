package cl.duocuc.segurosbpo.viewmodel

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FormularioViewModelTest {

    private lateinit var viewModel: FormularioViewModel

    @Before
    fun preparar() {
        viewModel = FormularioViewModel()
    }

    @Test
    fun onRegistrar_conFormularioVacio_muestraLosErrores() {
        val resultado = viewModel.onRegistrar()

        assertFalse(resultado)
        val estado = viewModel.uiState.value
        assertNotNull(estado.errorFecha)
        assertNotNull(estado.errorComuna)
        assertNotNull(estado.errorDescripcion)
        assertNotNull(estado.errorDatos)
        assertNull(estado.errorCompania)
    }

    @Test
    fun onRegistrar_conFormularioCompleto_registraYEvitaElDuplicado() {
        viewModel.onFechaCambia("20-10-2026")
        viewModel.onComunaCambia("Santiago")
        viewModel.onDescripcionCambia("Colisión en el estacionamiento")
        viewModel.onDatosCorrectosCambia(true)

        assertTrue(viewModel.onRegistrar())

        viewModel.confirmarRegistro("SIN-26-123456")
        assertEquals("SIN-26-123456", viewModel.uiState.value.numeroSiniestro)
        assertFalse(viewModel.onRegistrar())

        viewModel.onComunaCambia("Maipú")
        assertFalse(viewModel.uiState.value.reporteExitoso)
    }

    @Test
    fun onCompaniaSeleccionada_conCompaniaInactiva_impideRegistrar() {
        viewModel.onFechaCambia("20-10-2026")
        viewModel.onComunaCambia("Maipú")
        viewModel.onDescripcionCambia("Daño por agua en la cocina")
        viewModel.onDatosCorrectosCambia(true)

        viewModel.onCompaniaSeleccionada(Compania.PROTEGE)

        assertFalse(viewModel.onRegistrar())
        assertNotNull(viewModel.uiState.value.errorCompania)

        viewModel.onCompaniaSeleccionada(Compania.CORDILLERA)

        assertNull(viewModel.uiState.value.errorCompania)
        assertTrue(viewModel.onRegistrar())
    }

    @Test
    fun onLimpiar_borraElFormularioPeroMantieneAlAsegurado() {
        viewModel.onAseguradoSeleccionado(Asegurado.JORGE)
        viewModel.onComunaCambia("Renca")

        viewModel.onLimpiar()

        assertEquals(Asegurado.JORGE, viewModel.uiState.value.asegurado)
        assertEquals("", viewModel.uiState.value.comuna)

        viewModel.seleccionarAsegurado(Asegurado.CAMILA)
        assertEquals(Asegurado.CAMILA, viewModel.uiState.value.asegurado)
    }
}
