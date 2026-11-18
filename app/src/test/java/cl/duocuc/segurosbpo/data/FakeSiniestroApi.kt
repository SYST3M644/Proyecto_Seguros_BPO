package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.AsignarRequest
import cl.duocuc.segurosbpo.data.remote.AvanzarRequest
import cl.duocuc.segurosbpo.data.remote.GestionRequest
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.SiniestroApi
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeSiniestroApi : SiniestroApi {

    private val casos = mutableListOf(
        SiniestroResponse(
            id = 1, numero = "SIN-26-004861", aseguradoId = 1,
            aseguradoNombre = "Camila Rojas Fuentes",
            companiaCodigo = "CORDILLERA", companiaNombre = "Seguros Cordillera",
            plazoDiasHabiles = 10, tipoCodigo = "CHOQUE",
            tipoNombre = "Vehículo · choque o colisión",
            fechaOcurrencia = "10-11-2026", fechaReporte = "12-11-2026",
            comuna = "Quilicura", descripcion = "Colisión leve",
            huboTerceros = true, etapa = "EVALUACION",
            liquidadorId = 9, liquidadorNombre = "Maria P. Lagos",
            resultadoCierre = null, mensajeCierre = null,
            ultimaActualizacion = "12-11-2026 10:00"
        ),
        SiniestroResponse(
            id = 2, numero = "SIN-26-004862", aseguradoId = 1,
            aseguradoNombre = "Camila Rojas Fuentes",
            companiaCodigo = "LITORAL", companiaNombre = "Seguros Litoral",
            plazoDiasHabiles = 12, tipoCodigo = "DANO_AGUA",
            tipoNombre = "Hogar · daño por agua",
            fechaOcurrencia = "11-11-2026", fechaReporte = "12-11-2026",
            comuna = "Maipú", descripcion = "Filtración en la cocina",
            huboTerceros = false, etapa = "RECIBIDO",
            liquidadorId = null, liquidadorNombre = null,
            resultadoCierre = null, mensajeCierre = null,
            ultimaActualizacion = "12-11-2026 11:00"
        )
    )

    var sinConexion = false

    override suspend fun listarPorAsegurado(aseguradoId: String): List<SiniestroResponse> {
        if (sinConexion) throw IOException("Sin red")
        return casos.filter { it.aseguradoId == aseguradoId }
    }

    override suspend fun listarPorLiquidador(liquidadorId: String): List<SiniestroResponse> {
        if (sinConexion) throw IOException("Sin red")
        return casos.filter { it.liquidadorId == liquidadorId }
    }

    override suspend fun listarSinAsignar(sinAsignar: Boolean): List<SiniestroResponse> {
        if (sinConexion) throw IOException("Sin red")
        return casos.filter { it.liquidadorId == null && it.etapa == "RECIBIDO" }
    }

    override suspend fun obtener(id: String): SiniestroResponse {
        if (sinConexion) throw IOException("Sin red")
        return casos.firstOrNull { it.id == id } ?: throw error(404, "Siniestro no encontrado")
    }

    override suspend fun registrar(datos: RegistrarSiniestroRequest): SiniestroResponse {
        if (sinConexion) throw IOException("Sin red")
        if (datos.comuna.isBlank() || datos.descripcion.isBlank()) {
            throw error(400, "La comuna y la descripción son obligatorias")
        }
        val nuevo = casos.first().copy(
            id = (casos.size + 1).toString(),
            numero = "SIN-26-00486" + (casos.size + 1),
            aseguradoId = datos.aseguradoId,
            companiaCodigo = datos.companiaCodigo,
            tipoCodigo = datos.tipoCodigo,
            comuna = datos.comuna,
            descripcion = datos.descripcion,
            etapa = "RECIBIDO",
            liquidadorId = null,
            liquidadorNombre = null
        )
        casos.add(nuevo)
        return nuevo
    }

    override suspend fun asignar(id: String, datos: AsignarRequest): SiniestroResponse {
        if (sinConexion) throw IOException("Sin red")
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        val actualizado = casos[indice].copy(
            liquidadorId = datos.liquidadorId,
            liquidadorNombre = "Liquidador " + datos.liquidadorId,
            etapa = "EVALUACION"
        )
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun avanzar(id: String, datos: AvanzarRequest): SiniestroResponse {
        if (sinConexion) throw IOException("Sin red")
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        val etapas = listOf("RECIBIDO", "EVALUACION", "LIQUIDACION", "CERRADO")
        val actual = etapas.indexOf(casos[indice].etapa)
        if (actual == etapas.lastIndex) throw error(400, "El caso ya está cerrado")
        val siguiente = etapas[actual + 1]
        if (siguiente == "CERRADO" && (datos.resultado.isBlank() || datos.mensajeCierre.isBlank())) {
            throw error(400, "Para cerrar indica el resultado y el mensaje de cierre")
        }
        val actualizado = casos[indice].copy(etapa = siguiente)
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun listarGestiones(id: String): List<GestionResponse> {
        if (sinConexion) throw IOException("Sin red")
        return listOf(
            GestionResponse(1, id, "12-11-2026 10:00", "Camila Rojas Fuentes",
                "Reporte registrado", true)
        )
    }

    override suspend fun agregarGestion(id: String, datos: GestionRequest): GestionResponse {
        if (sinConexion) throw IOException("Sin red")
        if (datos.descripcion.isBlank()) {
            throw error(400, "La descripción de la gestión es obligatoria")
        }
        return GestionResponse(
            2, id, "12-11-2026 12:00", datos.responsable,
            datos.descripcion, datos.visibleAsegurado
        )
    }

    private fun error(codigo: Int, mensaje: String): HttpException {
        val cuerpo = """{"message":"$mensaje"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<SiniestroResponse>(codigo, cuerpo))
    }
}
