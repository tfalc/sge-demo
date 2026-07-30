package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.dto.LancamentoPresencaRequest;
import br.com.sge.modules.academico.dto.SalvarMatrizPresencaRequest;
import br.com.sge.modules.academico.service.AcademicoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presencas")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class PresencaController {

    private final AcademicoService academicoService;

    public PresencaController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @Operation(summary = "Lancamento de presencas em lote")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/lancamento")
    public ResponseEntity<ApiResponse<Object>> lancarPresencas(
            @Valid @RequestBody LancamentoPresencaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Presencas registradas", academicoService.lancarPresencas(request)));
    }

    @Operation(summary = "Presencas ja lancadas em uma aula")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listarPresencasDaAula(
            @RequestParam UUID turmaDisciplinaProfessorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAula) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Presencas encontradas",
                academicoService.listarPresencasDaAula(turmaDisciplinaProfessorId, dataAula)));
    }

    @Operation(summary = "Matriz de frequencia por bimestre (folha de diario)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @GetMapping("/matriz")
    public ResponseEntity<ApiResponse<Object>> matrizPresencas(
            @RequestParam UUID turmaDisciplinaProfessorId, @RequestParam UUID periodoId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Matriz de frequencia",
                academicoService.obterMatrizPresencas(turmaDisciplinaProfessorId, periodoId)));
    }

    @Operation(summary = "Salvar celulas da matriz de frequencia")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#request.turmaDisciplinaProfessorId())")
    @PostMapping("/matriz")
    public ResponseEntity<ApiResponse<Object>> salvarMatriz(
            @Valid @RequestBody SalvarMatrizPresencaRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Matriz salva", academicoService.salvarMatrizPresencas(request)));
    }

    @Operation(summary = "Assinar folha de frequencia do periodo")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @PostMapping("/matriz/assinar")
    public ResponseEntity<ApiResponse<Object>> assinarMatriz(
            @RequestParam UUID turmaDisciplinaProfessorId, @RequestParam UUID periodoId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Frequencia assinada",
                academicoService.assinarMatrizPresencas(turmaDisciplinaProfessorId, periodoId)));
    }
}
