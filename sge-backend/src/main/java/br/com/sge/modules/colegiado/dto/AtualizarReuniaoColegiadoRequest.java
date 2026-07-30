package br.com.sge.modules.colegiado.dto;

import java.time.LocalDate;

public record AtualizarReuniaoColegiadoRequest(
        String titulo,
        String tipo,
        LocalDate dataReuniao,
        String horaReuniao,
        String pauta,
        String ataTexto,
        String status) {}
