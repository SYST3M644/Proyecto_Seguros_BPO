package cl.duocuc.segurosbpo.data

import cl.duocuc.segurosbpo.data.remote.ActualizarUsuarioRequest
import cl.duocuc.segurosbpo.data.remote.CambiarContrasenaRequest
import cl.duocuc.segurosbpo.data.remote.LoginRequest
import cl.duocuc.segurosbpo.data.remote.LoginResponse
import cl.duocuc.segurosbpo.data.remote.PreguntaResponse
import cl.duocuc.segurosbpo.data.remote.RecuperarRequest
import cl.duocuc.segurosbpo.data.remote.RegistroRequest
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

    private val usuarios = mutableListOf(
        UsuarioResponse(
            id = "1", nombre = "Camila Rojas Fuentes", correo = "crojas@correo.cl",
            rut = "15.842.371-5", direccion = "Los Olmos 123, Quilicura", equipo = null,
            rolId = "1", rolNombre = "Asegurado", estado = "APROBADO", avatarUrl = null
        ),
        UsuarioResponse(
            id = "9", nombre = "Maria P. Lagos", correo = "mlagos@segurosbpo.cl",
            rut = "", direccion = null, equipo = "VEHICULOS",
            rolId = "2", rolNombre = "Liquidador", estado = "APROBADO", avatarUrl = null
        ),
        UsuarioResponse(
            id = "13", nombre = "Paulina G. Herrera", correo = "asignaciones@segurosbpo.cl",
            rut = "", direccion = null, equipo = null,
            rolId = "3", rolNombre = "Analista", estado = "APROBADO", avatarUrl = null
        ),
        UsuarioResponse(
            id = "21", nombre = "Nicolas Vera Soto", correo = "nvera@correo.cl",
            rut = "19.233.108-4", direccion = null, equipo = null,
            rolId = "1", rolNombre = "Asegurado", estado = "PENDIENTE", avatarUrl = null
        )
    )

    var sinConexion = false
    var ultimoBuscar: String? = "sin llamar"
    var contrasenaCambiada: CambiarContrasenaRequest? = null
    var recuperacion: RecuperarRequest? = null

    override suspend fun listar(buscar: String?): List<UsuarioResponse> {
        if (sinConexion) throw IOException("Sin red")
        ultimoBuscar = buscar
        if (buscar.isNullOrBlank()) return usuarios
        return usuarios.filter { it.nombre.contains(buscar, ignoreCase = true) }
    }

    override suspend fun pendientes(): List<UsuarioResponse> {
        if (sinConexion) throw IOException("Sin red")
        return usuarios.filter { it.rolId == "1" && it.estado == "PENDIENTE" }
    }

    override suspend fun aprobar(id: String): UsuarioResponse = cambiarEstado(id, "APROBADO")

    override suspend fun rechazar(id: String): UsuarioResponse = cambiarEstado(id, "RECHAZADO")

    override suspend fun obtener(id: String): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        return usuarios.firstOrNull { it.id == id } ?: throw error(404, "Usuario no encontrado")
    }

    override suspend fun iniciarSesion(datos: LoginRequest): LoginResponse {
        if (sinConexion) throw IOException("Sin red")
        val usuario = usuarios.firstOrNull { it.correo.equals(datos.correo, ignoreCase = true) }
        if (usuario == null || datos.contrasena != "123456") {
            throw error(401, "El correo o la contraseña no son correctos")
        }
        if (usuario.estado == "PENDIENTE") {
            throw error(403, "Tu cuenta todavía no está aprobada por el analista")
        }
        if (usuario.estado == "RECHAZADO") {
            throw error(403, "El analista rechazó esta cuenta")
        }
        return LoginResponse(usuario, "token-de-prueba")
    }

    override suspend fun registrar(datos: RegistroRequest): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        if (usuarios.any { it.correo.equals(datos.correo, ignoreCase = true) }) {
            throw error(409, "Ese correo ya está registrado")
        }
        val esAsegurado = datos.rolId == null || datos.rolId == "1"
        val nuevo = UsuarioResponse(
            id = (usuarios.size + 1).toString(),
            nombre = datos.nombre,
            correo = datos.correo,
            rut = datos.rut,
            direccion = datos.direccion,
            equipo = datos.equipo,
            rolId = datos.rolId ?: "1",
            rolNombre = if (esAsegurado) "Asegurado" else "Liquidador",
            estado = if (esAsegurado) "PENDIENTE" else "APROBADO",
            avatarUrl = null
        )
        usuarios.add(nuevo)
        return nuevo
    }

    override suspend fun pregunta(correo: String): PreguntaResponse {
        if (sinConexion) throw IOException("Sin red")
        val usuario = usuarios.firstOrNull { it.correo.equals(correo, ignoreCase = true) }
            ?: throw error(404, "No hay una cuenta con ese correo")
        return PreguntaResponse(usuario.correo, "¿Nombre de tu primera mascota?")
    }

    override suspend fun recuperar(datos: RecuperarRequest) {
        if (sinConexion) throw IOException("Sin red")
        if (usuarios.none { it.correo.equals(datos.correo, ignoreCase = true) }) {
            throw error(404, "No hay una cuenta con ese correo")
        }
        recuperacion = datos
    }

    override suspend fun actualizar(
        id: String,
        datos: ActualizarUsuarioRequest
    ): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        val indice = usuarios.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Usuario no encontrado")
        val actualizado = usuarios[indice].copy(
            nombre = datos.nombre,
            correo = datos.correo,
            rut = datos.rut,
            direccion = datos.direccion,
            equipo = datos.equipo ?: usuarios[indice].equipo
        )
        usuarios[indice] = actualizado
        return actualizado
    }

    override suspend fun cambiarContrasena(id: String, datos: CambiarContrasenaRequest) {
        if (sinConexion) throw IOException("Sin red")
        if (usuarios.none { it.id == id }) throw error(404, "Usuario no encontrado")
        if (datos.actual != "123456") throw error(400, "La contraseña actual no es correcta")
        contrasenaCambiada = datos
    }

    private fun cambiarEstado(id: String, estado: String): UsuarioResponse {
        if (sinConexion) throw IOException("Sin red")
        val indice = usuarios.indexOfFirst { it.id == id }
        if (indice < 0) throw error(404, "Usuario no encontrado")
        if (usuarios[indice].rolId != "1") {
            throw error(400, "Solo se aprueban cuentas de asegurados")
        }
        val actualizado = usuarios[indice].copy(estado = estado)
        usuarios[indice] = actualizado
        return actualizado
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
            RolResponse("1", "Asegurado"),
            RolResponse("2", "Liquidador"),
            RolResponse("3", "Analista")
        )
    }
}
