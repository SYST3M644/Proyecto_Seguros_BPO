package cl.duocuc.segurosbpo.data.remote

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class UsuarioResponse(
    val id: String,
    val nombre: String,
    val correo: String,
    val rut: String?,
    val equipo: String?,
    val rolId: String,
    val rolNombre: String,
    val avatarUrl: String?
)

data class RolResponse(
    val id: String,
    val nombre: String
)

data class CompaniaResponse(
    val id: String,
    val codigo: String,
    val nombre: String,
    val activa: Boolean,
    val plazoDiasHabiles: Int
)

data class TipoSiniestroResponse(
    val id: String,
    val codigo: String,
    val nombre: String,
    val esVehiculo: Boolean,
    val documentos: List<String>
)

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rut: String,
    val equipo: String? = null,
    val rolId: String? = null,
    val pregunta: String? = null,
    val respuesta: String? = null
)

data class PreguntaResponse(
    val correo: String,
    val pregunta: String
)

data class RecuperarRequest(
    val correo: String,
    val respuesta: String,
    val nueva: String
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val correo: String,
    val rut: String,
    val equipo: String? = null,
    val rolId: String? = null
)

data class CambiarContrasenaRequest(
    val actual: String,
    val nueva: String
)

data class RegionResponse(
    val id: String,
    val codigo: String,
    val nombre: String,
    val orden: Int
)

data class ComunaResponse(
    val id: String,
    val regionCodigo: String,
    val nombre: String
)

data class CompaniaRequest(
    val nombre: String,
    val plazoDiasHabiles: Int,
    val activa: Boolean = true
)

data class TipoSiniestroRequest(
    val nombre: String,
    val esVehiculo: Boolean,
    val documentos: List<String>
)

data class RegistrarSiniestroRequest(
    val aseguradoId: String,
    val companiaCodigo: String,
    val tipoCodigo: String,
    val fechaOcurrencia: String,
    val comuna: String,
    val descripcion: String,
    val huboTerceros: Boolean
)

data class AsignarRequest(
    val liquidadorId: String
)

data class AvanzarRequest(
    val resultado: String,
    val mensajeCierre: String
)

data class GestionRequest(
    val responsable: String,
    val descripcion: String,
    val visibleAsegurado: Boolean
)

data class SiniestroResponse(
    val id: String,
    val numero: String,
    val aseguradoId: String,
    val aseguradoNombre: String?,
    val companiaCodigo: String,
    val companiaNombre: String?,
    val plazoDiasHabiles: Int?,
    val tipoCodigo: String,
    val tipoNombre: String?,
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val comuna: String,
    val descripcion: String,
    val huboTerceros: Boolean,
    val etapa: String,
    val liquidadorId: String?,
    val liquidadorNombre: String?,
    val resultadoCierre: String?,
    val mensajeCierre: String?,
    val ultimaActualizacion: String
)

data class CargaLiquidadorResponse(
    val liquidadorId: String,
    val liquidadorNombre: String,
    val equipo: String,
    val abiertos: Int,
    val cerrados: Int
)

data class GestionResponse(
    val id: String,
    val siniestroId: String,
    val fecha: String,
    val responsable: String,
    val descripcion: String,
    val visibleAsegurado: Boolean
)

data class EvidenciaResponse(
    val id: String,
    val siniestroId: String,
    val tipoDocumento: String,
    val nombreArchivo: String,
    val mime: String,
    val tamanoBytes: Int,
    val fechaRegistro: String,
    val subidoPor: String,
    val archivoUrl: String
)

data class AvisoResponse(
    val id: String,
    val siniestroId: String,
    val numero: String,
    val destinatarioId: String,
    val mensaje: String,
    val fecha: String,
    val leido: Boolean
)

data class SolicitudResponse(
    val id: String,
    val siniestroId: String,
    val tipoDocumento: String,
    val comentario: String?,
    val solicitadoPor: String?,
    val fecha: String,
    val estado: String
)

data class SolicitudRequest(
    val tipoDocumento: String,
    val comentario: String,
    val solicitadoPor: String
)

data class ConsultaResponse(
    val id: String,
    val siniestroId: String,
    val pregunta: String,
    val respuesta: String?,
    val fechaPregunta: String,
    val fechaRespuesta: String?
)

data class ConsultaRequest(
    val texto: String
)

data class EvaluacionResponse(
    val id: String,
    val siniestroId: String,
    val nota: Int,
    val comentario: String?,
    val fecha: String
)

data class EvaluacionRequest(
    val nota: Int,
    val comentario: String
)

data class MotivoRequest(
    val texto: String
)
