package cl.duoc.seguros.siniestros;

import java.time.LocalDate;
import java.time.LocalDateTime;

record Actor(String id, String rol) {}
record ReporteRequest(String compania, String tipo, LocalDate fechaOcurrencia, String comuna, String descripcion) {}
record AsignacionRequest(String liquidadorId) {}
record GestionRequest(String descripcion, boolean visibleAsegurado) {}
record SolicitudRequest(String tipoDocumento, String comentario) {}
record CambioEtapaRequest(String etapa, String resultado, String mensajeCierre) {}
record DesistimientoRequest(String motivo) {}
record SiniestroResponse(String id, String numero, String compania, String tipo, String fechaOcurrencia,
    String fechaReporte, String estado, String equipoAsignado, String ultimaActualizacion, String proximoPaso, boolean reporteFueraPlazo, String resultadoCierre, String mensajeCierre) {}
record GestionResponse(String id, String fechaHora, String descripcion, String estadoResultante, String responsable) {}
record SolicitudResponse(Long id, String tipo, String comentario, String fechaSolicitud, String estado) {}
record DocumentoResponse(Long id, String tipo, String nombreArchivo, String fechaCarga, Long solicitudId) {}
record AvisoResponse(Long id, String siniestro, String mensaje, String fechaHora, boolean leido) {}
