package br.com.sge.modules.admin.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.admin.dto.AtualizarUsuarioRequest;
import br.com.sge.modules.admin.service.UsuarioAdminService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "admin", description = "Gestao de usuarios do sistema")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @Operation(summary = "Listar usuarios")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Usuarios", usuarioAdminService.listarUsuarios()));
    }

    @Operation(summary = "Atualizar perfil e status do usuario")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario atualizado", usuarioAdminService.atualizarUsuario(id, request)));
    }
}
