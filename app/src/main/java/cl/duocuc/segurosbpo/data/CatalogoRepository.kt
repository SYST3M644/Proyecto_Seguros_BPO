package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.CatalogoApi
import cl.duocuc.segurosbpo.data.remote.CompaniaRequest
import cl.duocuc.segurosbpo.data.remote.CompaniaResponse
import cl.duocuc.segurosbpo.data.remote.RetrofitClient
import cl.duocuc.segurosbpo.data.remote.TipoSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.TipoSiniestroResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class CatalogoRepository(
    private val catalogoApi: CatalogoApi = RetrofitClient.catalogoApi
) {

    suspend fun listarCompanias(): List<CompaniaResponse> = llamar {
        catalogoApi.listarCompanias()
    }

    suspend fun listarTipos(): List<TipoSiniestroResponse> = llamar {
        catalogoApi.listarTipos()
    }

    suspend fun crearCompania(nombre: String, plazoDiasHabiles: Int): CompaniaResponse = llamar {
        catalogoApi.crearCompania(CompaniaRequest(nombre.trim(), plazoDiasHabiles))
    }

    suspend fun crearTipo(
        nombre: String,
        esVehiculo: Boolean,
        documentos: List<String>
    ): TipoSiniestroResponse = llamar {
        catalogoApi.crearTipo(
            TipoSiniestroRequest(
                nombre.trim(),
                esVehiculo,
                documentos.map { it.trim() }.filter { it.isNotEmpty() }
            )
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
            400 -> "Revisa los datos del catálogo"
            409 -> "Ya existe un registro con ese nombre"
            else -> "El servidor respondió con un error " + error.code()
        }
    }
}
