package cl.duocuc.segurosbpo.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.segurosbpo.data.ArchivoRepository
import cl.duocuc.segurosbpo.data.CatalogoRepository
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.data.SesionRepository
import cl.duocuc.segurosbpo.data.SiniestroRepository
import cl.duocuc.segurosbpo.data.VistaPreviaArchivo
import cl.duocuc.segurosbpo.data.remote.CompaniaResponse
import cl.duocuc.segurosbpo.data.remote.AvisoResponse
import cl.duocuc.segurosbpo.data.remote.CargaLiquidadorResponse
import cl.duocuc.segurosbpo.data.remote.ConsultaResponse
import cl.duocuc.segurosbpo.data.remote.EvaluacionResponse
import cl.duocuc.segurosbpo.data.remote.SolicitudResponse
import cl.duocuc.segurosbpo.data.remote.ComunaResponse
import cl.duocuc.segurosbpo.data.remote.EvidenciaResponse
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.RegionResponse
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.data.remote.TipoSiniestroResponse
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SiniestrosViewModel(
    private val repositorio: SiniestroRepository = SiniestroRepository(),
    private val catalogos: CatalogoRepository = CatalogoRepository(),
    private val sesion: SesionRepository = SesionRepository(),
    private val archivos: ArchivoRepository = ArchivoRepository()
) : ViewModel() {

    private val _siniestros = MutableStateFlow<List<SiniestroResponse>>(emptyList())
    val siniestros = _siniestros.asStateFlow()

    private val _gestiones = MutableStateFlow<List<GestionResponse>>(emptyList())
    val gestiones = _gestiones.asStateFlow()

    private val _evidencias = MutableStateFlow<List<EvidenciaResponse>>(emptyList())
    val evidencias = _evidencias.asStateFlow()

    private val _companias = MutableStateFlow<List<CompaniaResponse>>(emptyList())
    val companias = _companias.asStateFlow()

    private val _tipos = MutableStateFlow<List<TipoSiniestroResponse>>(emptyList())
    val tipos = _tipos.asStateFlow()

    private val _regiones = MutableStateFlow<List<RegionResponse>>(emptyList())
    val regiones = _regiones.asStateFlow()

    private val _comunas = MutableStateFlow<List<ComunaResponse>>(emptyList())
    val comunas = _comunas.asStateFlow()

    private val _cargaLiquidadores = MutableStateFlow<List<CargaLiquidadorResponse>>(emptyList())
    val cargaLiquidadores = _cargaLiquidadores.asStateFlow()

    private val _liquidadores = MutableStateFlow<List<UsuarioResponse>>(emptyList())
    val liquidadores = _liquidadores.asStateFlow()

    private val _pendientes = MutableStateFlow<List<UsuarioResponse>>(emptyList())
    val pendientes = _pendientes.asStateFlow()

    private val _avisos = MutableStateFlow<List<AvisoResponse>>(emptyList())
    val avisos = _avisos.asStateFlow()

    private val _sinLeer = MutableStateFlow(0L)
    val sinLeer = _sinLeer.asStateFlow()

    private val _solicitudes = MutableStateFlow<List<SolicitudResponse>>(emptyList())
    val solicitudes = _solicitudes.asStateFlow()

    private val _consultas = MutableStateFlow<List<ConsultaResponse>>(emptyList())
    val consultas = _consultas.asStateFlow()

    private val _disponibles = MutableStateFlow<List<SiniestroResponse>>(emptyList())
    val disponibles = _disponibles.asStateFlow()

    private val _archivados = MutableStateFlow<List<SiniestroResponse>>(emptyList())
    val archivados = _archivados.asStateFlow()

    private val _evaluacion = MutableStateFlow<EvaluacionResponse?>(null)
    val evaluacion = _evaluacion.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso = _aviso.asStateFlow()

    fun cargarDeAsegurado(aseguradoId: String) {
        ejecutar { _siniestros.value = repositorio.listarPorAsegurado(aseguradoId) }
    }

    fun cargarDeLiquidador(liquidadorId: String) {
        ejecutar { _siniestros.value = repositorio.listarPorLiquidador(liquidadorId) }
    }

    fun cargarSinAsignar() {
        ejecutar {
            _siniestros.value = repositorio.listarSinAsignar()
            _liquidadores.value = sesion.listarUsuarios().filter { it.rolId == "2" }
        }
    }

    fun cargarCatalogos() {
        ejecutar {
            _companias.value = catalogos.listarCompanias()
            _tipos.value = catalogos.listarTipos()
            _regiones.value = catalogos.listarRegiones()
        }
    }

    fun cargarCarga() {
        ejecutar { _cargaLiquidadores.value = repositorio.listarCarga() }
    }

    fun subirFoto(
        siniestroId: String,
        tipoDocumento: String,
        subidoPor: String,
        bitmap: Bitmap
    ) {
        val archivo = ProcesadorEvidencia.desdeCamara(bitmap)
        subirEvidencia(
            siniestroId, tipoDocumento, subidoPor,
            archivo.nombre, archivo.mime, archivo.bytes
        )
    }

    fun subirArchivo(
        contexto: Context,
        siniestroId: String,
        tipoDocumento: String,
        subidoPor: String,
        uri: Uri
    ) {
        val archivo = runCatching { ProcesadorEvidencia.desdeSelector(contexto, uri) }
        archivo.fold(
            onSuccess = {
                subirEvidencia(
                    siniestroId, tipoDocumento, subidoPor, it.nombre, it.mime, it.bytes
                )
            },
            onFailure = { _error.value = it.message ?: "No se pudo leer el archivo" }
        )
    }

    fun cargarAvisos(usuarioId: String) {
        ejecutar {
            _avisos.value = repositorio.listarAvisos(usuarioId)
            _sinLeer.value = repositorio.avisosSinLeer(usuarioId)
        }
    }

    fun marcarAvisoLeido(id: String, usuarioId: String) {
        ejecutar {
            repositorio.marcarAvisoLeido(id)
            _avisos.value = repositorio.listarAvisos(usuarioId)
            _sinLeer.value = repositorio.avisosSinLeer(usuarioId)
        }
    }

    fun cargarSolicitudes(siniestroId: String) {
        ejecutar { _solicitudes.value = repositorio.listarSolicitudes(siniestroId) }
    }

    fun solicitarDocumento(
        siniestroId: String,
        tipoDocumento: String,
        comentario: String,
        solicitadoPor: String
    ) {
        ejecutar {
            repositorio.solicitar(siniestroId, tipoDocumento, comentario, solicitadoPor)
            _solicitudes.value = repositorio.listarSolicitudes(siniestroId)
            _aviso.value = "Documento solicitado al asegurado"
        }
    }

    fun marcarSolicitudRecibida(id: String, siniestroId: String) {
        ejecutar {
            repositorio.marcarSolicitudRecibida(id)
            _solicitudes.value = repositorio.listarSolicitudes(siniestroId)
            _aviso.value = "Documento marcado como recibido"
        }
    }

    fun anularSolicitud(id: String, siniestroId: String) {
        ejecutar {
            repositorio.anularSolicitud(id)
            _solicitudes.value = repositorio.listarSolicitudes(siniestroId)
            _aviso.value = "Solicitud anulada"
        }
    }

    fun cargarConsultas(siniestroId: String) {
        ejecutar { _consultas.value = repositorio.listarConsultas(siniestroId) }
    }

    fun preguntar(siniestroId: String, texto: String) {
        ejecutar {
            repositorio.preguntar(siniestroId, texto)
            _consultas.value = repositorio.listarConsultas(siniestroId)
            _aviso.value = "Consulta enviada"
        }
    }

    fun responderConsulta(id: String, siniestroId: String, texto: String) {
        ejecutar {
            repositorio.responderConsulta(id, texto)
            _consultas.value = repositorio.listarConsultas(siniestroId)
            _aviso.value = "Respuesta enviada"
        }
    }

    fun cargarDisponibles(liquidadorId: String) {
        ejecutar { _disponibles.value = repositorio.listarDisponibles(liquidadorId) }
    }

    fun tomarCaso(id: String, liquidadorId: String) {
        ejecutar {
            repositorio.tomar(id, liquidadorId)
            _disponibles.value = repositorio.listarDisponibles(liquidadorId)
            _siniestros.value = repositorio.listarPorLiquidador(liquidadorId)
            _aviso.value = "Tomaste el caso"
        }
    }

    fun cargarArchivados(liquidadorId: String) {
        ejecutar { _archivados.value = repositorio.listarArchivados(liquidadorId) }
    }

    fun archivar(id: String, liquidadorId: String) {
        ejecutar {
            repositorio.archivar(id)
            _siniestros.value = repositorio.listarPorLiquidador(liquidadorId)
            _archivados.value = repositorio.listarArchivados(liquidadorId)
            _aviso.value = "Caso archivado"
        }
    }

    fun restaurar(id: String, liquidadorId: String) {
        ejecutar {
            repositorio.restaurar(id)
            _siniestros.value = repositorio.listarPorLiquidador(liquidadorId)
            _archivados.value = repositorio.listarArchivados(liquidadorId)
            _aviso.value = "Caso restaurado"
        }
    }

    fun cargarEvaluacion(siniestroId: String) {
        viewModelScope.launch {
            _evaluacion.value = runCatching {
                repositorio.obtenerEvaluacion(siniestroId)
            }.getOrNull()
        }
    }

    fun evaluar(siniestroId: String, nota: Int, comentario: String) {
        ejecutar {
            _evaluacion.value = repositorio.evaluar(siniestroId, nota, comentario)
            _aviso.value = "Gracias por tu evaluación"
        }
    }

    fun desistir(siniestroId: String, motivo: String, aseguradoId: String) {
        ejecutar {
            repositorio.desistir(siniestroId, motivo)
            _siniestros.value = repositorio.listarPorAsegurado(aseguradoId)
            _aviso.value = "Desististe del caso"
        }
    }

    fun actualizarDescripcion(siniestroId: String, descripcion: String, aseguradoId: String) {
        ejecutar {
            repositorio.actualizarDescripcion(siniestroId, descripcion)
            _siniestros.value = repositorio.listarPorAsegurado(aseguradoId)
            _aviso.value = "Descripción actualizada"
        }
    }

    fun cargarComunas(regionCodigo: String) {
        _comunas.value = emptyList()
        ejecutar { _comunas.value = catalogos.listarComunas(regionCodigo) }
    }

    fun registrar(
        aseguradoId: String,
        companiaCodigo: String,
        tipoCodigo: String,
        fechaOcurrencia: String,
        comuna: String,
        descripcion: String,
        huboTerceros: Boolean,
        alTerminar: (SiniestroResponse?) -> Unit = {}
    ) {
        ejecutar {
            val nuevo = repositorio.registrar(
                RegistrarSiniestroRequest(
                    aseguradoId, companiaCodigo, tipoCodigo,
                    fechaOcurrencia, comuna, descripcion, huboTerceros
                )
            )
            _siniestros.value = listOf(nuevo) + _siniestros.value
            _aviso.value = "Tu siniestro quedó registrado con el número " + nuevo.numero
            alTerminar(nuevo)
        }
    }

    fun asignar(siniestroId: String, liquidadorId: String, nombre: String) {
        ejecutar {
            repositorio.asignar(siniestroId, liquidadorId)
            _siniestros.value = repositorio.listarSinAsignar()
            _cargaLiquidadores.value = repositorio.listarCarga()
            _aviso.value = "Caso asignado a " + nombre
        }
    }

    fun avanzar(siniestroId: String, resultado: String, mensajeCierre: String) {
        ejecutar {
            val actualizado = repositorio.avanzar(siniestroId, resultado, mensajeCierre)
            _siniestros.value = _siniestros.value.map {
                if (it.id == actualizado.id) actualizado else it
            }
            _aviso.value = if (actualizado.etapa == "CERRADO") {
                "Caso archivado"
            } else {
                "El caso avanzó a " + actualizado.etapa.lowercase()
            }
        }
    }

    fun cargarPendientes() {
        ejecutar { _pendientes.value = sesion.listarPendientes() }
    }

    fun aprobarAsegurado(id: String) {
        ejecutar {
            val aprobado = sesion.aprobarAsegurado(id)
            _pendientes.value = _pendientes.value.filterNot { it.id == aprobado.id }
            _aviso.value = aprobado.nombre + " ya puede entrar"
        }
    }

    fun rechazarAsegurado(id: String) {
        ejecutar {
            val rechazado = sesion.rechazarAsegurado(id)
            _pendientes.value = _pendientes.value.filterNot { it.id == rechazado.id }
            _aviso.value = "Cuenta de " + rechazado.nombre + " rechazada"
        }
    }

    fun cargarGestiones(siniestroId: String) {
        ejecutar { _gestiones.value = repositorio.listarGestiones(siniestroId) }
    }

    fun agregarGestion(
        siniestroId: String,
        responsable: String,
        descripcion: String,
        visible: Boolean
    ) {
        ejecutar {
            repositorio.agregarGestion(siniestroId, responsable, descripcion, visible)
            _gestiones.value = repositorio.listarGestiones(siniestroId)
            _aviso.value = "Gestión registrada"
        }
    }

    fun cargarEvidencias(siniestroId: String) {
        ejecutar { _evidencias.value = archivos.listarEvidencias(siniestroId) }
    }

    fun subirEvidencia(
        siniestroId: String,
        tipoDocumento: String,
        subidoPor: String,
        nombreArchivo: String,
        mime: String,
        contenido: ByteArray
    ) {
        ejecutar {
            archivos.subirEvidencia(
                siniestroId, tipoDocumento, subidoPor, nombreArchivo, mime, contenido
            )
            _evidencias.value = archivos.listarEvidencias(siniestroId)
            _aviso.value = "Documento enviado al servidor"
        }
    }

    suspend fun vistaPreviaEvidencia(evidenciaId: String, lado: Int): Bitmap? {
        val evidencia = _evidencias.value.firstOrNull { it.id == evidenciaId } ?: return null
        return runCatching {
            VistaPreviaArchivo.decodificar(
                archivos.descargarEvidencia(evidenciaId),
                evidencia.mime,
                lado
            )
        }.getOrNull()
    }

    fun eliminarEvidencia(siniestroId: String, evidenciaId: String) {
        ejecutar {
            archivos.eliminarEvidencia(evidenciaId)
            _evidencias.value = archivos.listarEvidencias(siniestroId)
            _aviso.value = "Documento eliminado"
        }
    }

    fun crearCompania(nombre: String, plazo: Int) {
        ejecutar {
            catalogos.crearCompania(nombre, plazo)
            _companias.value = catalogos.listarCompanias()
            _aviso.value = "Compañía agregada"
        }
    }

    fun crearTipo(nombre: String, esVehiculo: Boolean, documentos: List<String>) {
        ejecutar {
            catalogos.crearTipo(nombre, esVehiculo, documentos)
            _tipos.value = catalogos.listarTipos()
            _aviso.value = "Tipo de siniestro agregado"
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                accion()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ocurrió un error inesperado"
            } finally {
                _cargando.value = false
            }
        }
    }
}
