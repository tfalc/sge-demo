package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.dto.AtualizarNotaRequest;
import br.com.sge.modules.academico.dto.LancarNotaRequest;
import br.com.sge.modules.academico.service.AcademicoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notas")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class NotaController {

    private final AcademicoService academicoService;

    public NotaController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @Operation(summary = "Diario de notas (fichario) da turma/disciplina")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @GetMapping("/diario")
    public ResponseEntity<ApiResponse<Object>> diarioNotas(
            @RequestParam UUID turmaDisciplinaProfessorId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Diario de notas", academicoService.listarDiarioNotas(turmaDisciplinaProfessorId)));
    }

    @Operation(summary = "Lancar nota", description = "Cria ou substitui nota do mesmo tipo no periodo.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#request.turmaDisciplinaProfessorId())")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> lancarNota(@Valid @RequestBody LancarNotaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Nota lancada", academicoService.lancarNota(request)));
    }

    @Operation(summary = "Atualizar nota")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizarNota(
            @PathVariable UUID id, @Valid @RequestBody AtualizarNotaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Nota atualizada", academicoService.atualizarNota(id, request)));
    }
}
