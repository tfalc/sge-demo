package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.dto.AtualizarDisciplinaRequest;
import br.com.sge.modules.academico.dto.AtualizarTurmaRequest;
import br.com.sge.modules.academico.dto.CriarDisciplinaRequest;
import br.com.sge.modules.academico.dto.CriarProfessorRequest;
import br.com.sge.modules.academico.dto.CriarTurmaRequest;
import br.com.sge.modules.academico.dto.VincularDisciplinaTurmaRequest;
import br.com.sge.modules.academico.service.AcademicoEstruturaService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academico")
@Tag(name = "academico-estrutura", description = "Cadastro de turmas, disciplinas e professores")
public class AcademicoEstruturaController {

    private final AcademicoEstruturaService estruturaService;

    public AcademicoEstruturaController(AcademicoEstruturaService estruturaService) {
        this.estruturaService = estruturaService;
    }

    @Operation(summary = "Listar disciplinas")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/disciplinas")
    public ResponseEntity<ApiResponse<Object>> listarDisciplinas() {
        return ResponseEntity.ok(ApiResponse.ok("Disciplinas encontradas", estruturaService.listarDisciplinas()));
    }

    @Operation(summary = "Cadastrar disciplina")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/disciplinas")
    public ResponseEntity<ApiResponse<Object>> criarDisciplina(@Valid @RequestBody CriarDisciplinaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Disciplina cadastrada", estruturaService.criarDisciplina(request)));
    }

    @PutMapping("/disciplinas/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizarDisciplina(
            @PathVariable UUID id, @Valid @RequestBody AtualizarDisciplinaRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Disciplina atualizada", estruturaService.atualizarDisciplina(id, request)));
    }

    @DeleteMapping("/disciplinas/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirDisciplina(@PathVariable UUID id) {
        estruturaService.excluirDisciplina(id);
        return ResponseEntity.ok(ApiResponse.ok("Disciplina excluida", null));
    }

    @Operation(summary = "Listar professores")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/professores")
    public ResponseEntity<ApiResponse<Object>> listarProfessores() {
        return ResponseEntity.ok(ApiResponse.ok("Professores encontrados", estruturaService.listarProfessores()));
    }

    @Operation(summary = "Cadastrar professor com usuario de acesso")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/professores")
    public ResponseEntity<ApiResponse<Object>> criarProfessor(@Valid @RequestBody CriarProfessorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Professor cadastrado", estruturaService.criarProfessor(request)));
    }

    @Operation(summary = "Listar series")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/series")
    public ResponseEntity<ApiResponse<Object>> listarSeries() {
        return ResponseEntity.ok(ApiResponse.ok("Series encontradas", estruturaService.listarSeries()));
    }

    @Operation(summary = "Cadastrar turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/turmas")
    public ResponseEntity<ApiResponse<Object>> criarTurma(@Valid @RequestBody CriarTurmaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Turma cadastrada", estruturaService.criarTurma(request)));
    }

    @Operation(summary = "Vincular disciplina e professor a turma")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/turmas/{turmaId}/vinculos")
    public ResponseEntity<ApiResponse<Object>> vincularDisciplina(
            @PathVariable UUID turmaId, @Valid @RequestBody VincularDisciplinaTurmaRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Vinculo criado", estruturaService.vincularDisciplina(turmaId, request)));
    }

    @PutMapping("/turmas/{id}")
    public ResponseEntity<ApiResponse<Object>> atualizarTurma(
            @PathVariable UUID id, @Valid @RequestBody AtualizarTurmaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Turma atualizada", estruturaService.atualizarTurma(id, request)));
    }

    @DeleteMapping("/turmas/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirTurma(@PathVariable UUID id) {
        estruturaService.excluirTurma(id);
        return ResponseEntity.ok(ApiResponse.ok("Turma excluida", null));
    }

    @DeleteMapping("/vinculos/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirVinculo(@PathVariable UUID id) {
        estruturaService.excluirVinculo(id);
        return ResponseEntity.ok(ApiResponse.ok("Vinculo excluido", null));
    }
}
