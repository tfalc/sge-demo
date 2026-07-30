package br.com.sge.modules.cadastro.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.cadastro.dto.AtualizarEscolaRequest;
import br.com.sge.modules.cadastro.dto.CriarAlunoRequest;
import br.com.sge.modules.cadastro.dto.CriarResponsavelRequest;
import br.com.sge.modules.cadastro.dto.VincularResponsavelRequest;
import br.com.sge.modules.cadastro.service.CadastroService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cadastro")
@Tag(name = "cadastro", description = "Cadastro de alunos e estrutura escolar")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @Operation(summary = "Dados da escola")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/escola")
    public ResponseEntity<ApiResponse<Object>> obterEscola() {
        return ResponseEntity.ok(ApiResponse.ok("Escola encontrada", cadastroService.obterEscola()));
    }

    @Operation(summary = "Atualizar escola")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PutMapping("/escola")
    public ResponseEntity<ApiResponse<Object>> atualizarEscola(@Valid @RequestBody AtualizarEscolaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Escola atualizada", cadastroService.atualizarEscola(request)));
    }

    @Operation(summary = "Listar alunos")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/alunos")
    public ResponseEntity<ApiResponse<Object>> listarAlunos() {
        return ResponseEntity.ok(ApiResponse.ok("Alunos encontrados", cadastroService.listarAlunosDetalhados()));
    }

    @Operation(summary = "Listar responsaveis")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/responsaveis")
    public ResponseEntity<ApiResponse<Object>> listarResponsaveis() {
        return ResponseEntity.ok(
                ApiResponse.ok("Responsaveis encontrados", cadastroService.listarResponsaveis()));
    }

    @Operation(summary = "Cadastrar filho do responsavel logado")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/meus-filhos")
    public ResponseEntity<ApiResponse<Object>> criarMeuFilho(@Valid @RequestBody CriarAlunoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Filho cadastrado", cadastroService.criarFilhoResponsavelLogado(request)));
    }

    @Operation(summary = "Cadastrar aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/alunos")
    public ResponseEntity<ApiResponse<Object>> criarAluno(@Valid @RequestBody CriarAlunoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Aluno cadastrado", cadastroService.criarAluno(request)));
    }

    @Operation(summary = "Cadastrar responsavel e vincular a aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/responsaveis")
    public ResponseEntity<ApiResponse<Object>> criarResponsavel(@Valid @RequestBody CriarResponsavelRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Responsavel cadastrado", cadastroService.criarResponsavel(request)));
    }

    @Operation(summary = "Vincular responsavel a aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/alunos/{alunoId}/responsaveis")
    public ResponseEntity<ApiResponse<Object>> vincularResponsavel(
            @PathVariable UUID alunoId, @Valid @RequestBody VincularResponsavelRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Vinculo criado", cadastroService.vincularResponsavelAluno(alunoId, request)));
    }

    @Operation(summary = "Remover vinculo responsavel-aluno")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @DeleteMapping("/alunos/{alunoId}/responsaveis/{responsavelId}")
    public ResponseEntity<ApiResponse<Object>> desvincularResponsavel(
            @PathVariable UUID alunoId, @PathVariable UUID responsavelId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Vinculo removido", cadastroService.desvincularResponsavelAluno(alunoId, responsavelId)));
    }
}
