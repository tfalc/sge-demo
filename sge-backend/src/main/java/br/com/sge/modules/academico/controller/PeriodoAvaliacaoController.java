package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.service.AcademicoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periodos-avaliacao")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class PeriodoAvaliacaoController {

    private final AcademicoService academicoService;

    public PeriodoAvaliacaoController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @Operation(summary = "Listar periodos de avaliacao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listarPeriodos() {
        return ResponseEntity.ok(ApiResponse.ok("Periodos encontrados", academicoService.listarPeriodos()));
    }
}
