package cl.duocuc.segurosbpo.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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
