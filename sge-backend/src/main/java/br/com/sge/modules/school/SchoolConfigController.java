package br.com.sge.modules.school;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school")
@Tag(name = "school", description = "Pacote e configuracao da escola")
public class SchoolConfigController {

  private final SchoolConfigService schoolConfigService;
  private final SchoolNormativaService normativaService;

  public SchoolConfigController(
      SchoolConfigService schoolConfigService, SchoolNormativaService normativaService) {
    this.schoolConfigService = schoolConfigService;
    this.normativaService = normativaService;
  }

  @Operation(summary = "Configuracao publica da escola (branding e normativas)")
  @GetMapping("/config")
  public ResponseEntity<ApiResponse<Object>> configuracao() {
    return ResponseEntity.ok(
        ApiResponse.ok("Configuracao da escola", schoolConfigService.obterConfiguracao()));
  }

  @Operation(summary = "Normativa vigente do pacote da escola")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping("/normativa")
  public ResponseEntity<ApiResponse<Object>> normativa() {
    return ResponseEntity.ok(ApiResponse.ok("Normativa da escola", normativaService.obterNormativa()));
  }

  @Operation(summary = "Preview das alteracoes ao aplicar normativa do pacote")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @GetMapping("/normativa/preview-aplicar")
  public ResponseEntity<ApiResponse<Object>> previewAplicar() {
    return ResponseEntity.ok(
        ApiResponse.ok("Preview da normativa", normativaService.previewAplicar()));
  }

  @Operation(summary = "Aplicar normativa do pacote (escola, financeiro, matrizes)")
  @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
  @PostMapping("/normativa/aplicar")
  public ResponseEntity<ApiResponse<Object>> aplicarNormativa() {
    return ResponseEntity.ok(
        ApiResponse.ok("Normativa aplicada", normativaService.aplicarNormativa()));
  }
}
