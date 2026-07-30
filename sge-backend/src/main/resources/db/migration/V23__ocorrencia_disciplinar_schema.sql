-- Ocorrencias disciplinares (modulo Convivencia)
CREATE TABLE ocorrencia_disciplinar (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    turma_disciplina_professor_id UUID NOT NULL REFERENCES turma_disciplina_professor (id),
    data_ocorrencia DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ocorrencia_tdp ON ocorrencia_disciplinar (turma_disciplina_professor_id, data_ocorrencia DESC);
CREATE INDEX idx_ocorrencia_aluno ON ocorrencia_disciplinar (aluno_id, data_ocorrencia DESC);
