package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.AvatarApi
import cl.duocuc.segurosbpo.data.remote.EvidenciaApi
import cl.duocuc.segurosbpo.data.remote.EvidenciaResponse
import cl.duocuc.segurosbpo.data.remote.RetrofitClient
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class ArchivoRepository(
    private val avatarApi: AvatarApi = RetrofitClient.avatarApi,
    private val evidenciaApi: EvidenciaApi = RetrofitClient.evidenciaApi
) {

    suspend fun subirAvatar(usuarioId: String, imagen: ByteArray): UsuarioResponse = llamar {
        avatarApi.subir(usuarioId, parteDeArchivo("foto_perfil.jpg", "image/jpeg", imagen))
    }

    suspend fun listarEvidencias(siniestroId: String): List<EvidenciaResponse> = llamar {
        evidenciaApi.listar(siniestroId)
    }

    suspend fun subirEvidencia(
        siniestroId: String,
        tipoDocumento: String,
        subidoPor: String,
        nombreArchivo: String,
        mime: String,
        contenido: ByteArray
    ): EvidenciaResponse = llamar {
        evidenciaApi.subir(
            siniestroId,
            texto(tipoDocumento),
            texto(subidoPor),
            parteDeArchivo(nombreArchivo, mime, contenido)
        )
    }

    suspend fun eliminarEvidencia(evidenciaId: String) = llamar {
        evidenciaApi.eliminar(evidenciaId)
    }

    private fun texto(valor: String) = valor.toRequestBody("text/plain".toMediaType())

    private fun parteDeArchivo(nombre: String, mime: String, bytes: ByteArray) =
        MultipartBody.Part.createFormData(
            "archivo", nombre, bytes.toRequestBody(mime.toMediaType())
        )

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
            400 -> "El archivo no se puede subir"
            404 -> "No encontramos el caso o el archivo"
            else -> "El servidor respondió con un error " + error.code()
        }
    }
}
