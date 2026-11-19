package cl.duocuc.segurosbpo.model

import cl.duocuc.segurosbpo.data.remote.SiniestroResponse

object OrdenSiniestros {

    const val CERRADO = "CERRADO"

    fun paraLiquidador(casos: List<SiniestroResponse>): List<SiniestroResponse> =
        casos.sortedWith(
            compareBy<SiniestroResponse> { if (it.etapa == CERRADO) 1 else 0 }
                .thenByDescending { clave(it.ultimaActualizacion) }
                .thenByDescending { it.numero }
        )

    fun clave(fechaHora: String): Long {
        val partes = fechaHora.trim().split(" ")
        val dia = partes.getOrNull(0)?.split("-").orEmpty()
        if (dia.size < 3) return 0
        val hora = partes.getOrNull(1)?.split(":").orEmpty()

        val anio = dia[2].toLongOrNull() ?: return 0
        val mes = dia[1].toLongOrNull() ?: return 0
        val numeroDia = dia[0].toLongOrNull() ?: return 0
        val horas = hora.getOrNull(0)?.toLongOrNull() ?: 0
        val minutos = hora.getOrNull(1)?.toLongOrNull() ?: 0

        return anio * 100000000 + mes * 1000000 + numeroDia * 10000 + horas * 100 + minutos
    }
}
