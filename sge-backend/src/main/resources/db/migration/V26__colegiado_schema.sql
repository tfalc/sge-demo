CREATE TABLE colegiado_reuniao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'PEDAGOGICO',
    turma_id UUID REFERENCES turma (id),
    data_reuniao DATE NOT NULL,
    hora_reuniao TIME,
    pauta TEXT,
    ata_texto TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    concluida_em TIMESTAMPTZ
);

CREATE INDEX idx_colegiado_reuniao_turma ON colegiado_reuniao (turma_id, data_reuniao DESC);

CREATE TABLE colegiado_participante (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reuniao_id UUID NOT NULL REFERENCES colegiado_reuniao (id) ON DELETE CASCADE,
    usuario_id UUID REFERENCES usuario (id),
    nome_exibicao VARCHAR(200) NOT NULL,
    perfil VARCHAR(30)
);

CREATE INDEX idx_colegiado_participante_reuniao ON colegiado_participante (reuniao_id);

CREATE TABLE colegiado_encaminhamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reuniao_id UUID NOT NULL REFERENCES colegiado_reuniao (id) ON DELETE CASCADE,
    descricao TEXT NOT NULL,
    responsavel_usuario_id UUID REFERENCES usuario (id),
    responsavel_nome VARCHAR(200),
    prazo DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    concluido_em TIMESTAMPTZ
);

CREATE INDEX idx_colegiado_encaminhamento_status ON colegiado_encaminhamento (status, prazo);
