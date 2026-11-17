package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.data.remote.ServidorSiniestros
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendRestInstrumentedTest {
    @Test
    fun androidReportaYConsultaAlServidorJava() = runBlocking {
        val asegurado = Asegurado.CAMILA
        val estado = FormularioUiState(
            asegurado = asegurado,
            fechaOcurrencia = "02-09-2026",
            comuna = "Maipú",
            descripcion = "Siniestro ficticio para prueba Android REST"
        )
        val creado = ServidorSiniestros.reportar(estado)
        assertTrue(creado.numero.startsWith("SIN-"))
        assertEquals("Recibido", creado.estado)
        val casos = ServidorSiniestros.listar(asegurado)
        assertTrue(casos.any { it.numero == creado.numero })
        val historial = ServidorSiniestros.historial(asegurado, creado.numero)
        assertTrue(historial.isNotEmpty())
    }
}
