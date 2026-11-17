package cl.duoc.seguros.catalogos;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class CatalogosController {
    public record Compania(String codigo, String nombre, List<String> lineas, int plazoEvaluacionDias, boolean activa) {}
    public record TipoSiniestro(String codigo, String nombre, String linea, List<String> documentosHabituales) {}

    private static final List<Compania> COMPANIAS = List.of(
        new Compania("ASG-01", "Seguros Cordillera", List.of("VEHICULO", "HOGAR"), 10, true),
        new Compania("ASG-02", "Seguros Litoral", List.of("HOGAR"), 12, true),
        new Compania("ASG-03", "Aseguradora Patagonia Sur", List.of("VEHICULO", "HOGAR"), 10, true),
        new Compania("ASG-04", "Seguros Valle Central", List.of("VEHICULO"), 8, true),
        new Compania("ASG-05", "Protege Seguros", List.of("HOGAR"), 12, false)
    );
    private static final List<TipoSiniestro> TIPOS = List.of(
        new TipoSiniestro("TS-01", "Vehículo - choque o colisión", "VEHICULO", List.of("Fotografías de los daños", "Constancia policial", "Presupuesto de reparación")),
        new TipoSiniestro("TS-02", "Vehículo - robo", "VEHICULO", List.of("Constancia de denuncia policial", "Declaración de llaves", "Padrón del vehículo")),
        new TipoSiniestro("TS-03", "Vehículo - rotura de cristales", "VEHICULO", List.of("Fotografías del cristal", "Presupuesto de reposición")),
        new TipoSiniestro("TS-04", "Hogar - daño por agua", "HOGAR", List.of("Fotografías del daño", "Informe o boleta del gasfíter", "Presupuesto")),
        new TipoSiniestro("TS-05", "Hogar - robo", "HOGAR", List.of("Constancia policial", "Listado de especies", "Boletas o facturas")),
        new TipoSiniestro("TS-06", "Hogar - incendio", "HOGAR", List.of("Fotografías", "Certificado de Bomberos", "Presupuesto"))
    );

    @GetMapping("/health")
    public Map<String, String> salud() {
        return Map.of("status", "UP", "service", "catalogos");
    }

    @GetMapping("/companias")
    public List<Compania> companias() {
        return COMPANIAS;
    }

    @GetMapping("/companias/{codigo}")
    public Compania compania(@PathVariable String codigo) {
        return COMPANIAS.stream().filter(c -> c.codigo().equals(codigo)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compañía inexistente"));
    }

    @GetMapping("/tipos-siniestro")
    public List<TipoSiniestro> tipos() {
        return TIPOS;
    }

    @GetMapping("/tipos-siniestro/{codigo}")
    public TipoSiniestro tipo(@PathVariable String codigo) {
        return TIPOS.stream().filter(t -> t.codigo().equals(codigo)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de siniestro inexistente"));
    }
}
