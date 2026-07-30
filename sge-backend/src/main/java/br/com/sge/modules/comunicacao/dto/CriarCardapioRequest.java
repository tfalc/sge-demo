package br.com.sge.modules.comunicacao.dto;

import br.com.sge.modules.comunicacao.entity.TipoRefeicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CriarCardapioRequest(
        @NotNull LocalDate dataRefeicao,
        @NotNull TipoRefeicao tipoRefeicao,
        @NotBlank String descricao,
        Integer calorias) {}
