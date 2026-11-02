package cl.duocuc.segurosbpo.ui.preview

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TipoSiniestro

object Muestras {

    const val FECHA = "02-11-2026"
    const val FECHA_HORA = "02-11-2026 13:12"

    val siniestroEnEvaluacion = Siniestro(
        id = 1,
        numero = "SIN-26-004861",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "28-10-2026",
        fechaReporte = FECHA,
        comuna = "Quilicura",
        descripcion = "Colisión leve en el estacionamiento del supermercado.",
        huboTerceros = true,
        etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = FECHA_HORA,
        liquidadorAsignado = Liquidador.VEHICULOS,
        liquidadorId = "LIQ_VEHICULOS_1",
        liquidadorNombre = "María P. Lagos"
    )

    val siniestroRecibido = siniestroEnEvaluacion.copy(
        id = 2,
        numero = "SIN-26-004862",
        tipo = TipoSiniestro.DANO_AGUA,
        comuna = "Maipú",
        descripcion = "Filtración de agua en la cocina.",
        huboTerceros = false,
        etapa = EtapaSiniestro.RECIBIDO,
        liquidadorAsignado = null,
        liquidadorId = null,
        liquidadorNombre = null
    )

    val siniestroCerrado = siniestroEnEvaluacion.copy(
        id = 3,
        numero = "SIN-26-004863",
        tipo = TipoSiniestro.CRISTALES,
        descripcion = "Rotura del cristal lateral.",
        etapa = EtapaSiniestro.CERRADO,
        resultadoCierre = "Aprobado",
        mensajeCierre = "Se aprobó la reposición del cristal."
    )

    val siniestros = listOf(siniestroEnEvaluacion, siniestroRecibido, siniestroCerrado)

    val asegurada = CuentaLocal(
        id = "CAMILA",
        nombre = "Camila Rojas Fuentes",
        rut = "15.842.371-5",
        correo = "crojas@correo.cl",
        telefono = "900000000",
        rol = RolCuenta.ASEGURADO,
        equipo = null
    )

    val liquidadora = CuentaLocal(
        id = "LIQ_VEHICULOS_1",
        nombre = "María P. Lagos",
        rut = "",
        correo = "mlagos@segurosbpo.cl",
        telefono = "900000000",
        rol = RolCuenta.LIQUIDADOR,
        equipo = Liquidador.VEHICULOS.name
    )

    val liquidador = CuentaLocal(
        id = "LIQ_VEHICULOS_2",
        nombre = "Esteban R. Fuentes",
        rut = "",
        correo = "efuentes@segurosbpo.cl",
        telefono = "900000000",
        rol = RolCuenta.LIQUIDADOR,
        equipo = Liquidador.VEHICULOS.name
    )

    val analista = CuentaLocal(
        id = "ANALISTA_01",
        nombre = "Paulina G.",
        rut = "",
        correo = "asignaciones@segurosbpo.cl",
        telefono = "900000000",
        rol = RolCuenta.ANALISTA,
        equipo = null
    )

    val cuentas = listOf(asegurada, liquidadora, liquidador, analista)

    val equipos = mapOf(
        Liquidador.VEHICULOS to listOf(liquidadora, liquidador),
        Liquidador.HOGAR to emptyList<CuentaLocal>()
    )

    val cargaPorEquipo = mapOf(Liquidador.VEHICULOS to 3, Liquidador.HOGAR to 1)

    val cargaPorPersona = mapOf("LIQ_VEHICULOS_1" to 2, "LIQ_VEHICULOS_2" to 1)

    val solicitudPendiente = SolicitudDocumento(
        id = 1,
        siniestroId = 1,
        tipo = "Presupuesto de reparación",
        comentario = "Pídelo en un taller de la red.",
        fechaSolicitud = FECHA_HORA,
        fechaEntrega = null
    )

    val solicitudes = listOf(solicitudPendiente)

    val evidencia = Evidencia(
        id = 1,
        siniestroId = 1,
        tipoDocumento = "Fotografías de los daños",
        nombreArchivo = "foto_camara.jpg",
        tipoMime = "image/jpeg",
        tamanoBytes = 184_320,
        fechaRegistro = FECHA_HORA
    )

    val evidencias = listOf(evidencia)
}
