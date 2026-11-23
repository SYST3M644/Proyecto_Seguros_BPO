package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class SiniestroRepositoryTest {

    private lateinit var apiFalsa: FakeSiniestroApi
    private lateinit var repositorio: SiniestroRepository

    @Before
    fun preparar() {
        apiFalsa = FakeSiniestroApi()
        repositorio = SiniestroRepository(siniestroApi = apiFalsa)
    }

    @Test
    fun listarPorAsegurado_traeLosCasosConLosNombresResueltos() = runBlocking {
        val casos = repositorio.listarPorAsegurado("1")

        assertEquals(2, casos.size)
        assertEquals("Seguros Cordillera", casos[0].companiaNombre)
        assertEquals("Vehículo · choque o colisión", casos[0].tipoNombre)
    }

    @Test
    fun asignar_dejaElCasoEnEvaluacionConSuLiquidador() = runBlocking {
        val asignado = repositorio.asignar(id = "2", liquidadorId = "11")

        assertEquals("11", asignado.liquidadorId)
        assertEquals("EVALUACION", asignado.etapa)
    }

    @Test
    fun registrar_creaElCasoEnEtapaRecibidoYSinLiquidador() = runBlocking {
        val nuevo = repositorio.registrar(
            RegistrarSiniestroRequest(
                aseguradoId = "1", companiaCodigo = "CORDILLERA",
                tipoCodigo = "CHOQUE", fechaOcurrencia = "10-11-2026",
                comuna = "Renca", descripcion = "Rayado en el costado",
                huboTerceros = false
            )
        )

        assertEquals("RECIBIDO", nuevo.etapa)
        assertEquals("Renca", nuevo.comuna)
        assertEquals(3, repositorio.listarPorAsegurado("1").size)
    }

    @Test
    fun sinConexion_entregaUnMensajeEntendible() {
        apiFalsa.sinConexion = true

        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.listarPorAsegurado("1") }
        }

        assertEquals("No se pudo conectar con el servidor", error.message)
    }
}
