package br.com.sge.modules.notificacoes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.support.DevH2TestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class NotificacaoControllerIntegrationTest {

    @DynamicPropertySource
    static void isolatedH2(DynamicPropertyRegistry registry) {
        DevH2TestSupport.registerIsolatedH2(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String accessToken;

    @BeforeEach
    void login() throws Exception {
        var loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"pai@sge.com\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        accessToken = root.path("data").path("accessToken").asText();
    }

    @Test
    @Transactional
    void listarResumoEMarcarLida() throws Exception {
        Usuario pai = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue("pai@sge.com")
                .orElseThrow();
        notificacaoService.criar(
                pai,
                TipoNotificacao.COBRANCA_GERADA,
                "Cobranca teste",
                "Detalhe",
                "/pais/cobrancas",
                UUID.randomUUID());

        mockMvc.perform(get("/api/notificacoes/resumo").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.naoLidas").value(1));

        var listResponse = mockMvc.perform(get("/api/notificacoes").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].titulo").value("Cobranca teste"))
                .andReturn();

        JsonNode lista = objectMapper.readTree(listResponse.getResponse().getContentAsString());
        String id = lista.path("data").get(0).path("id").asText();

        mockMvc.perform(post("/api/notificacoes/" + id + "/lida").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lida").value(true));

        mockMvc.perform(get("/api/notificacoes/resumo").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.naoLidas").value(0));
    }
}
