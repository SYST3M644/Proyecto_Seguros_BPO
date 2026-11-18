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
