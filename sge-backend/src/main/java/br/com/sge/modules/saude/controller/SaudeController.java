package br.com.sge.modules.saude.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.saude.dto.CriarAgendamentoSaudeRequest;
import br.com.sge.modules.saude.repository.ProfissionalSaudeRepository;
import br.com.sge.modules.saude.service.SaudeService;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saude")
@Tag(name = "saude", description = "Agenda e atendimentos de saude")
public class SaudeController {

    private final SaudeService saudeService;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;

    public SaudeController(
            SaudeService saudeService,
            ProfissionalSaudeRepository profissionalRepository,
            UsuarioRepository usuarioRepository) {
        this.saudeService = saudeService;
        this.profissionalRepository = profissionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Agenda do profissional de saude")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/agenda/{profissionalId}")
    public ResponseEntity<ApiResponse<Object>> agenda(@PathVariable UUID profissionalId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Agenda encontrada", saudeService.listarAgendaProfissional(profissionalId)));
    }

    @Operation(summary = "Historico de atendimentos do aluno", description = "Pais veem apenas data e status (sem observacoes privadas).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/alunos/{alunoId}/historico")
    public ResponseEntity<ApiResponse<Object>> historico(
            @PathVariable UUID alunoId, @RequestParam(defaultValue = "false") boolean incluirPrivado) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Historico encontrado", saudeService.listarHistoricoAluno(alunoId, incluirPrivado)));
    }

    @Operation(summary = "Agendar atendimento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/agendamentos")
    public ResponseEntity<ApiResponse<Object>> criarAgendamento(
            @Valid @RequestBody CriarAgendamentoSaudeRequest request, Authentication authentication) {
        UUID profissionalId = resolverProfissionalId(authentication);
        return ResponseEntity.ok(
                ApiResponse.ok("Agendamento criado", saudeService.criarAgendamento(request, profissionalId)));
    }

    private UUID resolverProfissionalId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        var usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        return profissionalRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profissional de saude nao vinculado ao usuario"))
                .getId();
    }
}
