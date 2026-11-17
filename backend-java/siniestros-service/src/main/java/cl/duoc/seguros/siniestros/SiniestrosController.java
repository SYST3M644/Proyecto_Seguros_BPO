package cl.duoc.seguros.siniestros;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class SiniestrosController {
    private final SiniestrosService service;

    public SiniestrosController(SiniestrosService service) {
        this.service = service;
    }

    private Actor actor(String id, String rol) {
        return new Actor(id, rol);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "siniestros");
    }

    @PostMapping("/siniestros")
    public ResponseEntity<SiniestroResponse> reportar(@RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody ReporteRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reportar(actor(id, rol), body));
    }

    @GetMapping("/siniestros")
    public List<SiniestroResponse> listar(@RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        return service.listar(actor(id, rol));
    }

    @GetMapping("/siniestros/{numero}")
    public SiniestroResponse detalle(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        return service.detalle(numero, actor(id, rol));
    }

    @GetMapping("/siniestros/{numero}/historial")
    public List<GestionResponse> historial(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        return service.historial(numero, actor(id, rol));
    }

    @PostMapping("/siniestros/{numero}/asignacion")
    public SiniestroResponse asignar(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody AsignacionRequest body) {
        return service.asignar(numero, actor(id, rol), body);
    }

    @PostMapping("/siniestros/{numero}/gestiones")
    public GestionResponse gestionar(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody GestionRequest body) {
        return service.gestionar(numero, actor(id, rol), body);
    }

    @PostMapping("/siniestros/{numero}/solicitudes")
    public ResponseEntity<SolicitudResponse> solicitar(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody SolicitudRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(numero, actor(id, rol), body));
    }

    @GetMapping("/siniestros/{numero}/solicitudes")
    public List<SolicitudResponse> solicitudes(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        return service.solicitudes(numero, actor(id, rol));
    }

    @PostMapping(value = "/siniestros/{numero}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> adjuntar(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestParam String tipo, @RequestParam(required = false) Long solicitudId,
        @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adjuntar(numero, actor(id, rol), tipo, solicitudId, archivo));
    }

    @GetMapping("/siniestros/{numero}/documentos")
    public List<DocumentoResponse> documentos(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        return service.documentos(numero, actor(id, rol));
    }

    @GetMapping("/siniestros/{numero}/documentos/{documentoId}")
    public ResponseEntity<byte[]> descargar(@PathVariable String numero, @PathVariable Long documentoId,
        @RequestHeader("X-Actor-Id") String id, @RequestHeader("X-Role") String rol) {
        DocumentoAdjunto doc = service.descargar(numero, documentoId, actor(id, rol));
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(doc.tipoMime))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidencia-" + doc.id + "\"")
            .body(doc.contenido);
    }

    @PostMapping("/siniestros/{numero}/etapa")
    public SiniestroResponse etapa(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody CambioEtapaRequest body) {
        return service.avanzar(numero, actor(id, rol), body);
    }

    @PostMapping("/siniestros/{numero}/desistimiento")
    public SiniestroResponse desistir(@PathVariable String numero, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol, @RequestBody DesistimientoRequest body) {
        return service.desistir(numero, actor(id, rol), body);
    }

    @GetMapping("/avisos")
    public List<AvisoResponse> avisos(@RequestHeader("X-Actor-Id") String id, @RequestHeader("X-Role") String rol) {
        return service.avisos(actor(id, rol));
    }

    @PatchMapping("/avisos/{avisoId}/leido")
    public ResponseEntity<Void> marcarLeido(@PathVariable Long avisoId, @RequestHeader("X-Actor-Id") String id,
        @RequestHeader("X-Role") String rol) {
        service.leerAviso(avisoId, actor(id, rol));
        return ResponseEntity.noContent().build();
    }
}
