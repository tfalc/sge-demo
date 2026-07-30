package br.com.sge.modules.admin.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.admin.dto.AtualizarAcessosMenuRequest;
import br.com.sge.modules.admin.service.PerfilAcessoMenuService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/acessos-menu")
@Tag(name = "admin-acessos", description = "Matriz de menus superiores por perfil")
public class PerfilAcessoMenuController {

    private final PerfilAcessoMenuService service;

    public PerfilAcessoMenuController(PerfilAcessoMenuService service) {
        this.service = service;
    }

    @Operation(summary = "Obter matriz de acessos de menu")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> obter() {
        return ResponseEntity.ok(ApiResponse.ok("Matriz de acessos", service.obterMatriz()));
    }

    @Operation(summary = "Salvar matriz de acessos (somente ADMIN)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<Object>> salvar(@RequestBody AtualizarAcessosMenuRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Acessos atualizados", service.salvarMatriz(request)));
    }

    @Operation(summary = "Restaurar defaults de acesso (somente ADMIN)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/defaults")
    public ResponseEntity<ApiResponse<Object>> restaurarDefaults() {
        return ResponseEntity.ok(ApiResponse.ok("Defaults restaurados", service.restaurarDefaults()));
    }
}
