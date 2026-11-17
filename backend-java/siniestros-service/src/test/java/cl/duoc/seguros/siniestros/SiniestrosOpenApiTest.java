package cl.duoc.seguros.siniestros;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SiniestrosOpenApiTest {
    @Autowired MockMvc mvc;

    @Test
    void publicaContratoOpenApiYParametrosDeIdentidad() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Siniestros Seguros BPO"))
            .andExpect(jsonPath("$.paths['/api/v1/siniestros']").exists())
            .andExpect(jsonPath("$.paths['/api/v1/avisos']").exists())
            .andExpect(jsonPath("$.paths['/api/v1/siniestros'].get.parameters[?(@.name == 'X-Actor-Id')]").isNotEmpty());
    }
}
