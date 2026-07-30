package br.com.sge.modules.auth.dto;

import java.util.UUID;

public record FilhoResumo(
        UUID alunoId,
        String nome,
        String matricula,
        String turmaNome,
        UUID turmaId,
        boolean autorizaUsoImagem) {}
