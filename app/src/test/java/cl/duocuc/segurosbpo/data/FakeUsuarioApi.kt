package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.LoginRequest
import cl.duocuc.segurosbpo.data.remote.RolApi
import cl.duocuc.segurosbpo.data.remote.RolResponse
import cl.duocuc.segurosbpo.data.remote.UsuarioApi
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeUsuarioApi : UsuarioApi {

    private val usuarios = listOf(
        UsuarioResponse(
            1, "Camila Rojas Fuentes", "crojas@correo.cl", "15.842.371-5",
            null, 1, "Asegurado", null
        ),
        UsuarioResponse(
            9, "Maria P. Lagos", "mlagos@segurosbpo.cl", "",
            "VEHICULOS", 2, "Liquidador", null
        ),
        UsuarioResponse(
            13, "Paulina G. Herrera", "asignaciones@segurosbpo.cl", "",
            null, 3, "Analista", null
        )
    )

    var sinConexion = false
    var ultimoBuscar: String? = "sin llamar"

    override suspend fun listar(buscar: String?): List<UsuarioResponse> {
        if (sinConexion) throw IOException("Sin red")
        ultimoBuscar = buscar
        if (buscar.isNullOrBlank()) return usuarios
        return usuarios.filter { it.nombre.contains(buscar, ignoreCase = true) }
    }

    override suspend fun obtener(id: String): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        return usuarios.firstOrNull { it.id == id } ?: throw error(404, "Usuario no encontrado")
    }

    override suspend fun iniciarSesion(datos: LoginRequest): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        val usuario = usuarios.firstOrNull { it.correo.equals(datos.correo, ignoreCase = true) }
        if (usuario == null || datos.contrasena != "123456") {
            throw error(401, "El correo o la contraseña no son correctos")
        }
        return usuario
    }

    private fun error(codigo: Int, mensaje: String): HttpException {
        val cuerpo = """{"message":"$mensaje"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<UsuarioResponse>(codigo, cuerpo))
    }
}

class FakeRolApi : RolApi {

    var sinConexion = false

    override suspend fun listar(): List<RolResponse> {
        if (sinConexion) throw IOException("Sin red")
        return listOf(
            RolResponse(1, "Asegurado"),
            RolResponse(2, "Liquidador"),
            RolResponse(3, "Analista")
        )
    }
}
