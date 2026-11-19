package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.AsignarRequest
import cl.duocuc.segurosbpo.data.remote.AvanzarRequest
import cl.duocuc.segurosbpo.data.remote.GestionRequest
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.AvisoResponse
import cl.duocuc.segurosbpo.data.remote.CargaLiquidadorResponse
import cl.duocuc.segurosbpo.data.remote.ConsultaRequest
import cl.duocuc.segurosbpo.data.remote.ConsultaResponse
import cl.duocuc.segurosbpo.data.remote.EvaluacionRequest
import cl.duocuc.segurosbpo.data.remote.EvaluacionResponse
import cl.duocuc.segurosbpo.data.remote.MotivoRequest
import cl.duocuc.segurosbpo.data.remote.SolicitudRequest
import cl.duocuc.segurosbpo.data.remote.SolicitudResponse
import cl.duocuc.segurosbpo.data.remote.RetrofitClient
import cl.duocuc.segurosbpo.data.remote.SiniestroApi
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class SiniestroRepository(
    private val siniestroApi: SiniestroApi = RetrofitClient.siniestroApi
) {

    suspend fun listarPorAsegurado(aseguradoId: String): List<SiniestroResponse> =
        llamar { siniestroApi.listarPorAsegurado(aseguradoId) }

    suspend fun listarPorLiquidador(liquidadorId: String): List<SiniestroResponse> =
        llamar { siniestroApi.listarPorLiquidador(liquidadorId) }

    suspend fun listarSinAsignar(): List<SiniestroResponse> =
        llamar { siniestroApi.listarSinAsignar() }

    suspend fun obtener(id: String): SiniestroResponse =
        llamar { siniestroApi.obtener(id) }

    suspend fun registrar(datos: RegistrarSiniestroRequest): SiniestroResponse =
        llamar { siniestroApi.registrar(datos) }

    suspend fun asignar(id: String, liquidadorId: String): SiniestroResponse =
        llamar { siniestroApi.asignar(id, AsignarRequest(liquidadorId)) }

    suspend fun avanzar(id: String, resultado: String, mensajeCierre: String): SiniestroResponse =
        llamar { siniestroApi.avanzar(id, AvanzarRequest(resultado, mensajeCierre)) }

    suspend fun listarGestiones(id: String): List<GestionResponse> =
        llamar { siniestroApi.listarGestiones(id) }

    suspend fun agregarGestion(
        id: String,
        responsable: String,
        descripcion: String,
        visibleAsegurado: Boolean
    ): GestionResponse =
        llamar {
            siniestroApi.agregarGestion(
                id, GestionRequest(responsable, descripcion, visibleAsegurado)
            )
        }

    suspend fun listarCarga(): List<CargaLiquidadorResponse> = llamar {
        siniestroApi.listarCarga()
    }

    suspend fun listarAvisos(usuarioId: String): List<AvisoResponse> = llamar {
        siniestroApi.listarAvisos(usuarioId)
    }

    suspend fun avisosSinLeer(usuarioId: String): Long = llamar {
        siniestroApi.avisosSinLeer(usuarioId)
    }

    suspend fun marcarAvisoLeido(id: String): AvisoResponse = llamar {
        siniestroApi.marcarAvisoLeido(id)
    }

    suspend fun listarSolicitudes(siniestroId: String): List<SolicitudResponse> = llamar {
        siniestroApi.listarSolicitudes(siniestroId)
    }

    suspend fun solicitar(
        siniestroId: String,
        tipoDocumento: String,
        comentario: String,
        solicitadoPor: String
    ): SolicitudResponse = llamar {
        siniestroApi.solicitar(
            siniestroId,
            SolicitudRequest(tipoDocumento.trim(), comentario.trim(), solicitadoPor)
        )
    }

    suspend fun marcarSolicitudRecibida(id: String): SolicitudResponse = llamar {
        siniestroApi.marcarSolicitudRecibida(id)
    }

    suspend fun anularSolicitud(id: String): SolicitudResponse = llamar {
        siniestroApi.anularSolicitud(id)
    }

    suspend fun listarConsultas(siniestroId: String): List<ConsultaResponse> = llamar {
        siniestroApi.listarConsultas(siniestroId)
    }

    suspend fun preguntar(siniestroId: String, texto: String): ConsultaResponse = llamar {
        siniestroApi.preguntar(siniestroId, ConsultaRequest(texto.trim()))
    }

    suspend fun responderConsulta(id: String, texto: String): ConsultaResponse = llamar {
        siniestroApi.responderConsulta(id, ConsultaRequest(texto.trim()))
    }

    suspend fun obtenerEvaluacion(siniestroId: String): EvaluacionResponse = llamar {
        siniestroApi.obtenerEvaluacion(siniestroId)
    }

    suspend fun evaluar(siniestroId: String, nota: Int, comentario: String): EvaluacionResponse =
        llamar { siniestroApi.evaluar(siniestroId, EvaluacionRequest(nota, comentario.trim())) }

    suspend fun listarDisponibles(liquidadorId: String): List<SiniestroResponse> = llamar {
        siniestroApi.listarDisponibles(liquidadorId)
    }

    suspend fun tomar(id: String, liquidadorId: String): SiniestroResponse = llamar {
        siniestroApi.tomar(id, liquidadorId)
    }

    suspend fun listarArchivados(liquidadorId: String): List<SiniestroResponse> = llamar {
        siniestroApi.listarArchivados(liquidadorId)
    }

    suspend fun archivar(id: String): SiniestroResponse = llamar { siniestroApi.archivar(id) }

    suspend fun restaurar(id: String): SiniestroResponse = llamar { siniestroApi.restaurar(id) }

    suspend fun desistir(id: String, motivo: String): SiniestroResponse = llamar {
        siniestroApi.desistir(id, MotivoRequest(motivo.trim()))
    }

    suspend fun actualizarDescripcion(id: String, descripcion: String): SiniestroResponse =
        llamar { siniestroApi.actualizarDescripcion(id, MotivoRequest(descripcion.trim())) }

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        withContext(Dispatchers.IO) { peticion() }
    } catch (e: HttpException) {
        throw Exception(mensajeDelServidor(e, e.response()?.errorBody()?.string()))
    } catch (e: IOException) {
        throw Exception("No se pudo conectar con el servidor")
    }

    private fun mensajeDelServidor(error: HttpException, cuerpo: String?): String {
        val mensaje = cuerpo?.let {
            runCatching { JSONObject(it).optString("message") }.getOrNull()
        }
        if (!mensaje.isNullOrBlank()) return mensaje
        return when (error.code()) {
            400 -> "Revisa los datos del siniestro"
            404 -> "No encontramos ese siniestro"
            503 -> "Un servicio necesario no está disponible"
            else -> "El servidor respondió con un error " + error.code()
        }
    }
}
