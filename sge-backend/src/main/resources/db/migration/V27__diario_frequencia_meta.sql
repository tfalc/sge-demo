CREATE TABLE diario_frequencia_meta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turma_disciplina_professor_id UUID NOT NULL REFERENCES turma_disciplina_professor (id) ON DELETE CASCADE,
    periodo_avaliacao_id UUID NOT NULL REFERENCES periodo_avaliacao (id) ON DELETE CASCADE,
    aulas_previstas INT,
    assinatura_em TIMESTAMPTZ,
    UNIQUE (turma_disciplina_professor_id, periodo_avaliacao_id)
);

CREATE INDEX idx_diario_freq_meta_tdp ON diario_frequencia_meta (turma_disciplina_professor_id);
