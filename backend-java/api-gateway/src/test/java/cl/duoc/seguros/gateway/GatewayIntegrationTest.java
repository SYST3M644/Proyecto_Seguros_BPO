package cl.duoc.seguros.gateway;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest {
    static final HttpServer SINIESTROS = servidor("siniestros");
    static final HttpServer CATALOGOS = servidor("catalogos");

    private static HttpServer servidor(String nombre) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                String body = exchange.getRequestURI().getPath().equals("/v3/api-docs")
                    ? "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"" + nombre + "\",\"version\":\"v1\"},\"paths\":{}}"
                    : "{\"service\":\"" + nombre + "\",\"role\":\"" +
                        exchange.getRequestHeaders().getFirst("X-Role") + "\"}";
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void destinos(DynamicPropertyRegistry registry) {
        registry.add("services.siniestros-url", () -> "http://127.0.0.1:" + SINIESTROS.getAddress().getPort());
        registry.add("services.catalogos-url", () -> "http://127.0.0.1:" + CATALOGOS.getAddress().getPort());
    }

    @AfterAll
    static void cerrar() {
        SINIESTROS.stop(0);
        CATALOGOS.stop(0);
    }

    @LocalServerPort int puerto;
    HttpClient cliente = HttpClient.newHttpClient();

    HttpResponse<String> consultar(String path, String rol) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + path));
        if (rol != null) builder.header("X-Role", rol);
        return cliente.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void enrutaSiniestrosConEncabezados() throws Exception {
        var respuesta = consultar("/api/v1/siniestros/SIN-26-000001", "ASEGURADO");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"service\":\"siniestros\""));
        assertTrue(respuesta.body().contains("\"role\":\"ASEGURADO\""));
    }

    @Test
    void enrutaCatalogos() throws Exception {
        var respuesta = consultar("/api/v1/companias/ASG-01", null);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"service\":\"catalogos\""));
    }

    @Test
    void publicaLasDosEspecificacionesDeSwagger() throws Exception {
        var siniestros = consultar("/openapi/siniestros", null);
        var catalogos = consultar("/openapi/catalogos", null);
        assertEquals(200, siniestros.statusCode());
        assertEquals(200, catalogos.statusCode());
        assertTrue(siniestros.body().contains("\"title\":\"siniestros\""));
        assertTrue(catalogos.body().contains("\"title\":\"catalogos\""));
        var swaggerConfig = consultar("/v3/api-docs/swagger-config", null);
        assertEquals(200, swaggerConfig.statusCode());
        assertTrue(swaggerConfig.body().contains("/openapi/siniestros"));
        assertTrue(swaggerConfig.body().contains("/openapi/catalogos"));
    }

    @Test
    void elGatewayTieneSuPropiaRutaDeEstado() throws Exception {
        var respuesta = consultar("/api/v1/health", null);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("api-gateway"));
    }
}
