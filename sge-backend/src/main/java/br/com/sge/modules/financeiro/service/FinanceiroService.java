package br.com.sge.modules.financeiro.service;

import br.com.sge.modules.financeiro.dto.CriarCobrancaRequest;
import br.com.sge.modules.financeiro.entity.PlanoPagamento;
import br.com.sge.modules.financeiro.dto.PixWebhookPayload;
import br.com.sge.modules.financeiro.entity.Cobranca;
import br.com.sge.modules.financeiro.entity.Contrato;
import br.com.sge.modules.financeiro.entity.StatusCobranca;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.AnoLetivo;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.AnoLetivoRepository;
import br.com.sge.modules.financeiro.dto.CriarContratoRequest;
import br.com.sge.modules.financeiro.dto.CriarPlanoPagamentoRequest;
import br.com.sge.modules.financeiro.entity.PlanoPagamento;
import br.com.sge.modules.financeiro.repository.CobrancaRepository;
import br.com.sge.modules.financeiro.repository.ContratoRepository;
import br.com.sge.modules.financeiro.repository.PlanoPagamentoRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceiroService {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroService.class);

    private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

    private static final List<StatusCobranca> STATUS_NAO_INADIMPLENTE = List.of(StatusCobranca.PAGO, StatusCobranca.CANCELADO);

    private final CobrancaRepository cobrancaRepository;
    private final ContratoRepository contratoRepository;
    private final PlanoPagamentoRepository planoPagamentoRepository;
    private final AlunoRepository alunoRepository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final FinancialPixService financialPixService;
    private final CobrancaStatusRules cobrancaStatusRules;
    private final NotificacaoService notificacaoService;

    public FinanceiroService(
            CobrancaRepository cobrancaRepository,
            ContratoRepository contratoRepository,
            PlanoPagamentoRepository planoPagamentoRepository,
            AlunoRepository alunoRepository,
            AnoLetivoRepository anoLetivoRepository,
            FinancialPixService financialPixService,
            CobrancaStatusRules cobrancaStatusRules,
            NotificacaoService notificacaoService) {
        this.cobrancaRepository = cobrancaRepository;
        this.contratoRepository = contratoRepository;
        this.planoPagamentoRepository = planoPagamentoRepository;
        this.alunoRepository = alunoRepository;
        this.anoLetivoRepository = anoLetivoRepository;
        this.financialPixService = financialPixService;
        this.cobrancaStatusRules = cobrancaStatusRules;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Marca PENDENTE → VENCIDO quando {@code vencimento < hoje} (Brasil). Usado pelo job diário.
     *
     * @return quantidade de linhas atualizadas
     */
    @Transactional
    public int atualizarCobrancasVencidas() {
        LocalDate hoje = LocalDate.now(ZONA_BR);
        List<Cobranca> pendentes =
                cobrancaRepository.findPendentesComVencimentoUltrapassado(StatusCobranca.PENDENTE, hoje);
        for (Cobranca cobranca : pendentes) {
            UUID alunoId = cobranca.getContrato().getAluno().getId();
            String alunoNome = cobranca.getContrato().getAluno().getPessoa().getNome();
            notificacaoService.notificarResponsaveisDoAluno(
                    alunoId,
                    TipoNotificacao.COBRANCA_VENCIDA,
                    "Cobranca vencida",
                    "Mensalidade de " + alunoNome + " venceu em " + cobranca.getVencimento() + ".",
                    "/pais/cobrancas",
                    cobranca.getId());
        }
        return cobrancaRepository.updateStatusPendenteParaVencido(StatusCobranca.PENDENTE, StatusCobranca.VENCIDO, hoje);
    }

    /**
     * Persiste nova cobranca e gera dados PIX (simulacao provedor).
     */
    @Transactional
    public Map<String, Object> criarCobranca(CriarCobrancaRequest req) {
        Contrato contrato = contratoRepository
                .findById(req.contratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato nao encontrado"));

        Cobranca c = new Cobranca();
        c.setContrato(contrato);
        c.setCompetencia(req.competencia());
        c.setValor(req.valor());
        c.setVencimento(req.vencimento());
        c.setStatus(StatusCobranca.PENDENTE);

        Cobranca saved = cobrancaRepository.save(c);
        financialPixService.generatePixCharge(saved);
        saved = cobrancaRepository.save(saved);

        log.info("[Financeiro] Cobranca criada com PIX: id={} contratoId={} pixTxid={}", saved.getId(), req.contratoId(), saved.getPixTxid());

        UUID alunoId = contrato.getAluno().getId();
        String alunoNome = contrato.getAluno().getPessoa().getNome();
        notificacaoService.notificarResponsaveisDoAluno(
                alunoId,
                TipoNotificacao.COBRANCA_GERADA,
                "Nova cobranca disponivel",
                "Mensalidade de " + alunoNome + " — vencimento " + saved.getVencimento() + ".",
                "/pais/cobrancas",
                saved.getId());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("contratoId", req.contratoId());
        m.put("competencia", saved.getCompetencia().toString());
        m.put("valor", saved.getValor().doubleValue());
        m.put("vencimento", saved.getVencimento().toString());
        m.put("status", saved.getStatus().name());
        m.put("pixTxId", saved.getPixTxid());
        m.put("pixQrCode", saved.getPixQrcode());
        m.put("pixModo", financialPixService.getModo().name());
        return m;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterConfigPix() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("modo", financialPixService.getModo().name());
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarCobrancas(String responsavelId) {
        UUID rid;
        try {
            rid = UUID.fromString(responsavelId.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("responsavelId deve ser um UUID valido");
        }
        return cobrancaRepository.findByResponsavelId(rid).stream()
                .map(c -> toCobrancaMap(c, rid))
                .toList();
    }

    private Map<String, Object> toCobrancaMap(Cobranca c, UUID responsavelId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("responsavelId", responsavelId);
        m.put("alunoNome", c.getContrato().getAluno().getPessoa().getNome());
        m.put("competencia", c.getCompetencia().toString());
        m.put("valor", c.getValor().doubleValue());
        m.put("vencimento", c.getVencimento().toString());
        m.put("status", c.getStatus().name());
        m.put("pagoEm", c.getPagoEm() != null ? c.getPagoEm().toString() : null);
        return m;
    }

    /**
     * QR Code (Base64 do payload simulado) e status atual da cobranca.
     */
    @Transactional
    public Map<String, Object> obterQrCode(String cobrancaId) {
        UUID id;
        try {
            id = UUID.fromString(cobrancaId.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("cobrancaId deve ser um UUID valido");
        }
        Cobranca c = cobrancaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobranca nao encontrada"));

        garantirPixGeradoSeAberto(c);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cobrancaId", c.getId());
        out.put("qrCode", c.getPixQrcode() != null ? c.getPixQrcode() : "");
        out.put("pixCopyPaste", FinancialPixService.decodePixCopyPaste(c.getPixQrcode()));
        out.put("qrCodeImageUrl", c.getPixQrImageUrl());
        out.put("pixModo", financialPixService.getModo().name());
        out.put("valor", c.getValor().doubleValue());
        out.put("status", c.getStatus().name());
        return out;
    }

    /**
     * Ambiente local: simula confirmacao de pagamento PIX (equivalente ao webhook COMPLETED).
     */
    @Transactional
    public Map<String, Object> simularPagamentoPix(String cobrancaId) {
        if (!financialPixService.isSimulacao()) {
            throw new IllegalStateException(
                    "Simulacao de pagamento disponivel apenas com OPENPIX_APP_ID vazio (modo SIMULACAO)");
        }
        return processarWebhookPix(new PixWebhookPayload(cobrancaId, "COMPLETED"));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPlanos() {
        return planoPagamentoRepository.findAllByOrderByNomeAsc().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("nome", p.getNome());
                    m.put("valorMensalidade", p.getValorMensalidade().doubleValue());
                    m.put("diaVencimento", p.getDiaVencimento());
                    return m;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> criarPlano(CriarPlanoPagamentoRequest req) {
        PlanoPagamento p = new PlanoPagamento();
        p.setNome(req.nome().trim());
        p.setValorMensalidade(req.valorMensalidade());
        p.setDiaVencimento(req.diaVencimento());
        PlanoPagamento saved = planoPagamentoRepository.save(p);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("nome", saved.getNome());
        m.put("valorMensalidade", saved.getValorMensalidade().doubleValue());
        m.put("diaVencimento", saved.getDiaVencimento());
        return m;
    }

    @Transactional
    public Map<String, Object> criarContrato(CriarContratoRequest req) {
        if (contratoRepository.existsByAlunoIdAndStatus(req.alunoId(), "ATIVO")) {
            throw new IllegalArgumentException("Aluno ja possui contrato ativo");
        }
        Aluno aluno = alunoRepository
                .findDetalhadoById(req.alunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        PlanoPagamento plano = planoPagamentoRepository
                .findById(req.planoId())
                .orElseThrow(() -> new IllegalArgumentException("Plano nao encontrado"));
        AnoLetivo anoLetivo = anoLetivoRepository.findAllByOrderByAnoDesc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ano letivo nao configurado"));

        Contrato contrato = new Contrato();
        contrato.setAluno(aluno);
        contrato.setPlano(plano);
        contrato.setAnoLetivo(anoLetivo);
        contrato.setDataInicio(req.dataInicio() != null ? req.dataInicio() : LocalDate.now(ZONA_BR));
        contrato.setStatus("ATIVO");

        Contrato saved = contratoRepository.save(contrato);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("alunoId", aluno.getId());
        m.put("alunoNome", aluno.getPessoa().getNome());
        m.put("planoNome", plano.getNome());
        m.put("status", saved.getStatus());
        return m;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarContratosAtivos() {
        return contratoRepository.findAllAtivosComAluno().stream()
                .map(ct -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", ct.getId());
                    m.put("alunoId", ct.getAluno().getId());
                    m.put("alunoNome", ct.getAluno().getPessoa().getNome());
                    m.put("matricula", ct.getAluno().getMatricula());
                    m.put("planoNome", ct.getPlano().getNome());
                    m.put("valorMensalidade", ct.getPlano().getValorMensalidade().doubleValue());
                    m.put("diaVencimento", ct.getPlano().getDiaVencimento());
                    m.put("dataInicio", ct.getDataInicio());
                    return m;
                })
                .toList();
    }

    private void garantirPixGeradoSeAberto(Cobranca c) {
        if (c.getStatus() != StatusCobranca.PENDENTE && c.getStatus() != StatusCobranca.VENCIDO) {
            return;
        }
        if (c.getPixTxid() != null && c.getPixQrcode() != null) {
            return;
        }
        financialPixService.generatePixCharge(c);
        cobrancaRepository.save(c);
        log.info("[Financeiro] PIX retrogerado para cobranca id={}", c.getId());
    }

    /**
     * Webhook PIX: idempotente; aplica regras PENDENTE/VENCIDO → PAGO; PAGO não altera; CANCELADO ignorado.
     */
    @Transactional
    public Map<String, Object> processarWebhookPix(PixWebhookPayload payload) {
        if (payload == null || payload.correlationID() == null || payload.correlationID().isBlank()) {
            throw new IllegalArgumentException("correlationID e obrigatorio");
        }

        UUID cobrancaId;
        try {
            cobrancaId = UUID.fromString(payload.correlationID().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("correlationID deve ser UUID da cobranca");
        }

        String statusProvedor = payload.status() != null ? payload.status().trim() : "";
        log.info("[PIX webhook] Recebido correlationID={} status={}", cobrancaId, statusProvedor);

        if (!"COMPLETED".equalsIgnoreCase(statusProvedor)) {
            log.info("[PIX webhook] Ignorado: status nao COMPLETED cobrancaId={}", cobrancaId);
            return Map.of(
                    "acknowledged", true,
                    "ignored", true,
                    "reason", "STATUS_NOT_COMPLETED");
        }

        Cobranca c = cobrancaRepository
                .findByIdForUpdate(cobrancaId)
                .orElseThrow(() -> new IllegalArgumentException("Cobranca nao encontrada"));

        if (c.getStatus() == StatusCobranca.PAGO) {
            log.info("[PIX webhook] Idempotente: cobranca ja PAGO cobrancaId={} — sem alteracao", cobrancaId);
            return Map.of(
                    "acknowledged", true,
                    "idempotent", true,
                    "cobrancaStatus", StatusCobranca.PAGO.name());
        }

        if (c.getStatus() == StatusCobranca.CANCELADO) {
            log.warn("[PIX webhook] Ignorado: cobranca cancelada cobrancaId={}", cobrancaId);
            return Map.of(
                    "acknowledged", true,
                    "ignored", true,
                    "reason", "CANCELADO");
        }

        cobrancaStatusRules.assertPodeMarcarComoPago(c.getStatus());

        c.setStatus(StatusCobranca.PAGO);
        c.setPagoEm(Instant.now());
        cobrancaRepository.save(c);

        log.info("[PIX webhook] Cobranca atualizada para PAGO cobrancaId={} pagoEm={}", cobrancaId, c.getPagoEm());

        UUID alunoId = c.getContrato().getAluno().getId();
        String alunoNome = c.getContrato().getAluno().getPessoa().getNome();
        notificacaoService.notificarResponsaveisDoAluno(
                alunoId,
                TipoNotificacao.PAGAMENTO_CONFIRMADO,
                "Pagamento confirmado",
                "Pagamento da mensalidade de " + alunoNome + " foi confirmado.",
                "/pais/cobrancas",
                c.getId());

        return Map.of(
                "acknowledged", true,
                "idempotent", false,
                "cobrancaStatus", StatusCobranca.PAGO.name(),
                "pagoEm", c.getPagoEm().toString());
    }

    /**
     * Lista cobranças vencidas (vencimento &lt; hoje) ainda não pagas nem canceladas.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarInadimplentes() {
        LocalDate hoje = LocalDate.now(ZONA_BR);
        return cobrancaRepository.findCobrancasVencidasNaoPagas(hoje, STATUS_NAO_INADIMPLENTE).stream()
                .map(this::toCobrancaVencidaMap)
                .toList();
    }

    private Map<String, Object> toCobrancaVencidaMap(Cobranca c) {
        LocalDate hoje = LocalDate.now(ZONA_BR);
        int diasAtraso = (int) ChronoUnit.DAYS.between(c.getVencimento(), hoje);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("contratoId", c.getContrato().getId());
        m.put("alunoNome", c.getContrato().getAluno().getPessoa().getNome());
        m.put("competencia", c.getCompetencia().toString());
        m.put("valor", c.getValor().doubleValue());
        m.put("vencimento", c.getVencimento().toString());
        m.put("status", c.getStatus().name());
        m.put("diasAtraso", diasAtraso);
        return m;
    }

    /**
     * Totais do mês corrente (fuso São Paulo): recebido no mês, pendente no prazo, valor vencido/atrasado.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> relatorioMensal() {
        YearMonth ym = YearMonth.now(ZONA_BR);
        LocalDate hoje = LocalDate.now(ZONA_BR);
        Instant inicio = ym.atDay(1).atStartOfDay(ZONA_BR).toInstant();
        Instant fim = ym.plusMonths(1).atDay(1).atStartOfDay(ZONA_BR).toInstant();

        BigDecimal totalRecebido =
                cobrancaRepository.sumValorPagoEntre(StatusCobranca.PAGO, inicio, fim);
        if (totalRecebido == null) {
            totalRecebido = BigDecimal.ZERO;
        }

        BigDecimal totalPendente = cobrancaRepository.sumValorPendenteNoPrazo(StatusCobranca.PENDENTE, hoje);
        if (totalPendente == null) {
            totalPendente = BigDecimal.ZERO;
        }

        BigDecimal totalVencido = cobrancaRepository.sumValorVencidoOuAtrasado(hoje, STATUS_NAO_INADIMPLENTE);
        if (totalVencido == null) {
            totalVencido = BigDecimal.ZERO;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mes", ym.toString());
        out.put("totalRecebido", totalRecebido.doubleValue());
        out.put("totalPendente", totalPendente.doubleValue());
        out.put("totalVencido", totalVencido.doubleValue());
        return out;
    }

    /**
     * Gera cobrancas do mes para todos os contratos ativos que ainda nao possuem competencia.
     * Idempotente: contratos ja faturados no mes sao ignorados.
     */
    @Transactional
    public Map<String, Object> gerarCobrancasDoMes(YearMonth competenciaYm) {
        LocalDate competencia = competenciaYm.atDay(1);
        List<Contrato> contratos = contratoRepository.findAllAtivosComAluno();
        int criadas = 0;
        int ignoradas = 0;
        List<Map<String, Object>> itens = new ArrayList<>();

        for (Contrato contrato : contratos) {
            if (cobrancaRepository.existsByContratoIdAndCompetencia(contrato.getId(), competencia)) {
                ignoradas++;
                continue;
            }
            PlanoPagamento plano = contrato.getPlano();
            LocalDate vencimento = vencimentoParaCompetencia(competenciaYm, plano.getDiaVencimento());
            Map<String, Object> cobranca = criarCobranca(new CriarCobrancaRequest(
                    contrato.getId(), competencia, plano.getValorMensalidade(), vencimento));
            itens.add(cobranca);
            criadas++;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("competencia", competencia.toString());
        out.put("criadas", criadas);
        out.put("ignoradas", ignoradas);
        out.put("cobrancas", itens);
        log.info("[Financeiro] Cobrancas do mes {}: criadas={} ignoradas={}", competencia, criadas, ignoradas);
        return out;
    }

    @Transactional
    public Map<String, Object> gerarCobrancasMesAtual() {
        return gerarCobrancasDoMes(YearMonth.now(ZONA_BR));
    }

    private static LocalDate vencimentoParaCompetencia(YearMonth ym, int diaVencimento) {
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(Math.max(diaVencimento, 1), lastDay);
        return ym.atDay(safeDay);
    }
}
