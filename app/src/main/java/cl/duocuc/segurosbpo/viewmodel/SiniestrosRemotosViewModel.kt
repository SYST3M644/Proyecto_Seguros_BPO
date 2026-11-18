package cl.duocuc.segurosbpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.segurosbpo.data.ArchivoRepository
import cl.duocuc.segurosbpo.data.CatalogoRepository
import cl.duocuc.segurosbpo.data.SesionRepository
import cl.duocuc.segurosbpo.data.SiniestroRepository
import cl.duocuc.segurosbpo.data.remote.CompaniaResponse
import cl.duocuc.segurosbpo.data.remote.EvidenciaResponse
import cl.duocuc.segurosbpo.data.remote.GestionResponse
import cl.duocuc.segurosbpo.data.remote.RegistrarSiniestroRequest
import cl.duocuc.segurosbpo.data.remote.SiniestroResponse
import cl.duocuc.segurosbpo.data.remote.TipoSiniestroResponse
import cl.duocuc.segurosbpo.data.remote.UsuarioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SiniestrosRemotosViewModel(
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

    private val _liquidadores = MutableStateFlow<List<UsuarioResponse>>(emptyList())
    val liquidadores = _liquidadores.asStateFlow()

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
        }
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
            _aviso.value = "Caso asignado a " + nombre
        }
    }

    fun avanzar(siniestroId: String, resultado: String, mensajeCierre: String) {
        ejecutar {
            val actualizado = repositorio.avanzar(siniestroId, resultado, mensajeCierre)
            _siniestros.value = _siniestros.value.map {
                if (it.id == actualizado.id) actualizado else it
            }
            _aviso.value = "El caso avanzó a " + actualizado.etapa.lowercase()
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
