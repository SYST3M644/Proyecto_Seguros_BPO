package cl.duocuc.segurosbpo.ui.preview

import cl.duocuc.segurosbpo.data.remote.AvisoResponse
import cl.duocuc.segurosbpo.data.remote.CargaLiquidadorResponse
import cl.duocuc.segurosbpo.data.remote.ConsultaResponse
import cl.duocuc.segurosbpo.data.remote.EvaluacionResponse
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.EvidenciaResponse
import cl.duocuc.segurosbpo.data.remote.SolicitudResponse
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.AvisoSiniestro
import cl.duocuc.segurosbpo.model.Consulta
import cl.duocuc.segurosbpo.model.EvaluacionCierre
import cl.duocuc.segurosbpo.model.Gestion
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

    val gestiones = listOf(
        Gestion(1, 1, FECHA_HORA, "Asegurado", "Reporte registrado con numero SIN-26-004861", true),
        Gestion(2, 1, FECHA_HORA, "Analista de administracion", "Caso asignado a Maria P. Lagos, del equipo Vehiculos", true),
        Gestion(3, 1, FECHA_HORA, "Maria P. Lagos", "Se solicito el presupuesto de reparacion.", true)
    )

    val consultaRespondida = Consulta(
        id = 1,
        siniestroId = 1,
        pregunta = "Cuanto demora la revision del presupuesto?",
        respuesta = "Entre tres y cinco dias habiles desde que lo recibimos.",
        fechaPregunta = FECHA_HORA,
        fechaRespuesta = FECHA_HORA
    )

    val consultaPendiente = Consulta(
        id = 2,
        siniestroId = 1,
        pregunta = "Puedo llevar el auto a un taller de mi confianza?",
        respuesta = null,
        fechaPregunta = FECHA_HORA,
        fechaRespuesta = null
    )

    val consultas = listOf(consultaRespondida, consultaPendiente)

    val evaluacion = EvaluacionCierre(
        siniestroId = 3,
        nota = 4,
        comentario = "La atencion fue clara y rapida.",
        fecha = FECHA_HORA
    )

    val avisos = listOf(
        AvisoSiniestro(
            id = 1,
            siniestroId = 1,
            numeroSiniestro = "SIN-26-004861",
            mensaje = "Necesitamos el documento: Presupuesto de reparacion",
            fecha = FECHA_HORA,
            leido = false,
            solicitudId = 1,
            documentoPedido = "Presupuesto de reparacion"
        ),
        AvisoSiniestro(
            id = 2,
            siniestroId = 1,
            numeroSiniestro = "SIN-26-004861",
            mensaje = "Tu caso quedo a cargo de Maria P. Lagos.",
            fecha = FECHA_HORA,
            leido = true
        )
    )

    val siniestrosApi = listOf(
        casoApi("SIN-26-004861", "Seguros Cordillera", "Vehículo · choque", "EVALUACION"),
        casoApi("SIN-26-004862", "Protege Seguros", "Hogar · robo", "RECIBIDO"),
        casoApi("SIN-26-004863", "Seguros Cordillera", "Hogar · incendio", "CERRADO")
    )

    private fun casoApi(
        numero: String,
        compania: String,
        tipo: String,
        etapa: String
    ) = SiniestroResponse(
        id = numero,
        numero = numero,
        aseguradoId = "1",
        aseguradoNombre = "Camila Rojas Fuentes",
        companiaCodigo = "CORDILLERA",
        companiaNombre = compania,
        plazoDiasHabiles = 10,
        tipoCodigo = "CHOQUE",
        tipoNombre = tipo,
        fechaOcurrencia = "10-11-2026",
        fechaReporte = "12-11-2026",
        comuna = "Quilicura",
        descripcion = "Colisión leve en el estacionamiento",
        huboTerceros = false,
        etapa = etapa,
        liquidadorId = null,
        liquidadorNombre = null,
        resultadoCierre = null,
        mensajeCierre = null,
        ultimaActualizacion = FECHA_HORA
    )

    val avisosApi = listOf(
        AvisoResponse(
            id = "1", siniestroId = "1", numero = "SIN-26-004861",
            destinatarioId = "1", mensaje = "Tu caso avanzó a en evaluación",
            fecha = FECHA_HORA, leido = false
        ),
        AvisoResponse(
            id = "2", siniestroId = "1", numero = "SIN-26-004861",
            destinatarioId = "1", mensaje = "Te piden un documento: Boleta del taller",
            fecha = FECHA_HORA, leido = true
        )
    )

    val consultasApi = listOf(
        ConsultaResponse(
            id = "1", siniestroId = "1",
            pregunta = "¿Cuánto demora la evaluación del vehículo?",
            respuesta = "Entre cinco y siete días hábiles desde la inspección.",
            fechaPregunta = FECHA_HORA, fechaRespuesta = FECHA_HORA
        ),
        ConsultaResponse(
            id = "2", siniestroId = "1",
            pregunta = "¿Necesitan algún documento adicional?",
            respuesta = null, fechaPregunta = FECHA_HORA, fechaRespuesta = null
        )
    )

    val solicitudesApi = listOf(
        SolicitudResponse(
            id = "1", siniestroId = "1", tipoDocumento = "Boleta del taller",
            comentario = "La necesitamos para liquidar", solicitadoPor = "Maria P. Lagos",
            fecha = FECHA_HORA, estado = "PENDIENTE"
        ),
        SolicitudResponse(
            id = "2", siniestroId = "1", tipoDocumento = "Constancia policial",
            comentario = "", solicitadoPor = "Maria P. Lagos",
            fecha = FECHA_HORA, estado = "RECIBIDA"
        )
    )

    val evidenciasApi = listOf(
        EvidenciaResponse(
            id = "1", siniestroId = "1", tipoDocumento = "Fotografías de los daños",
            nombreArchivo = "frontal.jpg", mime = "image/jpeg", tamanoBytes = 184320,
            fechaRegistro = FECHA_HORA, subidoPor = "Camila Rojas Fuentes",
            archivoUrl = ""
        )
    )

    val cargaApi = listOf(
        CargaLiquidadorResponse("1", "Maria P. Lagos", "VEHICULOS", 4, 9),
        CargaLiquidadorResponse("2", "Pedro C. Rivas", "HOGAR", 2, 5)
    )

    val gestionesApi = listOf(
        GestionResponse(
            id = "1", siniestroId = "1", fecha = FECHA_HORA,
            responsable = "Camila Rojas Fuentes",
            descripcion = "Reporte registrado con número SIN-26-004861",
            visibleAsegurado = true
        ),
        GestionResponse(
            id = "2", siniestroId = "1", fecha = FECHA_HORA,
            responsable = "Analista de administración",
            descripcion = "Caso asignado a Maria P. Lagos",
            visibleAsegurado = true
        )
    )

    val evaluacionApi = EvaluacionResponse(
        id = "1", siniestroId = "1", nota = 5,
        comentario = "Muy buena atención", fecha = FECHA_HORA
    )
}
