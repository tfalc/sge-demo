package br.com.sge.modules.rematricula.dto;

import java.util.List;

public record RematriculaConfigResponse(
        String id,
        String titulo,
        boolean habilitada,
        Integer anoLetivo,
        String anoLetivoId,
        boolean possuiModeloPdf,
        String pdfModeloNome,
        FormularioRematriculaDto formulario,
        List<String> sugestoesExtracao,
        String publicadoEm,
        String atualizadoEm) {}
