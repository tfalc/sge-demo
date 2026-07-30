package br.com.sge.modules.convivencia.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.convivencia.dto.RegistrarOcorrenciaRequest;
import br.com.sge.modules.convivencia.service.OcorrenciaService;
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
@RequestMapping("/api/ocorrencias")
@Tag(name = "convivencia", description = "Ocorrencias disciplinares")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    public OcorrenciaController(OcorrenciaService ocorrenciaService) {
        this.ocorrenciaService = ocorrenciaService;
    }

    @Operation(summary = "Registrar ocorrencia disciplinar")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#request.turmaDisciplinaProfessorId())")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> registrar(@Valid @RequestBody RegistrarOcorrenciaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ocorrencia registrada", ocorrenciaService.registrar(request)));
    }

    @Operation(summary = "Listar ocorrencias por vinculo turma/disciplina")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listarPorVinculo(
            @RequestParam UUID turmaDisciplinaProfessorId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Ocorrencias",
                ocorrenciaService.listarPorVinculo(turmaDisciplinaProfessorId)));
    }

    @Operation(summary = "Listar ocorrencias por turma (coordenacao)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaHorario(#turmaId)")
    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<ApiResponse<Object>> listarPorTurma(@PathVariable UUID turmaId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Ocorrencias da turma", ocorrenciaService.listarPorTurma(turmaId)));
    }

    @Operation(summary = "Marcar ocorrencia como vista (coordenacao)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @PutMapping("/{id}/vista")
    public ResponseEntity<ApiResponse<Object>> marcarVista(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Ocorrencia atualizada", ocorrenciaService.marcarVista(id)));
    }
}
