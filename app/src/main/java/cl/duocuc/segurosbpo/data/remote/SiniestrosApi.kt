package cl.duocuc.segurosbpo.data.remote

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.TipoSiniestro
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ReporteServidor(
    val compania: String,
    val tipo: String,
    val fechaOcurrencia: String,
    val comuna: String,
    val descripcion: String
)

data class CasoServidor(
    val id: String,
    val numero: String,
    val compania: String,
    val tipo: String,
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val estado: String,
    val equipoAsignado: String?,
    val ultimaActualizacion: String,
    val proximoPaso: String?
)

data class GestionServidor(
    val id: String,
    val fechaHora: String,
    val descripcion: String,
    val estadoResultante: String?,
    val responsable: String?
)

interface SiniestrosApi {
    @GET("siniestros")
    suspend fun listar(
        @Header("X-Actor-Id") rut: String,
        @Header("X-Role") rol: String = "ASEGURADO"
    ): List<CasoServidor>

    @GET("siniestros/{numero}")
    suspend fun obtener(
        @Header("X-Actor-Id") rut: String,
        @Header("X-Role") rol: String = "ASEGURADO",
        @Path("numero") numero: String
    ): CasoServidor

    @GET("siniestros/{numero}/historial")
    suspend fun historial(
        @Header("X-Actor-Id") rut: String,
        @Header("X-Role") rol: String = "ASEGURADO",
        @Path("numero") numero: String
    ): List<GestionServidor>

    @POST("siniestros")
    suspend fun reportar(
        @Header("X-Actor-Id") rut: String,
        @Header("X-Role") rol: String = "ASEGURADO",
        @Body reporte: ReporteServidor
    ): CasoServidor
}

object ServidorSiniestros {
    private val cliente: SiniestrosApi by lazy {
        val http = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/api/v1/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SiniestrosApi::class.java)
    }

    fun codigoCompania(compania: Compania): String = when (compania.name) {
        "CORDILLERA" -> "ASG-01"
        "LITORAL" -> "ASG-02"
        "PATAGONIA" -> "ASG-03"
        "VALLE_CENTRAL" -> "ASG-04"
        "PROTEGE" -> "ASG-05"
        else -> error("La compañía no está disponible en el servidor")
    }

    fun codigoTipo(tipo: TipoSiniestro): String = when (tipo.name) {
        "CHOQUE" -> "TS-01"
        "ROBO_VEHICULO" -> "TS-02"
        "CRISTALES" -> "TS-03"
        "DANO_AGUA" -> "TS-04"
        "ROBO_HOGAR" -> "TS-05"
        "INCENDIO" -> "TS-06"
        else -> error("El tipo de siniestro no está disponible en el servidor")
    }

    fun fechaIso(fecha: String): String {
        val entrada = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply { isLenient = false }
        val salida = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        return salida.format(requireNotNull(entrada.parse(fecha)) { "Fecha inválida" })
    }

    suspend fun listar(asegurado: Asegurado): List<CasoServidor> =
        cliente.listar(asegurado.rut)

    suspend fun historial(asegurado: Asegurado, numero: String): List<GestionServidor> =
        cliente.historial(asegurado.rut, numero = numero)

    suspend fun reportar(estado: FormularioUiState): CasoServidor =
        cliente.reportar(
            rut = estado.asegurado.rut,
            reporte = ReporteServidor(
                compania = codigoCompania(estado.compania),
                tipo = codigoTipo(estado.tipoSiniestro),
                fechaOcurrencia = fechaIso(estado.fechaOcurrencia),
                comuna = estado.comuna,
                descripcion = estado.descripcion
            )
        )
}
