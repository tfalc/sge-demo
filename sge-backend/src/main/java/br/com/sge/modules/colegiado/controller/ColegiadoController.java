package br.com.sge.modules.colegiado.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.colegiado.dto.AtualizarEncaminhamentoRequest;
import br.com.sge.modules.colegiado.dto.AtualizarReuniaoColegiadoRequest;
import br.com.sge.modules.colegiado.dto.CriarEncaminhamentoRequest;
import br.com.sge.modules.colegiado.dto.CriarReuniaoColegiadoRequest;
import br.com.sge.modules.colegiado.service.ColegiadoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
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
@RequestMapping("/api/colegiados")
@Tag(name = "colegiado", description = "Reunioes de colegiado pedagogico")
public class ColegiadoController {

    private final ColegiadoService colegiadoService;

    public ColegiadoController(ColegiadoService colegiadoService) {
        this.colegiadoService = colegiadoService;
    }

    @Operation(summary = "Listar reunioes de colegiado")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @GetMapping("/reunioes")
    public ResponseEntity<ApiResponse<Object>> listarReunioes(@RequestParam(required = false) UUID turmaId) {
        return ResponseEntity.ok(ApiResponse.ok("Reunioes", colegiadoService.listarReunioes(turmaId)));
    }

    @Operation(summary = "Detalhe da reuniao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @GetMapping("/reunioes/{id}")
    public ResponseEntity<ApiResponse<Object>> obterReuniao(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Reuniao", colegiadoService.obterReuniao(id)));
    }

    @Operation(summary = "Painel de dados para pauta (risco, ocorrencias)")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @GetMapping("/reunioes/{id}/painel-dados")
    public ResponseEntity<ApiResponse<Object>> painelDados(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Painel", colegiadoService.painelDados(id)));
    }

    @Operation(summary = "Agendar reuniao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @PostMapping("/reunioes")
    public ResponseEntity<ApiResponse<Object>> criar(@Valid @RequestBody CriarReuniaoColegiadoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Reuniao criada", colegiadoService.criarReuniao(request)));
    }

    @Operation(summary = "Atualizar reuniao")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @PutMapping("/reunioes/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @RequestBody AtualizarReuniaoColegiadoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Reuniao atualizada", colegiadoService.atualizarReuniao(id, request)));
    }

    @Operation(summary = "Concluir reuniao e gerar ata")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @PostMapping("/reunioes/{id}/concluir")
    public ResponseEntity<ApiResponse<Object>> concluir(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String ata = body != null ? body.get("ataTexto") : null;
        return ResponseEntity.ok(ApiResponse.ok("Reuniao concluida", colegiadoService.concluirReuniao(id, ata)));
    }

    @Operation(summary = "Criar encaminhamento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @PostMapping("/reunioes/{id}/encaminhamentos")
    public ResponseEntity<ApiResponse<Object>> criarEncaminhamento(
            @PathVariable UUID id, @Valid @RequestBody CriarEncaminhamentoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Encaminhamento criado", colegiadoService.criarEncaminhamento(id, request)));
    }

    @Operation(summary = "Atualizar status do encaminhamento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR', 'PROFESSOR')")
    @PutMapping("/encaminhamentos/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizarEncaminhamento(
            @PathVariable UUID id, @Valid @RequestBody AtualizarEncaminhamentoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Encaminhamento atualizado", colegiadoService.atualizarEncaminhamento(id, request)));
    }

    @Operation(summary = "Listar encaminhamentos pendentes")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'COORDENADOR', 'DIRETOR')")
    @GetMapping("/encaminhamentos/pendentes")
    public ResponseEntity<ApiResponse<Object>> pendentes(@RequestParam(required = false) UUID turmaId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Pendentes", colegiadoService.listarEncaminhamentosPendentes(turmaId)));
    }
}
