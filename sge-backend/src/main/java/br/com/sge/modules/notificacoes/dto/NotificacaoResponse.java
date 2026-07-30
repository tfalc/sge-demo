package br.com.sge.modules.notificacoes.dto;

import java.util.UUID;

public record NotificacaoResponse(
        UUID id,
        String tipo,
        String titulo,
        String mensagem,
        String link,
        UUID referenciaId,
        boolean lida,
        String criadoEm) {}
