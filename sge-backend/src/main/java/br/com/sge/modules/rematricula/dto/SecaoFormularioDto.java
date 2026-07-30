package br.com.sge.modules.rematricula.dto;

import java.util.List;

public record SecaoFormularioDto(String id, String titulo, int ordem, List<CampoFormularioDto> campos) {}
