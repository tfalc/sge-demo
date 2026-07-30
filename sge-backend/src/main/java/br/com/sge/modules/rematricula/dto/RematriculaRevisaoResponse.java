package br.com.sge.modules.rematricula.dto;

import java.util.List;
import java.util.Map;

public record RematriculaRevisaoResponse(
        String alunoId,
        String alunoNome,
        String tituloFormulario,
        List<SecaoRevisaoDto> secoes,
        List<String> erros) {}
