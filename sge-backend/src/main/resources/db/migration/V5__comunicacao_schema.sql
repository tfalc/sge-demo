CREATE TABLE comunicado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    conteudo TEXT NOT NULL,
    publicado_por UUID REFERENCES usuario (id),
    publicado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    visivel_para VARCHAR(100) NOT NULL,
    turma_id UUID REFERENCES turma (id)
);

CREATE TABLE cardapio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_refeicao DATE NOT NULL,
    tipo_refeicao VARCHAR(30) NOT NULL,
    descricao TEXT NOT NULL,
    calorias INT,
    nutricionista_id UUID REFERENCES usuario (id)
);

CREATE TABLE evento_agenda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    data_inicio TIMESTAMPTZ NOT NULL,
    data_fim TIMESTAMPTZ,
    tipo VARCHAR(50),
    turma_id UUID REFERENCES turma (id)
);

CREATE INDEX idx_comunicado_publicado_em ON comunicado (publicado_em DESC);
CREATE INDEX idx_cardapio_data ON cardapio (data_refeicao);
CREATE INDEX idx_evento_inicio ON evento_agenda (data_inicio);
