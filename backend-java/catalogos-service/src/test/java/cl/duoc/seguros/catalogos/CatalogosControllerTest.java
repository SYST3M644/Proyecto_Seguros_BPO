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
class CatalogosControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void companiaInactivaNoSeOculta() throws Exception {
        mvc.perform(get("/api/v1/companias/ASG-05"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activa").value(false))
            .andExpect(jsonPath("$.lineas[0]").value("HOGAR"));
    }

    @Test
    void tipoIncluyeLineaYDocumentos() throws Exception {
        mvc.perform(get("/api/v1/tipos-siniestro/TS-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.linea").value("VEHICULO"))
            .andExpect(jsonPath("$.documentosHabituales.length()").value(3));
    }

    @Test
    void codigoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/v1/companias/NO-EXISTE")).andExpect(status().isNotFound());
    }
}
