CREATE TABLE galeria_album (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    visivel_para VARCHAR(100) NOT NULL DEFAULT 'TODOS',
    turma_id UUID REFERENCES turma (id),
    publicado_por UUID REFERENCES usuario (id),
    publicado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE galeria_foto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    album_id UUID NOT NULL REFERENCES galeria_album (id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    legenda VARCHAR(500),
    ordem INT NOT NULL DEFAULT 0,
    enviado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_galeria_album_publicado ON galeria_album (publicado_em DESC);
CREATE INDEX idx_galeria_foto_album ON galeria_foto (album_id, ordem);
