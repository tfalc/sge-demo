package br.com.sge.modules.relatorios.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.relatorios.service.BoletimPdfService;
import br.com.sge.modules.relatorios.service.RelatorioService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "relatorios", description = "Relatorios, dashboard e boletim PDF")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final BoletimPdfService boletimPdfService;

    public RelatorioController(RelatorioService relatorioService, BoletimPdfService boletimPdfService) {
        this.relatorioService = relatorioService;
        this.boletimPdfService = boletimPdfService;
    }

    @Operation(summary = "Desempenho por turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/turma/{id}/desempenho")
    public ResponseEntity<ApiResponse<Object>> desempenhoTurma(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Desempenho da turma", relatorioService.desempenhoTurma(id)));
    }

    @Operation(summary = "Frequencia por turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/turma/{id}/frequencia")
    public ResponseEntity<ApiResponse<Object>> frequenciaTurma(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Frequencia da turma", relatorioService.frequenciaTurma(id)));
    }

    @Operation(summary = "Analise inteligente do aluno", description = "Motor pedagogico embutido (regras + heuristica), sem API externa.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping({"/aluno/{id}/analise-inteligente", "/aluno/{id}/analise-ia"})
    public ResponseEntity<ApiResponse<Object>> analiseAluno(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Analise inteligente gerada", relatorioService.analiseAluno(id)));
    }

    @Operation(summary = "Inadimplencia da escola")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/escola/inadimplencia")
    public ResponseEntity<ApiResponse<Object>> inadimplenciaEscola() {
        return ResponseEntity.ok(
                ApiResponse.ok("Inadimplencia da escola", relatorioService.inadimplenciaEscola()));
    }

    @Operation(summary = "Gerar boletim em PDF")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#alunoId)")
    @PostMapping("/boletim/{alunoId}/gerar-pdf")
    public ResponseEntity<byte[]> gerarBoletimPdf(@PathVariable UUID alunoId) {
        byte[] pdf = boletimPdfService.gerarPdf(alunoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=boletim-" + alunoId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
