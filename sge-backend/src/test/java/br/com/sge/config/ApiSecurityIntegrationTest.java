package br.com.sge.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.sge.support.DevH2TestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiSecurityIntegrationTest {

    @DynamicPropertySource
    static void isolatedH2(DynamicPropertyRegistry registry) {
        DevH2TestSupport.registerIsolatedH2(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void paiNaoPodeCadastrarDisciplina() throws Exception {
        String token = login("pai@sge.com");

        mockMvc.perform(
                        post("/api/academico/disciplinas")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Quimica\",\"codigo\":\"QUI\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void professorNaoPodeListarCadastroAlunos() throws Exception {
        String token = login("prof@sge.com");

        mockMvc.perform(get("/api/cadastro/alunos").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void secretariaPodeListarDisciplinas() throws Exception {
        String token = login("admin@sge.com");

        mockMvc.perform(get("/api/academico/disciplinas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void alunoPodeFazerLoginEConsultarMe() throws Exception {
        String token = login("aluno@sge.com");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void secretariaPodeCriarHorario() throws Exception {
        String token = login("admin@sge.com");

        mockMvc.perform(
                        post("/api/horarios")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "turmaId": "55555555-5555-5555-5555-555555555555",
                                          "diaSemana": 4,
                                          "horaInicio": "14:00",
                                          "horaFim": "15:00",
                                          "disciplinaId": "70707070-7070-7070-7070-707070707070",
                                          "professorId": "60606060-6060-6060-6060-606060606060"
                                        }
                                        """))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        var loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"" + email + "\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
