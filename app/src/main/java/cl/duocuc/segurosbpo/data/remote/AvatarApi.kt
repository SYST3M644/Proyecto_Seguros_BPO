package cl.duocuc.segurosbpo.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AvatarApi {

    @Multipart
    @POST("api/usuarios/{id}/avatar")
    suspend fun subir(@Path("id") id: String, @Part archivo: MultipartBody.Part): UsuarioResponse
}
