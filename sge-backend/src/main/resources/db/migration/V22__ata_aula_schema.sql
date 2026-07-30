-- Ata de aula (diario do professor)
CREATE TABLE ata_aula (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turma_disciplina_professor_id UUID NOT NULL REFERENCES turma_disciplina_professor (id),
    data_aula DATE NOT NULL,
    conteudo TEXT,
    tarefa_casa TEXT,
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ata_tdp_data UNIQUE (turma_disciplina_professor_id, data_aula)
);
