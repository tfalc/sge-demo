package br.com.sge.modules.comunicacao.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.comunicacao.dto.CriarCardapioRequest;
import br.com.sge.modules.comunicacao.service.ComunicacaoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cardapio")
@Tag(name = "comunicacao", description = "Comunicados, cardapio e agenda escolar")
public class CardapioController {

    private final ComunicacaoService comunicacaoService;

    public CardapioController(ComunicacaoService comunicacaoService) {
        this.comunicacaoService = comunicacaoService;
    }

    @Operation(summary = "Cardapio do dia")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(ApiResponse.ok("Cardapio encontrado", comunicacaoService.listarCardapio(data)));
    }

    @Operation(summary = "Cadastrar item de cardapio")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> criar(
            @Valid @RequestBody CriarCardapioRequest request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Nao autenticado");
        }
        return ResponseEntity.ok(ApiResponse.ok(
                "Cardapio cadastrado", comunicacaoService.criarCardapio(request, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        comunicacaoService.excluirCardapio(id);
        return ResponseEntity.ok(ApiResponse.ok("Item excluido", null));
    }
}
