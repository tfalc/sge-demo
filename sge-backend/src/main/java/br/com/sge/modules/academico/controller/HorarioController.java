package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.dto.AtualizarHorarioRequest;
import br.com.sge.modules.academico.dto.CriarHorarioRequest;
import br.com.sge.modules.academico.service.HorarioService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios")
@Tag(name = "horarios", description = "Grade de horarios por turma e professor")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @Operation(summary = "Horarios da turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaHorario(#turmaId)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarPorTurma(
            @RequestParam UUID turmaId) {
        return ResponseEntity.ok(ApiResponse.ok("Horarios da turma", horarioService.listarPorTurma(turmaId)));
    }

    @Operation(summary = "Horarios do professor")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessProfessorHorario(#professorId)")
    @GetMapping("/professor")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarPorProfessor(
            @RequestParam UUID professorId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Horarios do professor", horarioService.listarPorProfessor(professorId)));
    }

    @Operation(summary = "Criar horario")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> criar(@Valid @RequestBody CriarHorarioRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Horario criado", horarioService.criar(req)));
    }

    @Operation(summary = "Atualizar horario")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarHorarioRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Horario atualizado", horarioService.atualizar(id, req)));
    }

    @Operation(summary = "Excluir horario")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        horarioService.excluir(id);
        return ResponseEntity.ok(ApiResponse.ok("Horario excluido", null));
    }
}
