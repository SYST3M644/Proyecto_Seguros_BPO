package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.ActualizarUsuarioRequest
import cl.duocuc.segurosbpo.data.remote.CambiarContrasenaRequest
import cl.duocuc.segurosbpo.data.remote.LoginRequest
import cl.duocuc.segurosbpo.data.remote.PreguntaResponse
import cl.duocuc.segurosbpo.data.remote.RecuperarRequest
import cl.duocuc.segurosbpo.data.remote.RegistroRequest
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

    suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        rut: String,
        equipo: String? = null,
        rolId: String? = null,
        pregunta: String? = null,
        respuesta: String? = null
    ): UsuarioResponse = llamar {
        usuarioApi.registrar(
            RegistroRequest(
                nombre.trim(), correo.trim(), contrasena, rut.trim(),
                equipo, rolId, pregunta, respuesta
            )
        )
    }

    suspend fun preguntaDe(correo: String): PreguntaResponse = llamar {
        usuarioApi.pregunta(correo.trim())
    }

    suspend fun recuperar(correo: String, respuesta: String, nueva: String) = llamar {
        usuarioApi.recuperar(RecuperarRequest(correo.trim(), respuesta, nueva))
    }

    suspend fun actualizarPerfil(
        id: String,
        nombre: String,
        correo: String,
        rut: String,
        equipo: String? = null,
        rolId: String? = null
    ): UsuarioResponse = llamar {
        usuarioApi.actualizar(
            id,
            ActualizarUsuarioRequest(nombre.trim(), correo.trim(), rut.trim(), equipo, rolId)
        )
    }

    suspend fun cambiarContrasena(id: String, actual: String, nueva: String) = llamar {
        usuarioApi.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva))
    }

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
    "2" -> RolCuenta.LIQUIDADOR
    "3" -> RolCuenta.ANALISTA
    else -> RolCuenta.ASEGURADO
}
