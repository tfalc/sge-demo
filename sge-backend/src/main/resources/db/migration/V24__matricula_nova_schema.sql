-- Matricula nova (processo de ingresso + GED)
CREATE TABLE matricula_processo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id),
    turma_pretendida_id UUID REFERENCES turma (id),
    responsavel_id UUID REFERENCES responsavel (id),
    aluno_id UUID REFERENCES aluno (id),
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    candidato_nome VARCHAR(200) NOT NULL,
    matricula_sugerida VARCHAR(20),
    responsavel_nome VARCHAR(200),
    responsavel_email VARCHAR(200),
    responsavel_telefone VARCHAR(30),
    observacoes TEXT,
    motivo_rejeicao TEXT,
    criado_por_usuario_id UUID REFERENCES usuario (id),
    enviado_em TIMESTAMPTZ,
    aprovado_em TIMESTAMPTZ,
    rejeitado_em TIMESTAMPTZ,
    concluido_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE matricula_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    processo_id UUID NOT NULL REFERENCES matricula_processo (id) ON DELETE CASCADE,
    tipo VARCHAR(40) NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    enviado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_matricula_processo_status ON matricula_processo (status, criado_em DESC);
CREATE INDEX idx_matricula_documento_processo ON matricula_documento (processo_id);
