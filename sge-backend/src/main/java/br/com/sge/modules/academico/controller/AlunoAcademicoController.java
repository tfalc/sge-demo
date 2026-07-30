package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.service.AcademicoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alunos")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class AlunoAcademicoController {

    private final AcademicoService academicoService;

    public AlunoAcademicoController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @Operation(summary = "Boletim do aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#id)")
    @GetMapping("/{id}/boletim")
    public ResponseEntity<ApiResponse<Object>> boletim(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Boletim gerado", academicoService.obterBoletim(id)));
    }

    @Operation(summary = "Frequencia do aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#id)")
    @GetMapping("/{id}/frequencia")
    public ResponseEntity<ApiResponse<Object>> frequencia(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Frequencia calculada", academicoService.obterFrequencia(id)));
    }
}
