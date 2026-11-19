package cl.duocuc.segurosbpo.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SiniestroApi {

    @GET("api/siniestros")
    suspend fun listarPorAsegurado(@Query("aseguradoId") aseguradoId: String): List<SiniestroResponse>

    @GET("api/siniestros")
    suspend fun listarPorLiquidador(@Query("liquidadorId") liquidadorId: String): List<SiniestroResponse>

    @GET("api/siniestros")
    suspend fun listarSinAsignar(@Query("sinAsignar") sinAsignar: Boolean = true): List<SiniestroResponse>

    @GET("api/siniestros/avisos")
    suspend fun listarAvisos(@Query("usuarioId") usuarioId: String): List<AvisoResponse>

    @GET("api/siniestros/avisos/sin-leer")
    suspend fun avisosSinLeer(@Query("usuarioId") usuarioId: String): Long

    @PUT("api/siniestros/avisos/{id}/leido")
    suspend fun marcarAvisoLeido(@Path("id") id: String): AvisoResponse

    @GET("api/siniestros/{id}/solicitudes")
    suspend fun listarSolicitudes(@Path("id") siniestroId: String): List<SolicitudResponse>

    @POST("api/siniestros/{id}/solicitudes")
    suspend fun solicitar(
        @Path("id") siniestroId: String,
        @Body datos: SolicitudRequest
    ): SolicitudResponse

    @PUT("api/siniestros/solicitudes/{id}/recibida")
    suspend fun marcarSolicitudRecibida(@Path("id") id: String): SolicitudResponse

    @DELETE("api/siniestros/solicitudes/{id}")
    suspend fun anularSolicitud(@Path("id") id: String): SolicitudResponse

    @GET("api/siniestros/{id}/consultas")
    suspend fun listarConsultas(@Path("id") siniestroId: String): List<ConsultaResponse>

    @POST("api/siniestros/{id}/consultas")
    suspend fun preguntar(
        @Path("id") siniestroId: String,
        @Body datos: ConsultaRequest
    ): ConsultaResponse

    @PUT("api/siniestros/consultas/{id}/responder")
    suspend fun responderConsulta(
        @Path("id") id: String,
        @Body datos: ConsultaRequest
    ): ConsultaResponse

    @GET("api/siniestros/{id}/evaluacion")
    suspend fun obtenerEvaluacion(@Path("id") siniestroId: String): EvaluacionResponse

    @PUT("api/siniestros/{id}/evaluacion")
    suspend fun evaluar(
        @Path("id") siniestroId: String,
        @Body datos: EvaluacionRequest
    ): EvaluacionResponse

    @GET("api/siniestros/disponibles")
    suspend fun listarDisponibles(
        @Query("liquidadorId") liquidadorId: String
    ): List<SiniestroResponse>

    @PUT("api/siniestros/{id}/tomar")
    suspend fun tomar(
        @Path("id") id: String,
        @Query("liquidadorId") liquidadorId: String
    ): SiniestroResponse

    @GET("api/siniestros/archivados")
    suspend fun listarArchivados(
        @Query("liquidadorId") liquidadorId: String
    ): List<SiniestroResponse>

    @PUT("api/siniestros/{id}/archivar")
    suspend fun archivar(@Path("id") id: String): SiniestroResponse

    @PUT("api/siniestros/{id}/restaurar")
    suspend fun restaurar(@Path("id") id: String): SiniestroResponse

    @PUT("api/siniestros/{id}/desistir")
    suspend fun desistir(
        @Path("id") id: String,
        @Body datos: MotivoRequest
    ): SiniestroResponse

    @PUT("api/siniestros/{id}/descripcion")
    suspend fun actualizarDescripcion(
        @Path("id") id: String,
        @Body datos: MotivoRequest
    ): SiniestroResponse

    @GET("api/siniestros/carga")
    suspend fun listarCarga(): List<CargaLiquidadorResponse>

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
