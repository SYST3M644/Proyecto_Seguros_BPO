package cl.duocuc.segurosbpo.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CatalogoApi {

    @GET("api/regiones")
    suspend fun listarRegiones(): List<RegionResponse>

    @GET("api/regiones/{codigo}/comunas")
    suspend fun listarComunas(@Path("codigo") regionCodigo: String): List<ComunaResponse>

    @GET("api/companias")
    suspend fun listarCompanias(): List<CompaniaResponse>

    @POST("api/companias")
    suspend fun crearCompania(@Body datos: CompaniaRequest): CompaniaResponse

    @GET("api/tipos-siniestro")
    suspend fun listarTipos(): List<TipoSiniestroResponse>

    @POST("api/tipos-siniestro")
    suspend fun crearTipo(@Body datos: TipoSiniestroRequest): TipoSiniestroResponse
}
