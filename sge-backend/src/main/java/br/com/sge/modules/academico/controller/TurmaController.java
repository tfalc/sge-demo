package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.service.AcademicoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class TurmaController {

    private final AcademicoService academicoService;

    public TurmaController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @Operation(summary = "Listar turmas", description = "Opcionalmente filtra turmas do professor.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listarTurmas(@RequestParam(required = false) UUID professorId) {
        return ResponseEntity.ok(ApiResponse.ok("Turmas encontradas", academicoService.listarTurmas(professorId)));
    }

    @Operation(summary = "Alunos da turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/{id}/alunos")
    public ResponseEntity<ApiResponse<Object>> listarAlunos(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Alunos encontrados", academicoService.listarAlunosDaTurma(id)));
    }

    @Operation(summary = "Disciplinas vinculadas a turma", description = "Filtra por professor quando informado.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/{id}/disciplinas")
    public ResponseEntity<ApiResponse<Object>> listarDisciplinas(
            @PathVariable UUID id, @RequestParam(required = false) UUID professorId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Disciplinas encontradas", academicoService.listarVinculosDisciplina(id, professorId)));
    }
}
