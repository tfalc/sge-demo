package br.com.sge.modules.academico.entity;

public enum TipoNota {
    PROVA,
    TRABALHO,
    PARTICIPACAO,
    /** Nota final do bimestre (fichario / diario de classe). */
    FINAL,
    /** Atividade complementar ou revisao pontual. */
    COMPLEMENTAR
}
