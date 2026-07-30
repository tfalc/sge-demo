package br.com.sge.modules.notificacoes;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.support.DevH2TestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
class NotificacaoServiceTest {

    @DynamicPropertySource
    static void isolatedH2(DynamicPropertyRegistry registry) {
        DevH2TestSupport.registerIsolatedH2(registry);
    }

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @Transactional
    void criarListarEMarcarComoLida() {
        Usuario pai = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue("pai@sge.com")
                .orElseThrow();

        notificacaoService.criar(
                pai,
                TipoNotificacao.COMUNICADO_NOVO,
                "Teste",
                "Mensagem de teste",
                "/pais/comunicacao",
                UUID.randomUUID());

        var lista = notificacaoService.listarParaUsuario("pai@sge.com");
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().titulo()).isEqualTo("Teste");
        assertThat(lista.getFirst().lida()).isFalse();

        var resumo = notificacaoService.resumoParaUsuario("pai@sge.com");
        assertThat(resumo.get("naoLidas")).isEqualTo(1L);

        var lida = notificacaoService.marcarComoLida(lista.getFirst().id(), "pai@sge.com");
        assertThat(lida.lida()).isTrue();

        resumo = notificacaoService.resumoParaUsuario("pai@sge.com");
        assertThat(resumo.get("naoLidas")).isEqualTo(0L);
    }
}
