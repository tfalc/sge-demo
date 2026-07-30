package br.com.sge.modules.financeiro.schedule;

import br.com.sge.modules.financeiro.service.FinanceiroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Marca diariamente cobranças PENDENTES com vencimento ultrapassado como VENCIDO.
 */
@Component
public class CobrancaVencimentoScheduler {

    private static final Logger log = LoggerFactory.getLogger(CobrancaVencimentoScheduler.class);

    private final FinanceiroService financeiroService;

    public CobrancaVencimentoScheduler(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    /** Todo dia às 06:00 (America/Sao_Paulo). */
    @Scheduled(cron = "0 0 6 * * *", zone = "America/Sao_Paulo")
    public void marcarCobrancasVencidas() {
        int atualizadas = financeiroService.atualizarCobrancasVencidas();
        if (atualizadas > 0) {
            log.info("[Financeiro] Job vencimento: {} cobranca(s) PENDENTE -> VENCIDO", atualizadas);
        }
    }
}
