package br.com.sge.modules.comunicacao.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.comunicacao.dto.AtualizarEventoAgendaRequest;
import br.com.sge.modules.comunicacao.dto.CriarEventoAgendaRequest;
import br.com.sge.modules.comunicacao.service.ComunicacaoService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/agenda")
@Tag(name = "comunicacao", description = "Comunicados, cardapio e agenda escolar")
public class AgendaController {

    private final ComunicacaoService comunicacaoService;

    public AgendaController(ComunicacaoService comunicacaoService) {
        this.comunicacaoService = comunicacaoService;
    }

    @Operation(summary = "Eventos no periodo")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(required = false) UUID turmaId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Eventos encontrados", comunicacaoService.listarAgenda(inicio, fim, turmaId)));
    }

    @Operation(summary = "Cadastrar evento na agenda")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> criar(@Valid @RequestBody CriarEventoAgendaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Evento cadastrado", comunicacaoService.criarEventoAgenda(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarEventoAgendaRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Evento atualizado", comunicacaoService.atualizarEventoAgenda(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable UUID id) {
        comunicacaoService.excluirEventoAgenda(id);
        return ResponseEntity.ok(ApiResponse.ok("Evento excluido", null));
    }
}
