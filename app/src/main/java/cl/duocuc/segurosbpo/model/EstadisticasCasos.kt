package cl.duocuc.segurosbpo.model

import cl.duocuc.segurosbpo.data.remote.SiniestroResponse

data class PuntoEstadistico(val etiqueta: String, val cantidad: Int)

object EstadisticasCasos {
    fun porTipo(siniestros: List<SiniestroResponse>): List<PuntoEstadistico> =
        siniestros.groupingBy { it.tipoNombre ?: it.tipoCodigo }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { PuntoEstadistico(it.key, it.value) }

    fun porMes(siniestros: List<SiniestroResponse>): List<PuntoEstadistico> {
        return siniestros.mapNotNull { siniestro ->
            val fecha = siniestro.fechaReporte
            if (!Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}").matches(fecha.take(10))) null
            else fecha.substring(6, 10) + "-" + fecha.substring(3, 5)
        }.groupingBy { it }.eachCount()
            .toSortedMap().entries.toList().takeLast(6)
            .map { PuntoEstadistico(it.key.substring(5) + "/" + it.key.substring(0, 4), it.value) }
    }
}
