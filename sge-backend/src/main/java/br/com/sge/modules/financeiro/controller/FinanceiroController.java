package br.com.sge.modules.financeiro.controller;

import br.com.sge.config.OpenApiConfig;
import br.com.sge.modules.financeiro.dto.CriarCobrancaRequest;
import br.com.sge.modules.financeiro.dto.CriarContratoRequest;
import br.com.sge.modules.financeiro.dto.CriarPlanoPagamentoRequest;
import br.com.sge.modules.financeiro.dto.PixWebhookPayload;
import br.com.sge.modules.financeiro.openpix.OpenPixWebhookParser;
import br.com.sge.modules.financeiro.openpix.OpenPixWebhookValidator;
import br.com.sge.modules.financeiro.service.FinanceiroService;
import br.com.sge.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financeiro")
@Tag(name = "financeiro", description = "Cobranças, PIX, inadimplência e relatórios")
public class FinanceiroController {

    private final FinanceiroService financeiroService;
    private final OpenPixWebhookParser openPixWebhookParser;
    private final OpenPixWebhookValidator openPixWebhookValidator;

    public FinanceiroController(
            FinanceiroService financeiroService,
            OpenPixWebhookParser openPixWebhookParser,
            OpenPixWebhookValidator openPixWebhookValidator) {
        this.financeiroService = financeiroService;
        this.openPixWebhookParser = openPixWebhookParser;
        this.openPixWebhookValidator = openPixWebhookValidator;
    }

    @Operation(summary = "Criar cobrança", description = "Registra cobrança e gera PIX (OpenPix ou simulação).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/cobrancas")
    public ResponseEntity<ApiResponse<Object>> criarCobranca(@Valid @RequestBody CriarCobrancaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cobranca criada", financeiroService.criarCobranca(request)));
    }

    @Operation(summary = "Listar cobranças do responsável")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canListCobrancasResponsavel(#responsavelId)")
    @GetMapping("/cobrancas")
    public ResponseEntity<ApiResponse<Object>> listarCobrancas(@RequestParam String responsavelId) {
        return ResponseEntity.ok(ApiResponse.ok("Cobrancas encontradas", financeiroService.listarCobrancas(responsavelId)));
    }

    @Operation(summary = "QR Code PIX da cobrança", description = "Retorna copia-e-cola, imagem (OpenPix) e status.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessCobranca(#id)")
    @GetMapping("/cobrancas/{id}/qrcode")
    public ResponseEntity<ApiResponse<Object>> obterQrCode(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("QR Code", financeiroService.obterQrCode(id)));
    }

    @Operation(summary = "Modo PIX", description = "SIMULACAO (sem App ID) ou OPENPIX (credenciais configuradas).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/pix/config")
    public ResponseEntity<ApiResponse<Object>> configPix() {
        return ResponseEntity.ok(ApiResponse.ok("Config PIX", financeiroService.obterConfigPix()));
    }

    @Operation(
            summary = "Webhook PIX",
            description = "Callback OpenPix (evento charge) ou payload simplificado de simulação. Público.")
    @PostMapping("/webhook/pix")
    public ResponseEntity<ApiResponse<Object>> webhookPix(
            @RequestBody JsonNode body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        openPixWebhookValidator.validarAuthorization(authorization);
        PixWebhookPayload payload = openPixWebhookParser.parse(body);
        return ResponseEntity.ok(ApiResponse.ok("Webhook PIX processado", financeiroService.processarWebhookPix(payload)));
    }

    @Operation(
            summary = "Cobranças vencidas (inadimplentes)",
            description = "Lista cobranças com vencimento anterior a hoje, ainda não pagas nem canceladas.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/inadimplentes")
    public ResponseEntity<ApiResponse<Object>> inadimplentes() {
        return ResponseEntity.ok(ApiResponse.ok("Inadimplentes encontrados", financeiroService.listarInadimplentes()));
    }

    @Operation(
            summary = "Relatório financeiro mensal",
            description = "totalRecebido no mês (pagamentos), totalPendente no prazo, totalVencido/atrasado.")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/relatorio-mensal")
    public ResponseEntity<ApiResponse<Object>> relatorioMensal() {
        return ResponseEntity.ok(ApiResponse.ok("Relatorio mensal", financeiroService.relatorioMensal()));
    }

    @Operation(summary = "Contratos ativos", description = "Lista contratos para geracao de cobrancas (ambiente local).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/contratos")
    public ResponseEntity<ApiResponse<Object>> listarContratos() {
        return ResponseEntity.ok(ApiResponse.ok("Contratos ativos", financeiroService.listarContratosAtivos()));
    }

    @Operation(summary = "Listar planos de pagamento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @GetMapping("/planos")
    public ResponseEntity<ApiResponse<Object>> listarPlanos() {
        return ResponseEntity.ok(ApiResponse.ok("Planos encontrados", financeiroService.listarPlanos()));
    }

    @Operation(summary = "Cadastrar plano de pagamento")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/planos")
    public ResponseEntity<ApiResponse<Object>> criarPlano(@Valid @RequestBody CriarPlanoPagamentoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Plano cadastrado", financeiroService.criarPlano(request)));
    }

    @Operation(summary = "Criar contrato de mensalidade")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/contratos")
    public ResponseEntity<ApiResponse<Object>> criarContrato(@Valid @RequestBody CriarContratoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Contrato criado", financeiroService.criarContrato(request)));
    }

    @Operation(
            summary = "Simular pagamento PIX",
            description = "Apenas modo SIMULACAO: marca cobranca como PAGO (equivalente ao webhook).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PreAuthorize("@sgeAuth.canAccessCobranca(#id)")
    @PostMapping("/cobrancas/{id}/simular-pagamento")
    public ResponseEntity<ApiResponse<Object>> simularPagamento(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Pagamento simulado", financeiroService.simularPagamentoPix(id)));
    }

    @Operation(
            summary = "Gerar cobrancas do mes",
            description = "Cria cobrancas para contratos ativos sem competencia do mes corrente (idempotente).")
    @SecurityRequirement(name = OpenApiConfig.SCHEME_BEARER_JWT)
    @PostMapping("/gerar-cobrancas-mes")
    public ResponseEntity<ApiResponse<Object>> gerarCobrancasMes() {
        return ResponseEntity.ok(
                ApiResponse.ok("Cobrancas do mes geradas", financeiroService.gerarCobrancasMesAtual()));
    }
}
