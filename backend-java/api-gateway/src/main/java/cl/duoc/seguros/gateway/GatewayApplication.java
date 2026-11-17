package cl.duoc.seguros.gateway;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    RouteLocator rutas(RouteLocatorBuilder builder,
                        @Value("${services.siniestros-url}") String siniestros,
                        @Value("${services.catalogos-url}") String catalogos) {
        return builder.routes()
            .route("siniestros", r -> r.path("/api/v1/siniestros", "/api/v1/siniestros/**", "/api/v1/avisos", "/api/v1/avisos/**")
                .uri(siniestros))
            .route("catalogos", r -> r.path("/api/v1/companias", "/api/v1/companias/**", "/api/v1/tipos-siniestro", "/api/v1/tipos-siniestro/**")
                .uri(catalogos))
            .route("openapi-siniestros", r -> r.path("/openapi/siniestros")
                .filters(f -> f.setPath("/v3/api-docs")).uri(siniestros))
            .route("openapi-catalogos", r -> r.path("/openapi/catalogos")
                .filters(f -> f.setPath("/v3/api-docs")).uri(catalogos))
            .build();
    }

    @RestController
    static class EstadoGateway {
        @GetMapping("/api/v1/health")
        Map<String, String> health() {
            return Map.of("status", "UP", "service", "api-gateway");
        }
    }
}
