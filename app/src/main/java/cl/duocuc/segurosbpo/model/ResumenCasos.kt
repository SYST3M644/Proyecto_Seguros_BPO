package cl.duocuc.segurosbpo.model

import androidx.compose.runtime.Immutable
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse

@Immutable
data class ResumenCasos(
    val recibidos: Int,
    val evaluacion: Int,
    val liquidacion: Int,
    val cerrados: Int
) {
    val total: Int get() = recibidos + evaluacion + liquidacion + cerrados
    val abiertos: Int get() = total - cerrados

    companion object {
        fun desde(casos: List<SiniestroResponse>): ResumenCasos = ResumenCasos(
            recibidos = casos.count { it.etapa == "RECIBIDO" },
            evaluacion = casos.count { it.etapa == "EVALUACION" },
            liquidacion = casos.count { it.etapa == "LIQUIDACION" },
            cerrados = casos.count { it.etapa == "CERRADO" }
        )
    }
}
