package br.com.sge.modules.financeiro.service;

import br.com.sge.modules.financeiro.entity.StatusCobranca;
import org.springframework.stereotype.Component;

/**
 * Regras de negócio para transição de status de {@link br.com.sge.modules.financeiro.entity.Cobranca}.
 */
@Component
public class CobrancaStatusRules {

    /**
     * Valida se a cobrança pode ser marcada como PAGO (ex.: confirmação PIX).
     * Permitido: PENDENTE → PAGO, VENCIDO → PAGO.
     * Não permitido: já PAGO, CANCELADO, ou qualquer outro estado.
     */
    public void assertPodeMarcarComoPago(StatusCobranca statusAtual) {
        if (statusAtual == StatusCobranca.PAGO) {
            return;
        }
        if (statusAtual == StatusCobranca.CANCELADO) {
            throw new IllegalArgumentException("Cobranca cancelada nao pode ser marcada como paga");
        }
        if (statusAtual != StatusCobranca.PENDENTE && statusAtual != StatusCobranca.VENCIDO) {
            throw new IllegalArgumentException(
                    "Transicao invalida: apenas cobrancas PENDENTE ou VENCIDO podem ser pagas (atual: "
                            + statusAtual + ")");
        }
    }
}
