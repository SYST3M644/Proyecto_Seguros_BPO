package cl.duocuc.segurosbpo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val URL_ROLES = "http://10.0.2.2:8081/"
    private const val URL_USUARIOS = "http://10.0.2.2:8082/"
    private const val URL_CATALOGOS = "http://10.0.2.2:8083/"
    private const val URL_SINIESTROS = "http://10.0.2.2:8084/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val rolApi: RolApi by lazy { crear(URL_ROLES).create(RolApi::class.java) }

    val usuarioApi: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }

    val catalogoApi: CatalogoApi by lazy { crear(URL_CATALOGOS).create(CatalogoApi::class.java) }

    val siniestroApi: SiniestroApi by lazy { crear(URL_SINIESTROS).create(SiniestroApi::class.java) }

    val avatarApi: AvatarApi by lazy { crear(URL_USUARIOS).create(AvatarApi::class.java) }

    val evidenciaApi: EvidenciaApi by lazy { crear(URL_SINIESTROS).create(EvidenciaApi::class.java) }
}
