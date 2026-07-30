package br.com.sge.modules.matriculanova.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.matriculanova.dto.AtualizarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.dto.CriarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.dto.RejeitarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.entity.TipoDocumentoMatricula;
import br.com.sge.modules.matriculanova.service.MatriculaNovaService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/matricula-nova")
@Tag(name = "matricula-nova", description = "Matricula nova e gestao documental")
public class MatriculaNovaController {

    private final MatriculaNovaService matriculaNovaService;

    public MatriculaNovaController(MatriculaNovaService matriculaNovaService) {
        this.matriculaNovaService = matriculaNovaService;
    }

    @Operation(summary = "Anos letivos disponiveis")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/anos-letivos")
    public ResponseEntity<ApiResponse<Object>> listarAnosLetivos() {
        return ResponseEntity.ok(
                ApiResponse.ok("Anos letivos", matriculaNovaService.listarAnosLetivos()));
    }

    @Operation(summary = "Listar processos de matricula")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/processos")
    public ResponseEntity<ApiResponse<Object>> listar(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                ApiResponse.ok("Processos de matricula", matriculaNovaService.listarProcessos(status)));
    }

    @Operation(summary = "Detalhe do processo")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/processos/{id}")
    public ResponseEntity<ApiResponse<Object>> obter(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Processo", matriculaNovaService.obterProcesso(id)));
    }

    @Operation(summary = "Criar processo (rascunho)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PostMapping("/processos")
    public ResponseEntity<ApiResponse<Object>> criar(@Valid @RequestBody CriarMatriculaProcessoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Processo criado", matriculaNovaService.criarProcesso(request)));
    }

    @Operation(summary = "Atualizar processo")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/processos/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarMatriculaProcessoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Processo atualizado", matriculaNovaService.atualizarProcesso(id, request)));
    }

    @Operation(summary = "Enviar para analise")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/processos/{id}/enviar")
    public ResponseEntity<ApiResponse<Object>> enviar(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Processo enviado", matriculaNovaService.enviar(id)));
    }

    @Operation(summary = "Aprovar matricula")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/processos/{id}/aprovar")
    public ResponseEntity<ApiResponse<Object>> aprovar(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Processo aprovado", matriculaNovaService.aprovar(id)));
    }

    @Operation(summary = "Rejeitar matricula")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PutMapping("/processos/{id}/rejeitar")
    public ResponseEntity<ApiResponse<Object>> rejeitar(
            @PathVariable UUID id, @Valid @RequestBody RejeitarMatriculaProcessoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Processo rejeitado", matriculaNovaService.rejeitar(id, request)));
    }

    @Operation(summary = "Concluir — cria aluno no cadastro")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PostMapping("/processos/{id}/concluir")
    public ResponseEntity<ApiResponse<Object>> concluir(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Matricula concluida", matriculaNovaService.concluir(id)));
    }

    @Operation(summary = "Upload de documento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @PostMapping(value = "/processos/{id}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> uploadDocumento(
            @PathVariable UUID id,
            @RequestParam TipoDocumentoMatricula tipo,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.ok("Documento enviado", matriculaNovaService.uploadDocumento(id, tipo, file)));
    }

    @Operation(summary = "Download de documento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @GetMapping("/processos/{processoId}/documentos/{documentoId}")
    public ResponseEntity<byte[]> downloadDocumento(
            @PathVariable UUID processoId, @PathVariable UUID documentoId) {
        MatriculaNovaService.DocumentoDownload dl =
                matriculaNovaService.downloadDocumento(processoId, documentoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dl.filename() + "\"")
                .contentType(MediaType.parseMediaType(dl.contentType()))
                .body(dl.bytes());
    }

    @Operation(summary = "Excluir documento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'DIRETOR')")
    @DeleteMapping("/processos/{processoId}/documentos/{documentoId}")
    public ResponseEntity<ApiResponse<Object>> excluirDocumento(
            @PathVariable UUID processoId, @PathVariable UUID documentoId) {
        matriculaNovaService.excluirDocumento(processoId, documentoId);
        return ResponseEntity.ok(ApiResponse.ok("Documento removido", null));
    }
}
