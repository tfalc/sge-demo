package br.com.sge.modules.rematricula.dto;

import java.util.List;

public record AtualizarRematriculaConfigRequest(
        String titulo,
        Boolean habilitada,
        FormularioRematriculaDto formulario) {}
