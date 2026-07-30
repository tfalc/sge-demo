package br.com.sge.modules.notificacoes.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.notificacoes.dto.NotificacaoResponse;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "notificacoes", description = "Central de notificacoes in-app")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Operation(summary = "Listar notificacoes do usuario logado")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificacaoResponse>>> listar(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Notificacoes",
                notificacaoService.listarParaUsuario(requireEmail(authentication))));
    }

    @Operation(summary = "Resumo (quantidade nao lidas)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/resumo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resumo(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Resumo",
                notificacaoService.resumoParaUsuario(requireEmail(authentication))));
    }

    @Operation(summary = "Marcar notificacao como lida")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/{id}/lida")
    public ResponseEntity<ApiResponse<NotificacaoResponse>> marcarLida(
            @PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Notificacao lida",
                notificacaoService.marcarComoLida(id, requireEmail(authentication))));
    }

    @Operation(summary = "Marcar todas como lidas")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/marcar-todas-lidas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> marcarTodasLidas(Authentication authentication) {
        int atualizadas = notificacaoService.marcarTodasComoLidas(requireEmail(authentication));
        return ResponseEntity.ok(ApiResponse.ok("Todas marcadas como lidas", Map.of("atualizadas", atualizadas)));
    }

    private static String requireEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        return authentication.getName();
    }
}
