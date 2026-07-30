package br.com.sge.modules.rematricula.dto;

import java.util.Map;

public record AlunoRematriculaPortalDto(
        String alunoId,
        String alunoNome,
        String turmaNome,
        String statusSubmissao,
        Map<String, Object> respostas) {}
