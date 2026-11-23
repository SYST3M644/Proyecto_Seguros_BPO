package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.AsignarRequest
import cl.duocuc.segurosbpo.data.remote.AvanzarRequest
import cl.duocuc.segurosbpo.data.remote.AvisoResponse
import cl.duocuc.segurosbpo.data.remote.CargaLiquidadorResponse
import cl.duocuc.segurosbpo.data.remote.ConsultaRequest
import cl.duocuc.segurosbpo.data.remote.ConsultaResponse
import cl.duocuc.segurosbpo.data.remote.EvaluacionRequest
import cl.duocuc.segurosbpo.data.remote.EvaluacionResponse
import cl.duocuc.segurosbpo.data.remote.GestionRequest
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.MotivoRequest
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.SiniestroApi
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.data.remote.SolicitudRequest
import cl.duocuc.segurosbpo.data.remote.SolicitudResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeSiniestroApi : SiniestroApi {

    private val casos = mutableListOf(
        SiniestroResponse(
            id = "1", numero = "SIN-26-004861", aseguradoId = "1",
            aseguradoNombre = "Camila Rojas Fuentes",
            companiaCodigo = "CORDILLERA", companiaNombre = "Seguros Cordillera",
            plazoDiasHabiles = 10, tipoCodigo = "CHOQUE",
            tipoNombre = "Vehículo · choque o colisión",
            fechaOcurrencia = "10-11-2026", fechaReporte = "12-11-2026",
            comuna = "Quilicura", descripcion = "Colisión leve",
            huboTerceros = true, etapa = "EVALUACION",
            liquidadorId = "9", liquidadorNombre = "Maria P. Lagos",
            resultadoCierre = null, mensajeCierre = null,
            ultimaActualizacion = "12-11-2026 10:00"
        ),
        SiniestroResponse(
            id = "2", numero = "SIN-26-004862", aseguradoId = "1",
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

    private val avisos = mutableListOf(
        AvisoResponse(
            id = "1", siniestroId = "1", numero = "SIN-26-004861",
            destinatarioId = "1", mensaje = "Tu caso pasó a evaluación",
            fecha = "12-11-2026 10:05", leido = false
        )
    )

    private val solicitudes = mutableListOf<SolicitudResponse>()
    private val consultas = mutableListOf<ConsultaResponse>()
    private val evaluaciones = mutableListOf<EvaluacionResponse>()
    private val gestiones = mutableListOf(
        GestionResponse(
            id = "1", siniestroId = "1", fecha = "12-11-2026 10:00",
            responsable = "Camila Rojas Fuentes",
            descripcion = "Reporte registrado", visibleAsegurado = true
        )
    )
    private val archivados = mutableListOf<String>()

    var sinConexion = false

    override suspend fun listarPorAsegurado(aseguradoId: String): List<SiniestroResponse> {
        red()
        return casos.filter { it.aseguradoId == aseguradoId }
    }

    override suspend fun listarPorLiquidador(liquidadorId: String): List<SiniestroResponse> {
        red()
        return casos.filter { it.liquidadorId == liquidadorId }
    }

    override suspend fun listarSinAsignar(sinAsignar: Boolean): List<SiniestroResponse> {
        red()
        return casos.filter { it.liquidadorId == null && it.etapa == "RECIBIDO" }
    }

    override suspend fun listarAvisos(usuarioId: String): List<AvisoResponse> {
        red()
        return avisos.filter { it.destinatarioId == usuarioId }
    }

    override suspend fun avisosSinLeer(usuarioId: String): Long {
        red()
        return avisos.count { it.destinatarioId == usuarioId && !it.leido }.toLong()
    }

    override suspend fun marcarAvisoLeido(id: String): AvisoResponse {
        red()
        val indice = avisos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Aviso no encontrado")
        val actualizado = avisos[indice].copy(leido = true)
        avisos[indice] = actualizado
        return actualizado
    }

    override suspend fun listarSolicitudes(siniestroId: String): List<SolicitudResponse> {
        red()
        return solicitudes.filter { it.siniestroId == siniestroId }
    }

    override suspend fun solicitar(
        siniestroId: String,
        datos: SolicitudRequest
    ): SolicitudResponse {
        red()
        if (datos.tipoDocumento.isBlank()) {
            throw error(400, "Indica qué documento necesitas")
        }
        val nueva = SolicitudResponse(
            id = (solicitudes.size + 1).toString(),
            siniestroId = siniestroId,
            tipoDocumento = datos.tipoDocumento,
            comentario = datos.comentario,
            solicitadoPor = datos.solicitadoPor,
            fecha = "12-11-2026 13:00",
            estado = "PENDIENTE"
        )
        solicitudes.add(nueva)
        return nueva
    }

    override suspend fun marcarSolicitudRecibida(id: String): SolicitudResponse =
        cambiarSolicitud(id, "RECIBIDA")

    override suspend fun anularSolicitud(id: String): SolicitudResponse =
        cambiarSolicitud(id, "ANULADA")

    override suspend fun listarConsultas(siniestroId: String): List<ConsultaResponse> {
        red()
        return consultas.filter { it.siniestroId == siniestroId }
    }

    override suspend fun preguntar(
        siniestroId: String,
        datos: ConsultaRequest
    ): ConsultaResponse {
        red()
        if (datos.texto.isBlank()) throw error(400, "Escribe tu consulta")
        val nueva = ConsultaResponse(
            id = (consultas.size + 1).toString(),
            siniestroId = siniestroId,
            pregunta = datos.texto,
            respuesta = null,
            fechaPregunta = "12-11-2026 14:00",
            fechaRespuesta = null
        )
        consultas.add(nueva)
        return nueva
    }

    override suspend fun responderConsulta(
        id: String,
        datos: ConsultaRequest
    ): ConsultaResponse {
        red()
        val indice = consultas.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Consulta no encontrada")
        if (datos.texto.isBlank()) throw error(400, "Escribe la respuesta")
        val actualizada = consultas[indice].copy(
            respuesta = datos.texto,
            fechaRespuesta = "12-11-2026 15:00"
        )
        consultas[indice] = actualizada
        return actualizada
    }

    override suspend fun obtenerEvaluacion(siniestroId: String): EvaluacionResponse {
        red()
        return evaluaciones.firstOrNull { it.siniestroId == siniestroId }
            ?: throw error(404, "Este caso todavía no tiene evaluación")
    }

    override suspend fun evaluar(
        siniestroId: String,
        datos: EvaluacionRequest
    ): EvaluacionResponse {
        red()
        if (datos.nota !in 1..5) throw error(400, "La nota va de 1 a 5")
        val nueva = EvaluacionResponse(
            id = (evaluaciones.size + 1).toString(),
            siniestroId = siniestroId,
            nota = datos.nota,
            comentario = datos.comentario,
            fecha = "12-11-2026 16:00"
        )
        evaluaciones.removeAll { it.siniestroId == siniestroId }
        evaluaciones.add(nueva)
        return nueva
    }

    override suspend fun listarDisponibles(liquidadorId: String): List<SiniestroResponse> {
        red()
        return casos.filter { it.liquidadorId == null && it.etapa == "RECIBIDO" }
    }

    override suspend fun tomar(id: String, liquidadorId: String): SiniestroResponse {
        red()
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        if (casos[indice].liquidadorId != null) throw error(409, "Otro liquidador ya lo tomó")
        val actualizado = casos[indice].copy(
            liquidadorId = liquidadorId,
            liquidadorNombre = "Liquidador " + liquidadorId,
            etapa = "EVALUACION"
        )
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun listarArchivados(liquidadorId: String): List<SiniestroResponse> {
        red()
        return casos.filter { it.liquidadorId == liquidadorId && archivados.contains(it.id) }
    }

    override suspend fun archivar(id: String): SiniestroResponse {
        red()
        val caso = casos.firstOrNull { it.id == id } ?: throw error(404, "Siniestro no encontrado")
        if (!archivados.contains(id)) archivados.add(id)
        return caso
    }

    override suspend fun restaurar(id: String): SiniestroResponse {
        red()
        val caso = casos.firstOrNull { it.id == id } ?: throw error(404, "Siniestro no encontrado")
        archivados.remove(id)
        return caso
    }

    override suspend fun desistir(id: String, datos: MotivoRequest): SiniestroResponse {
        red()
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        if (datos.texto.isBlank()) throw error(400, "Indica el motivo")
        val actualizado = casos[indice].copy(
            etapa = "CERRADO",
            resultadoCierre = "Desistido",
            mensajeCierre = datos.texto
        )
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun actualizarDescripcion(
        id: String,
        datos: MotivoRequest
    ): SiniestroResponse {
        red()
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        if (datos.texto.isBlank()) throw error(400, "La descripción es obligatoria")
        val actualizado = casos[indice].copy(descripcion = datos.texto)
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun listarCarga(): List<CargaLiquidadorResponse> {
        red()
        return casos.mapNotNull { it.liquidadorId }.distinct().map { liquidadorId ->
            val suyos = casos.filter { it.liquidadorId == liquidadorId }
            CargaLiquidadorResponse(
                liquidadorId = liquidadorId,
                liquidadorNombre = suyos.first().liquidadorNombre.orEmpty(),
                equipo = "VEHICULOS",
                abiertos = suyos.count { it.etapa != "CERRADO" },
                cerrados = suyos.count { it.etapa == "CERRADO" }
            )
        }
    }

    override suspend fun obtener(id: String): SiniestroResponse {
        red()
        return casos.firstOrNull { it.id == id } ?: throw error(404, "Siniestro no encontrado")
    }

    override suspend fun registrar(datos: RegistrarSiniestroRequest): SiniestroResponse {
        red()
        if (datos.comuna.isBlank() || datos.descripcion.isBlank()) {
            throw error(400, "La comuna y la descripción son obligatorias")
        }
        val nuevo = casos.first().copy(
            id = (casos.size + 1).toString(),
            numero = "SIN-26-00486" + (casos.size + 1),
            aseguradoId = datos.aseguradoId,
            companiaCodigo = datos.companiaCodigo,
            tipoCodigo = datos.tipoCodigo,
            fechaOcurrencia = datos.fechaOcurrencia,
            comuna = datos.comuna,
            descripcion = datos.descripcion,
            huboTerceros = datos.huboTerceros,
            etapa = "RECIBIDO",
            liquidadorId = null,
            liquidadorNombre = null
        )
        casos.add(nuevo)
        return nuevo
    }

    override suspend fun asignar(id: String, datos: AsignarRequest): SiniestroResponse {
        red()
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
        red()
        val indice = casos.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Siniestro no encontrado")
        val etapas = listOf("RECIBIDO", "EVALUACION", "LIQUIDACION", "CERRADO")
        val actual = etapas.indexOf(casos[indice].etapa)
        if (actual == etapas.lastIndex) throw error(400, "El caso ya está cerrado")
        val siguiente = etapas[actual + 1]
        if (siguiente == "CERRADO" &&
            (datos.resultado.isBlank() || datos.mensajeCierre.isBlank())
        ) {
            throw error(400, "Para cerrar indica el resultado y el mensaje de cierre")
        }
        val actualizado = casos[indice].copy(
            etapa = siguiente,
            resultadoCierre = if (siguiente == "CERRADO") datos.resultado else null,
            mensajeCierre = if (siguiente == "CERRADO") datos.mensajeCierre else null
        )
        casos[indice] = actualizado
        return actualizado
    }

    override suspend fun listarGestiones(id: String): List<GestionResponse> {
        red()
        return gestiones.filter { it.siniestroId == id }
    }

    override suspend fun agregarGestion(id: String, datos: GestionRequest): GestionResponse {
        red()
        if (datos.descripcion.isBlank()) {
            throw error(400, "La descripción de la gestión es obligatoria")
        }
        val nueva = GestionResponse(
            id = (gestiones.size + 1).toString(),
            siniestroId = id,
            fecha = "12-11-2026 12:00",
            responsable = datos.responsable,
            descripcion = datos.descripcion,
            visibleAsegurado = datos.visibleAsegurado
        )
        gestiones.add(nueva)
        return nueva
    }

    private fun cambiarSolicitud(id: String, estado: String): SolicitudResponse {
        red()
        val indice = solicitudes.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Solicitud no encontrada")
        val actualizada = solicitudes[indice].copy(estado = estado)
        solicitudes[indice] = actualizada
        return actualizada
    }

    private fun red() {
        if (sinConexion) throw IOException("Sin red")
    }

    private fun error(codigo: Int, mensaje: String): HttpException {
        val cuerpo = """{"message":"$mensaje"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<SiniestroResponse>(codigo, cuerpo))
    }
}
