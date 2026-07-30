CREATE TABLE profissional_saude (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL REFERENCES pessoa (id),
    usuario_id UUID REFERENCES usuario (id),
    especialidade VARCHAR(100),
    registro_conselho VARCHAR(50)
);

CREATE TABLE agendamento_saude (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    profissional_id UUID NOT NULL REFERENCES profissional_saude (id),
    data_hora TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    observacoes TEXT,
    privado BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_agendamento_aluno ON agendamento_saude (aluno_id);
CREATE INDEX idx_agendamento_profissional ON agendamento_saude (profissional_id);
