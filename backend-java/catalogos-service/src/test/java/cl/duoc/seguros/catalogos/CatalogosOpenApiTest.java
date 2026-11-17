package cl.duoc.seguros.catalogos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogosOpenApiTest {
    @Autowired MockMvc mvc;

    @Test
    void publicaContratoOpenApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Catálogos Seguros BPO"))
            .andExpect(jsonPath("$.paths['/api/v1/companias']").exists())
            .andExpect(jsonPath("$.paths['/api/v1/tipos-siniestro']").exists());
    }
}
