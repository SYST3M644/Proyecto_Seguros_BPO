package cl.duoc.seguros.siniestros;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Siniestros Seguros BPO", version = "v1", description = "Denuncia, seguimiento, documentos, etapas e historial (identidad simulada de uso académico)"), servers = @Server(url = "/"))
public class SiniestrosApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiniestrosApplication.class, args);
    }
}
