package cl.duoc.seguros.siniestros;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SiniestrosApiTest {
    private static final String RUT = "15.842.371-5";
    private static final String OTRO_RUT = "9.418.552-1";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean CatalogosCliente catalogos;

    @BeforeEach
    void prepararCatalogos() {
        when(catalogos.compania("ASG-01")).thenReturn(
            new CatalogosCliente.Compania("ASG-01", "Seguros Cordillera", List.of("VEHICULO", "HOGAR"), 10, true));
        when(catalogos.compania("ASG-05")).thenReturn(
            new CatalogosCliente.Compania("ASG-05", "Protege Seguros", List.of("HOGAR"), 12, false));
        when(catalogos.tipo("TS-01")).thenReturn(
            new CatalogosCliente.Tipo("TS-01", "Vehículo - choque o colisión", "VEHICULO", List.of("Fotografías")));
        when(catalogos.tipo("TS-04")).thenReturn(
            new CatalogosCliente.Tipo("TS-04", "Hogar - daño por agua", "HOGAR", List.of("Fotografías")));
    }

    private String crear(String rut) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/v1/siniestros")
                .header("X-Actor-Id", rut).header("X-Role", "ASEGURADO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"compania":"ASG-01","tipo":"TS-01","fechaOcurrencia":"2026-09-02",
                     "comuna":"Maipú","descripcion":"Choque leve con daño en puerta"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("Recibido"))
            .andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("numero").asText();
    }

    @Test
    void reporteYConsultaConContratoAndroid() throws Exception {
        String numero = crear(RUT);
        mvc.perform(get("/api/v1/siniestros/{numero}", numero)
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(numero))
            .andExpect(jsonPath("$.tipo").value("Vehículo - choque o colisión"))
            .andExpect(jsonPath("$.fechaOcurrencia").value("2026-09-02"))
            .andExpect(jsonPath("$.proximoPaso").exists());
        mvc.perform(get("/api/v1/siniestros/{numero}/historial", numero)
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].descripcion").exists());
    }

    @Test
    void noPermiteLeerCasosDeOtroAsegurado() throws Exception {
        String numero = crear(RUT);
        mvc.perform(get("/api/v1/siniestros/{numero}", numero)
                .header("X-Actor-Id", OTRO_RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/siniestros")
                .header("X-Actor-Id", OTRO_RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + numero + "')]").isEmpty());
    }

    @Test
    void impideFechaFuturaYCompaniaInactiva() throws Exception {
        String futuro = LocalDate.now(ZoneId.of("America/Santiago")).plusDays(2).toString();
        String peticion = """
            {"compania":"ASG-01","tipo":"TS-01","fechaOcurrencia":"%s",
             "comuna":"Maipú","descripcion":"Prueba"}
            """.formatted(futuro);
        mvc.perform(post("/api/v1/siniestros")
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO")
                .contentType(MediaType.APPLICATION_JSON).content(peticion))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/siniestros")
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"compania":"ASG-05","tipo":"TS-04","fechaOcurrencia":"2026-09-02",
                     "comuna":"Maipú","descripcion":"Prueba"}
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void asignacionSolicitudesYEtapasCumplenReglas() throws Exception {
        String numero = crear(RUT);
        mvc.perform(post("/api/v1/siniestros/{numero}/asignacion", numero)
                .header("X-Actor-Id", "ADM-01").header("X-Role", "ANALISTA")
                .contentType(MediaType.APPLICATION_JSON).content("{\"liquidadorId\":\"LIQ-02\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/siniestros/{numero}/asignacion", numero)
                .header("X-Actor-Id", "ADM-01").header("X-Role", "ANALISTA")
                .contentType(MediaType.APPLICATION_JSON).content("{\"liquidadorId\":\"LIQ-01\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("En evaluación"));
        MvcResult peticion = mvc.perform(post("/api/v1/siniestros/{numero}/solicitudes", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoDocumento\":\"Fotografía del daño\",\"comentario\":\"Adjuntar fotografía\"}"))
            .andExpect(status().isCreated()).andReturn();
        Long solicitudId = json.readTree(peticion.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(post("/api/v1/siniestros/{numero}/etapa", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR")
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"En liquidación\"}"))
            .andExpect(status().isConflict());
        MockMultipartFile pdf = new MockMultipartFile("archivo", "evidencia.pdf", "application/pdf",
            "%PDF-1.4 prueba".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/v1/siniestros/{numero}/documentos", numero)
                .file(pdf).param("tipo", "Fotografía del daño").param("solicitudId", solicitudId.toString())
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/siniestros/{numero}/etapa", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR")
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"En liquidación\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("En liquidación"));
        mvc.perform(post("/api/v1/siniestros/{numero}/etapa", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR")
                .contentType(MediaType.APPLICATION_JSON).content(
                    "{\"etapa\":\"Cerrado\",\"resultado\":\"Aprobado\",\"mensajeCierre\":\"La compañía aprobó el caso\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("Cerrado"))
            .andExpect(jsonPath("$.resultadoCierre").value("Aprobado"))
            .andExpect(jsonPath("$.mensajeCierre").value("La compañía aprobó el caso"));
        mvc.perform(multipart("/api/v1/siniestros/{numero}/documentos", numero)
                .file(pdf).param("tipo", "Fotografía del daño")
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isConflict());
    }

    @Test
    void gestionInternaNoApareceAlAsegurado() throws Exception {
        String numero = crear(RUT);
        mvc.perform(post("/api/v1/siniestros/{numero}/asignacion", numero)
                .header("X-Actor-Id", "ADM-01").header("X-Role", "ANALISTA")
                .contentType(MediaType.APPLICATION_JSON).content("{\"liquidadorId\":\"LIQ-01\"}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/siniestros/{numero}/gestiones", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Dato exclusivo del equipo\",\"visibleAsegurado\":false}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/siniestros/{numero}/historial", numero)
                .header("X-Actor-Id", RUT).header("X-Role", "ASEGURADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.descripcion == 'Dato exclusivo del equipo')]").isEmpty());
        mvc.perform(get("/api/v1/siniestros/{numero}/historial", numero)
                .header("X-Actor-Id", "LIQ-01").header("X-Role", "LIQUIDADOR"))
            .andExpect(jsonPath("$[?(@.descripcion == 'Dato exclusivo del equipo')]").isNotEmpty());
    }
}
