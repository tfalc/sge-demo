package br.com.sge.modules.rematricula.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.rematricula.dto.AtualizarRematriculaConfigRequest;
import br.com.sge.modules.rematricula.dto.SalvarRespostasRequest;
import br.com.sge.modules.rematricula.service.RematriculaService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rematricula")
@Tag(name = "rematricula", description = "Rematricula online — config, portal dos pais e validacao secretaria")
public class RematriculaController {

    private final RematriculaService rematriculaService;

    public RematriculaController(RematriculaService rematriculaService) {
        this.rematriculaService = rematriculaService;
    }

    @Operation(summary = "Obter configuracao da rematricula (secretaria/admin)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Object>> obterConfig() {
        return ResponseEntity.ok(ApiResponse.ok("Configuracao carregada", rematriculaService.obterConfig()));
    }

    @Operation(summary = "Atualizar configuracao e formulario")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Object>> atualizarConfig(
            @Valid @RequestBody AtualizarRematriculaConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuracao atualizada", rematriculaService.atualizarConfig(request)));
    }

    @Operation(summary = "Enviar PDF modelo (processado no servidor)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PostMapping(value = "/config/modelo-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> uploadModeloPdf(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("PDF modelo recebido", rematriculaService.uploadModeloPdf(file)));
    }

    @Operation(summary = "Baixar PDF modelo")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/config/modelo-pdf")
    public ResponseEntity<byte[]> downloadModeloPdf() {
        byte[] pdf = rematriculaService.downloadModeloPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + rematriculaService.nomeModeloPdf() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Portal da rematricula para responsavel")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('PAI', 'ADMIN', 'SECRETARIA')")
    @GetMapping("/portal")
    public ResponseEntity<ApiResponse<Object>> portal(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.ok("Portal carregado", rematriculaService.portalParaResponsavel(authentication.getName())));
    }

    @Operation(summary = "Salvar rascunho das respostas")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#alunoId)")
    @PutMapping("/alunos/{alunoId}/rascunho")
    public ResponseEntity<ApiResponse<Object>> salvarRascunho(
            @PathVariable UUID alunoId,
            @RequestBody SalvarRespostasRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Rascunho salvo",
                rematriculaService.salvarRascunho(alunoId, authentication.getName(), request)));
    }

    @Operation(summary = "Validar respostas e obter revisao antes de confirmar")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#alunoId)")
    @PostMapping("/alunos/{alunoId}/revisao")
    public ResponseEntity<ApiResponse<Object>> revisar(
            @PathVariable UUID alunoId,
            @RequestBody SalvarRespostasRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.ok("Revisao gerada", rematriculaService.revisar(alunoId, authentication.getName(), request)));
    }

    @Operation(summary = "Confirmar envio do formulario preenchido")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessAluno(#alunoId)")
    @PostMapping("/alunos/{alunoId}/confirmar")
    public ResponseEntity<ApiResponse<Object>> confirmar(@PathVariable UUID alunoId, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Formulario enviado para validacao da secretaria",
                rematriculaService.confirmarEnvio(alunoId, authentication.getName())));
    }

    @Operation(summary = "Listar submissoes aguardando validacao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/submissoes/pendentes")
    public ResponseEntity<ApiResponse<Object>> listarPendentes() {
        return ResponseEntity.ok(
                ApiResponse.ok("Submissoes pendentes", rematriculaService.listarPendentesSecretaria()));
    }

    @Operation(summary = "Detalhe de uma submissao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'PAI')")
    @GetMapping("/submissoes/{id}")
    public ResponseEntity<ApiResponse<Object>> detalhe(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Submissao carregada", rematriculaService.detalheSubmissao(id)));
    }

    @Operation(summary = "Marcar submissao como validada pela secretaria")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/submissoes/{id}/validar")
    public ResponseEntity<ApiResponse<Object>> validarSecretaria(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Rematricula validada", rematriculaService.validarSecretaria(id)));
    }

    @Operation(summary = "Baixar PDF preenchido")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'PAI')")
    @GetMapping("/submissoes/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdfPreenchido(@PathVariable UUID id) {
        byte[] pdf = rematriculaService.downloadPdfPreenchido(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rematricula-preenchida.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
