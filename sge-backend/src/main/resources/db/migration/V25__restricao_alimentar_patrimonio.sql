-- Restricoes alimentares por aluno (nutricao)
CREATE TABLE restricao_alimentar (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id) ON DELETE CASCADE,
    descricao VARCHAR(500) NOT NULL,
    severidade VARCHAR(20) NOT NULL DEFAULT 'MODERADA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_restricao_alimentar_aluno ON restricao_alimentar (aluno_id);

-- Inventario patrimonial basico
CREATE TABLE patrimonio_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    categoria VARCHAR(80),
    localizacao VARCHAR(200),
    numero_patrimonio VARCHAR(50),
    data_aquisicao DATE,
    valor_aquisicao NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patrimonio_status ON patrimonio_item (status, nome);
