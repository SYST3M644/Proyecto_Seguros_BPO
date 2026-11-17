package cl.duoc.seguros.catalogos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Catálogos Seguros BPO", version = "v1", description = "Catálogos ficticios de compañías y tipos de siniestro"), servers = @Server(url = "/"))
public class CatalogosApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogosApplication.class, args);
    }
}
