-- CEM Monnerat: identidade da escola + matriz curricular (normativa RJ)

ALTER TABLE escola
    ADD COLUMN slug VARCHAR(80),
    ADD COLUMN municipio VARCHAR(100),
    ADD COLUMN uf CHAR(2),
    ADD COLUMN package_id VARCHAR(80);

UPDATE escola
SET
    nome = 'Centro Educacional Monnerat',
    slug = 'cem-monnerat',
    municipio = 'Nova Friburgo',
    uf = 'RJ',
    package_id = 'cem-monnerat'
WHERE id = '11111111-1111-1111-1111-111111111111';

CREATE TABLE matriz_curricular (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL REFERENCES escola (id),
    serie_id UUID REFERENCES serie (id),
    codigo VARCHAR(80) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    etapa VARCHAR(40) NOT NULL,
    modalidade VARCHAR(40) NOT NULL,
    aulas_semanais_total INT NOT NULL,
    minutos_aula INT NOT NULL DEFAULT 50,
    horas_anuais_minimas INT NOT NULL DEFAULT 800,
    normativa_ref TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_matriz_escola_codigo ON matriz_curricular (escola_id, codigo);

CREATE TABLE matriz_componente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    matriz_id UUID NOT NULL REFERENCES matriz_curricular (id) ON DELETE CASCADE,
    componente VARCHAR(120) NOT NULL,
    area VARCHAR(80),
    aulas_semanais INT NOT NULL,
    base_nacional_comum BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL DEFAULT 0
);

-- Matriz EF anos iniciais — regular parcial (Res. SEEDUC 4746; DOC RJ)
INSERT INTO matriz_curricular (
    id,
    escola_id,
    serie_id,
    codigo,
    nome,
    etapa,
    modalidade,
    aulas_semanais_total,
    minutos_aula,
    horas_anuais_minimas,
    normativa_ref
)
VALUES (
    '13131313-1313-1313-1313-131313131313',
    '11111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444444',
    'ef-ai-regular-parcial',
    'EF Anos Iniciais — Regular Parcial',
    'ANOS_INICIAIS',
    'REGULAR_PARCIAL',
    20,
    50,
    800,
    'Res. SEEDUC 4746/2011 Anexo I; DOC RJ 2019'
);

INSERT INTO matriz_componente (id, matriz_id, componente, area, aulas_semanais, base_nacional_comum, ordem)
VALUES
    ('13201320-1320-1320-1320-132013201320', '13131313-1313-1313-1313-131313131313', 'Lingua Portuguesa', 'Linguagens', 5, TRUE, 1),
    ('13211321-1321-1321-1321-132113211321', '13131313-1313-1313-1313-131313131313', 'Matematica', 'Matematica', 5, TRUE, 2),
    ('13221322-1322-1322-1322-132213221322', '13131313-1313-1313-1313-131313131313', 'Ciencias', 'Ciencias da Natureza', 3, TRUE, 3),
    ('13231323-1323-1323-1323-132313231323', '13131313-1313-1313-1313-131313131313', 'Historia', 'Ciencias Humanas', 2, TRUE, 4),
    ('13241324-1324-1324-1324-132413241324', '13131313-1313-1313-1313-131313131313', 'Geografia', 'Ciencias Humanas', 2, TRUE, 5),
    ('13251325-1325-1325-1325-132513251325', '13131313-1313-1313-1313-131313131313', 'Arte', 'Linguagens', 2, TRUE, 6),
    ('13261326-1326-1326-1326-132613261326', '13131313-1313-1313-1313-131313131313', 'Educacao Fisica', 'Linguagens', 1, TRUE, 7);

-- Disciplinas adicionais para alinhar com a matriz (turma 3A)
INSERT INTO disciplina (id, nome, codigo)
VALUES
    ('71717171-7171-7171-7171-717171717171', 'Ciencias', 'CIE'),
    ('72727272-7272-7272-7272-727272727272', 'Historia', 'HIS'),
    ('73737373-7373-7373-7373-737373737373', 'Geografia', 'GEO'),
    ('74747474-7474-7474-7474-747474747474', 'Arte', 'ART'),
    ('75757575-7575-7575-7575-757575757575', 'Educacao Fisica', 'EDF');
