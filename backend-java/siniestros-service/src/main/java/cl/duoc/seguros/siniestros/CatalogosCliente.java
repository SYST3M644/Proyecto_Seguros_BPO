package cl.duoc.seguros.siniestros;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CatalogosCliente {
    public record Compania(String codigo, String nombre, List<String> lineas, int plazoEvaluacionDias, boolean activa) {}
    public record Tipo(String codigo, String nombre, String linea, List<String> documentosHabituales) {}

    private final RestClient cliente;

    public CatalogosCliente(RestClient.Builder builder, @Value("${catalogos.base-url}") String baseUrl) {
        this.cliente = builder.baseUrl(baseUrl).build();
    }

    public Compania compania(String codigo) {
        return obtener("/api/v1/companias/{codigo}", codigo, Compania.class);
    }

    public Tipo tipo(String codigo) {
        return obtener("/api/v1/tipos-siniestro/{codigo}", codigo, Tipo.class);
    }

    private <T> T obtener(String url, String codigo, Class<T> clase) {
        try {
            T respuesta = cliente.get().uri(url, codigo).retrieve().body(clase);
            if (respuesta == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catálogo sin respuesta");
            }
            return respuesta;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Código de catálogo inexistente: " + codigo);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible consultar catálogos", e);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catálogos no disponible", e);
        }
    }
}
