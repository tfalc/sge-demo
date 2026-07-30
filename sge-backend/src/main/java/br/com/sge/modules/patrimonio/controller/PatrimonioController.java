package br.com.sge.modules.patrimonio.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.patrimonio.dto.AtualizarPatrimonioItemRequest;
import br.com.sge.modules.patrimonio.dto.CriarPatrimonioItemRequest;
import br.com.sge.modules.patrimonio.service.PatrimonioService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patrimonio/itens")
@Tag(name = "patrimonio", description = "Inventario patrimonial")
public class PatrimonioController {

    private final PatrimonioService patrimonioService;

    public PatrimonioController(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
    }

    @Operation(summary = "Listar itens patrimoniais")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Itens patrimoniais", patrimonioService.listar()));
    }

    @Operation(summary = "Cadastrar item patrimonial")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> criar(@Valid @RequestBody CriarPatrimonioItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item cadastrado", patrimonioService.criar(request)));
    }

    @Operation(summary = "Atualizar item patrimonial")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @RequestBody AtualizarPatrimonioItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item atualizado", patrimonioService.atualizar(id, request)));
    }

    @Operation(summary = "Excluir item patrimonial")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> excluir(@PathVariable UUID id) {
        patrimonioService.excluir(id);
        return ResponseEntity.ok(ApiResponse.ok("Item excluido", null));
    }
}
