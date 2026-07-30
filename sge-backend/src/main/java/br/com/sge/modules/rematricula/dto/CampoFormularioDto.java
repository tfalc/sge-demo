package br.com.sge.modules.rematricula.dto;

import java.util.List;

public record CampoFormularioDto(
        String id,
        String rotulo,
        String tipo,
        boolean obrigatorio,
        int ordem,
        List<String> opcoes) {}
