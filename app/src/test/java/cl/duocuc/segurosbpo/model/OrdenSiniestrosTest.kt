package cl.duocuc.segurosbpo.model

import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdenSiniestrosTest {

    private fun caso(numero: String, etapa: String, actualizacion: String) = SiniestroResponse(
        id = numero,
        numero = numero,
        aseguradoId = "1",
        aseguradoNombre = "Camila Rojas",
        companiaCodigo = "CORDILLERA",
        companiaNombre = "Cordillera",
        plazoDiasHabiles = 10,
        tipoCodigo = "CHOQUE",
        tipoNombre = "Choque",
        fechaOcurrencia = "10-11-2026",
        fechaReporte = "12-11-2026",
        comuna = "Quilicura",
        descripcion = "Colisión leve",
        huboTerceros = false,
        etapa = etapa,
        liquidadorId = "9",
        liquidadorNombre = "Pedro Rivas",
        resultadoCierre = null,
        mensajeCierre = null,
        ultimaActualizacion = actualizacion
    )

    @Test
    fun `diciembre queda antes que enero aunque como texto sea al reves`() {
        val enero = caso("A", "RECIBIDO", "15-01-2026 09:00")
        val diciembre = caso("B", "RECIBIDO", "02-12-2026 09:00")

        val orden = OrdenSiniestros.paraLiquidador(listOf(enero, diciembre))

        assertEquals(listOf("B", "A"), orden.map { it.numero })
        assertTrue(OrdenSiniestros.clave("02-12-2026 09:00") > OrdenSiniestros.clave("15-01-2026 09:00"))
    }

    @Test
    fun `los cerrados quedan al final aunque sean los mas recientes`() {
        val cerrado = caso("CERRADO", "CERRADO", "20-11-2026 18:00")
        val abierto = caso("ABIERTO", "LIQUIDACION", "01-03-2026 08:00")

        val orden = OrdenSiniestros.paraLiquidador(listOf(cerrado, abierto))

        assertEquals(listOf("ABIERTO", "CERRADO"), orden.map { it.numero })
    }

    @Test
    fun `entre abiertos manda la actividad mas reciente`() {
        val viejo = caso("VIEJO", "RECIBIDO", "01-11-2026 10:00")
        val nuevo = caso("NUEVO", "LIQUIDACION", "18-11-2026 07:30")
        val medio = caso("MEDIO", "EVALUACION", "10-11-2026 23:59")

        val orden = OrdenSiniestros.paraLiquidador(listOf(viejo, nuevo, medio))

        assertEquals(listOf("NUEVO", "MEDIO", "VIEJO"), orden.map { it.numero })
    }

    @Test
    fun `una fecha vacia o rara no revienta el orden`() {
        val sinFecha = caso("SIN", "RECIBIDO", "")
        val rara = caso("RARA", "RECIBIDO", "no es fecha")
        val buena = caso("BUENA", "RECIBIDO", "05-11-2026 12:00")

        val orden = OrdenSiniestros.paraLiquidador(listOf(sinFecha, rara, buena))

        assertEquals("BUENA", orden.first().numero)
        assertEquals(3, orden.size)
        assertEquals(0L, OrdenSiniestros.clave(""))
    }
}
