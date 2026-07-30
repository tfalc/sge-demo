package br.com.sge.modules.rematricula.dto;

import java.util.List;
import java.util.Map;

public record RematriculaPortalResponse(
        boolean habilitada,
        String titulo,
        FormularioRematriculaDto formulario,
        List<AlunoRematriculaPortalDto> alunos) {}
