package cl.duocuc.segurosbpo.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listar(@Query("buscar") buscar: String? = null): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: String): UsuarioResponse

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioResponse
}

interface RolApi {

    @GET("api/roles")
    suspend fun listar(): List<RolResponse>
}

interface CatalogoApi {

    @GET("api/companias")
    suspend fun listarCompanias(): List<CompaniaResponse>

    @POST("api/companias")
    suspend fun crearCompania(@Body datos: CompaniaRequest): CompaniaResponse

    @GET("api/tipos-siniestro")
    suspend fun listarTipos(): List<TipoSiniestroResponse>

    @POST("api/tipos-siniestro")
    suspend fun crearTipo(@Body datos: TipoSiniestroRequest): TipoSiniestroResponse
}

interface SiniestroApi {

    @GET("api/siniestros")
    suspend fun listarPorAsegurado(@Query("aseguradoId") aseguradoId: String): List<SiniestroResponse>

    @GET("api/siniestros")
    suspend fun listarPorLiquidador(@Query("liquidadorId") liquidadorId: String): List<SiniestroResponse>

    @GET("api/siniestros")
    suspend fun listarSinAsignar(@Query("sinAsignar") sinAsignar: Boolean = true): List<SiniestroResponse>

    @GET("api/siniestros/{id}")
    suspend fun obtener(@Path("id") id: String): SiniestroResponse

    @POST("api/siniestros")
    suspend fun registrar(@Body datos: RegistrarSiniestroRequest): SiniestroResponse

    @PUT("api/siniestros/{id}/asignar")
    suspend fun asignar(@Path("id") id: String, @Body datos: AsignarRequest): SiniestroResponse

    @PUT("api/siniestros/{id}/avanzar")
    suspend fun avanzar(@Path("id") id: String, @Body datos: AvanzarRequest): SiniestroResponse

    @GET("api/siniestros/{id}/gestiones")
    suspend fun listarGestiones(@Path("id") id: String): List<GestionResponse>

    @POST("api/siniestros/{id}/gestiones")
    suspend fun agregarGestion(@Path("id") id: String, @Body datos: GestionRequest): GestionResponse
}

interface AvatarApi {

    @Multipart
    @POST("api/usuarios/{id}/avatar")
    suspend fun subir(@Path("id") id: String, @Part archivo: MultipartBody.Part): UsuarioResponse
}

interface EvidenciaApi {

    @GET("api/siniestros/{id}/evidencias")
    suspend fun listar(@Path("id") siniestroId: String): List<EvidenciaResponse>

    @Multipart
    @POST("api/siniestros/{id}/evidencias")
    suspend fun subir(
        @Path("id") siniestroId: String,
        @Part("tipoDocumento") tipoDocumento: RequestBody,
        @Part("subidoPor") subidoPor: RequestBody,
        @Part archivo: MultipartBody.Part
    ): EvidenciaResponse

    @DELETE("api/siniestros/evidencias/{evidenciaId}")
    suspend fun eliminar(@Path("evidenciaId") evidenciaId: String)
}
