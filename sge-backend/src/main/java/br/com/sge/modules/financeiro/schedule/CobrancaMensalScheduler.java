package br.com.sge.modules.financeiro.schedule;

import br.com.sge.modules.financeiro.service.FinanceiroService;
import java.time.YearMonth;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Gera cobrancas automaticamente no primeiro dia de cada mes. */
@Component
public class CobrancaMensalScheduler {

    private static final Logger log = LoggerFactory.getLogger(CobrancaMensalScheduler.class);
    private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

    private final FinanceiroService financeiroService;

    public CobrancaMensalScheduler(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @Scheduled(cron = "0 0 7 1 * *", zone = "America/Sao_Paulo")
    public void gerarCobrancasMensais() {
        var resultado = financeiroService.gerarCobrancasDoMes(YearMonth.now(ZONA_BR));
        log.info(
                "[Financeiro] Job mensal: competencia={} criadas={} ignoradas={}",
                resultado.get("competencia"),
                resultado.get("criadas"),
                resultado.get("ignoradas"));
    }
}
