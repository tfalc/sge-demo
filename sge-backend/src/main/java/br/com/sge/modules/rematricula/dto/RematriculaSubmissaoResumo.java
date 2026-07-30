package br.com.sge.modules.rematricula.dto;

public record RematriculaSubmissaoResumo(
        String id,
        String alunoId,
        String alunoNome,
        String turmaNome,
        String status,
        String enviadoEm,
        String validadoSecretariaEm) {}
