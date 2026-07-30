package br.com.sge.modules.comunicacao.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.comunicacao.dto.AtualizarComunicadoRequest;
import br.com.sge.modules.comunicacao.dto.CriarComunicadoRequest;
import br.com.sge.modules.comunicacao.service.ComunicacaoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunicados")
@Tag(name = "comunicacao", description = "Comunicados, cardapio e agenda escolar")
public class ComunicadoController {

    private final ComunicacaoService comunicacaoService;

    public ComunicadoController(ComunicacaoService comunicacaoService) {
        this.comunicacaoService = comunicacaoService;
    }

    @Operation(
            summary = "Listar comunicados",
            description = "Sem filtros: todos (secretaria). Com audiencia/turmaId: segmentado para portais.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar(
            @RequestParam(required = false) String audiencia, @RequestParam(required = false) UUID turmaId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Comunicados encontrados", comunicacaoService.listarComunicados(audiencia, turmaId)));
    }

    @Operation(summary = "Publicar comunicado")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> criar(
            @Valid @RequestBody CriarComunicadoRequest request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        return ResponseEntity.ok(
                ApiResponse.ok("Comunicado publicado", comunicacaoService.criarComunicado(request, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarComunicadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Comunicado atualizado", comunicacaoService.atualizarComunicado(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        comunicacaoService.excluirComunicado(id);
        return ResponseEntity.ok(ApiResponse.ok("Comunicado excluido", null));
    }
}
