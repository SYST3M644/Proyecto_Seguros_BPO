package cl.duocuc.segurosbpo.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listar(@Query("buscar") buscar: String? = null): List<UsuarioResponse>

    @GET("api/usuarios/pendientes")
    suspend fun pendientes(): List<UsuarioResponse>

    @PUT("api/usuarios/{id}/aprobar")
    suspend fun aprobar(@Path("id") id: String): UsuarioResponse

    @PUT("api/usuarios/{id}/rechazar")
    suspend fun rechazar(@Path("id") id: String): UsuarioResponse

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: String): UsuarioResponse

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): LoginResponse

    @POST("api/usuarios")
    suspend fun registrar(@Body datos: RegistroRequest): UsuarioResponse

    @GET("api/usuarios/pregunta")
    suspend fun pregunta(@Query("correo") correo: String): PreguntaResponse

    @POST("api/usuarios/recuperar")
    suspend fun recuperar(@Body datos: RecuperarRequest)

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(
        @Path("id") id: String,
        @Body datos: ActualizarUsuarioRequest
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: String,
        @Body datos: CambiarContrasenaRequest
    )
}
