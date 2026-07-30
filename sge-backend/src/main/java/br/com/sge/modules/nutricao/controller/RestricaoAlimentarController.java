package br.com.sge.modules.nutricao.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.nutricao.dto.CriarRestricaoAlimentarRequest;
import br.com.sge.modules.nutricao.service.RestricaoAlimentarService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutricao/restricoes")
@Tag(name = "nutricao", description = "Restricoes alimentares por aluno")
public class RestricaoAlimentarController {

    private final RestricaoAlimentarService restricaoAlimentarService;

    public RestricaoAlimentarController(RestricaoAlimentarService restricaoAlimentarService) {
        this.restricaoAlimentarService = restricaoAlimentarService;
    }

    @Operation(summary = "Listar restricoes alimentares")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'NUTRICIONISTA')")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar(@RequestParam(required = false) UUID alunoId) {
        return ResponseEntity.ok(ApiResponse.ok("Restricoes", restricaoAlimentarService.listar(alunoId)));
    }

    @Operation(summary = "Cadastrar restricao alimentar")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'NUTRICIONISTA')")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> criar(@Valid @RequestBody CriarRestricaoAlimentarRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Restricao cadastrada", restricaoAlimentarService.criar(request)));
    }

    @Operation(summary = "Excluir restricao alimentar")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'NUTRICIONISTA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> excluir(@PathVariable UUID id) {
        restricaoAlimentarService.excluir(id);
        return ResponseEntity.ok(ApiResponse.ok("Restricao excluida", null));
    }
}
