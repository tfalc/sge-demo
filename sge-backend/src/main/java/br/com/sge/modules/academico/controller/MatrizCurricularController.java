package br.com.sge.modules.academico.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.academico.service.MatrizCurricularService;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academico/matrizes")
@Tag(name = "academico-matriz", description = "Matriz curricular (normativa RJ)")
public class MatrizCurricularController {

  private final MatrizCurricularService matrizService;

  public MatrizCurricularController(MatrizCurricularService matrizService) {
    this.matrizService = matrizService;
  }

  @Operation(summary = "Listar matrizes curriculares ativas")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping
  public ResponseEntity<ApiResponse<Object>> listar() {
    return ResponseEntity.ok(
        ApiResponse.ok("Matrizes curriculares", matrizService.listar()));
  }

  @Operation(summary = "Detalhe da matriz curricular")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Object>> obter(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.ok("Matriz curricular", matrizService.obter(id)));
  }

  @Operation(summary = "Matriz da serie")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping("/serie/{serieId}")
  public ResponseEntity<ApiResponse<Object>> porSerie(@PathVariable UUID serieId) {
    return ResponseEntity.ok(
        ApiResponse.ok("Matriz da serie", matrizService.obterPorSerie(serieId)));
  }

  @Operation(summary = "Validar turma contra matriz e grade horaria")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping("/validacao-turma/{turmaId}")
  public ResponseEntity<ApiResponse<Object>> validarTurma(
      @PathVariable UUID turmaId, @RequestParam(required = false) UUID matrizId) {
    return ResponseEntity.ok(
        ApiResponse.ok("Validacao da turma", matrizService.validarTurma(turmaId, matrizId)));
  }
}
