package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.AsignarRequest
import cl.duocuc.segurosbpo.data.remote.AvanzarRequest
import cl.duocuc.segurosbpo.data.remote.GestionRequest
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
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
