package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.dto.SalvarAtaAulaRequest;
import br.com.sge.modules.academico.service.AtaAulaService;
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
@RequestMapping("/api/atas")
@Tag(name = "academico", description = "Turmas, notas, frequencia e boletim")
public class AtaAulaController {

    private final AtaAulaService ataAulaService;

    public AtaAulaController(AtaAulaService ataAulaService) {
        this.ataAulaService = ataAulaService;
    }

    @Operation(summary = "Obter ata de aula por vinculo e data")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> obterAta(
            @RequestParam UUID turmaDisciplinaProfessorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAula) {
        return ResponseEntity.ok(
                ApiResponse.ok("Ata de aula", ataAulaService.obterAta(turmaDisciplinaProfessorId, dataAula)));
    }

    @Operation(summary = "Historico de atas por turma/disciplina")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#turmaDisciplinaProfessorId)")
    @GetMapping("/historico")
    public ResponseEntity<ApiResponse<Object>> historicoAtas(
            @RequestParam UUID turmaDisciplinaProfessorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        LocalDate fimEfetivo = fim != null ? fim : LocalDate.now();
        LocalDate inicioEfetivo = inicio != null ? inicio : fimEfetivo.minusDays(60);
        return ResponseEntity.ok(ApiResponse.ok(
                "Historico de atas",
                ataAulaService.listarHistorico(turmaDisciplinaProfessorId, inicioEfetivo, fimEfetivo)));
    }

    @Operation(summary = "Salvar ou atualizar ata de aula")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessTurmaDisciplinaProfessor(#request.turmaDisciplinaProfessorId())")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> salvarAta(@Valid @RequestBody SalvarAtaAulaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ata registrada", ataAulaService.salvarAta(request)));
    }
}
