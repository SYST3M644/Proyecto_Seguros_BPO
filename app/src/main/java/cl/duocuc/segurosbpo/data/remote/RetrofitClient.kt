package cl.duocuc.segurosbpo.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val URL_BASE = "http://10.0.2.2:6464/"

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val token = SesionToken.actual()
                val peticion = if (token.isNullOrBlank()) {
                    cadena.request()
                } else {
                    cadena.request().newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build()
                }
                cadena.proceed(peticion)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val rolApi: RolApi by lazy { retrofit.create(RolApi::class.java) }

    val usuarioApi: UsuarioApi by lazy { retrofit.create(UsuarioApi::class.java) }

    val catalogoApi: CatalogoApi by lazy { retrofit.create(CatalogoApi::class.java) }

    val siniestroApi: SiniestroApi by lazy { retrofit.create(SiniestroApi::class.java) }

    val avatarApi: AvatarApi by lazy { retrofit.create(AvatarApi::class.java) }

    val evidenciaApi: EvidenciaApi by lazy { retrofit.create(EvidenciaApi::class.java) }
}
