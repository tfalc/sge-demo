CREATE TABLE escola (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    cnpj VARCHAR(18),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ano_letivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL REFERENCES escola (id),
    ano INT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL
);

CREATE TABLE nivel_ensino (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT
);

CREATE TABLE serie (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nivel_id UUID NOT NULL REFERENCES nivel_ensino (id),
    nome VARCHAR(50) NOT NULL,
    ordem INT NOT NULL
);

CREATE TABLE turma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    serie_id UUID NOT NULL REFERENCES serie (id),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id),
    nome VARCHAR(20) NOT NULL,
    capacidade_max INT DEFAULT 30
);

CREATE TABLE pessoa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(200),
    telefone VARCHAR(20),
    data_nascimento DATE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID REFERENCES pessoa (id),
    email VARCHAR(200) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL REFERENCES pessoa (id),
    matricula VARCHAR(20) NOT NULL UNIQUE,
    turma_id UUID REFERENCES turma (id),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE responsavel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id UUID NOT NULL REFERENCES pessoa (id),
    usuario_id UUID REFERENCES usuario (id),
    grau_parentesco VARCHAR(50)
);

CREATE TABLE aluno_responsavel (
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    responsavel_id UUID NOT NULL REFERENCES responsavel (id),
    PRIMARY KEY (aluno_id, responsavel_id)
);

CREATE TABLE plano_pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    valor_mensalidade NUMERIC(10, 2) NOT NULL,
    dia_vencimento INT NOT NULL DEFAULT 10
);

CREATE TABLE contrato (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    plano_id UUID NOT NULL REFERENCES plano_pagamento (id),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id),
    data_inicio DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE cobranca (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contrato_id UUID NOT NULL REFERENCES contrato (id),
    competencia DATE NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    pix_txid VARCHAR(100),
    pix_qrcode TEXT,
    pago_em TIMESTAMPTZ
);

CREATE INDEX idx_cobranca_contrato ON cobranca (contrato_id);
CREATE INDEX idx_contrato_aluno ON contrato (aluno_id);
CREATE INDEX idx_usuario_email ON usuario (email);
