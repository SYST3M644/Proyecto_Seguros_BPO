package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.LoginRequest
import cl.duocuc.segurosbpo.data.remote.RetrofitClient
import cl.duocuc.segurosbpo.data.remote.RolApi
import cl.duocuc.segurosbpo.data.remote.UsuarioApi
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import cl.duocuc.segurosbpo.model.RolCuenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class SesionRepository(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi,
    private val rolApi: RolApi = RetrofitClient.rolApi
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioResponse =
        llamar { usuarioApi.iniciarSesion(LoginRequest(correo.trim(), contrasena)) }

    suspend fun listarUsuarios(buscar: String = ""): List<UsuarioResponse> =
        llamar { usuarioApi.listar(buscar.ifBlank { null }) }

    suspend fun listarRoles(): List<String> =
        llamar { rolApi.listar() }.map { it.nombre }

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
            401 -> "El correo o la contraseña no son correctos"
            404 -> "No encontramos lo que buscabas"
            503 -> "Un servicio necesario no está disponible"
            else -> "El servidor respondió con un error " + error.code()
        }
    }
}

fun UsuarioResponse.rolDeCuenta(): RolCuenta = when (rolId) {
    2L -> RolCuenta.LIQUIDADOR
    3L -> RolCuenta.ANALISTA
    else -> RolCuenta.ASEGURADO
}
