CREATE TABLE disciplina (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(20)
);

CREATE TABLE professor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL REFERENCES pessoa (id),
    usuario_id UUID REFERENCES usuario (id),
    registro_mec VARCHAR(50)
);

CREATE TABLE turma_disciplina_professor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turma_id UUID NOT NULL REFERENCES turma (id),
    disciplina_id UUID NOT NULL REFERENCES disciplina (id),
    professor_id UUID NOT NULL REFERENCES professor (id),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id)
);

CREATE TABLE periodo_avaliacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id),
    nome VARCHAR(50) NOT NULL,
    data_inicio DATE,
    data_fim DATE
);

CREATE TABLE nota (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    turma_disciplina_professor_id UUID NOT NULL REFERENCES turma_disciplina_professor (id),
    periodo_id UUID NOT NULL REFERENCES periodo_avaliacao (id),
    valor NUMERIC(4, 2) NOT NULL CHECK (valor >= 0 AND valor <= 10),
    tipo VARCHAR(30) NOT NULL,
    observacao TEXT,
    lancado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE presenca (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    turma_disciplina_professor_id UUID NOT NULL REFERENCES turma_disciplina_professor (id),
    data_aula DATE NOT NULL,
    presente BOOLEAN NOT NULL,
    justificativa TEXT,
    UNIQUE (aluno_id, turma_disciplina_professor_id, data_aula)
);

CREATE INDEX idx_nota_aluno ON nota (aluno_id);
CREATE INDEX idx_presenca_aluno ON presenca (aluno_id);
CREATE INDEX idx_tdp_turma ON turma_disciplina_professor (turma_id);
