package br.com.sge.modules.galeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CriarGaleriaAlbumRequest(
        @NotBlank @Size(max = 200) String titulo,
        String descricao,
        @NotBlank @Size(max = 100) String visivelPara,
        UUID turmaId,
        Boolean exigirConsentimentoImagem) {}
