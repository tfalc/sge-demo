-- Modo normativo (rigido) vs diretrizes (flexivel)

ALTER TABLE matriz_curricular
    ADD COLUMN modo_validacao VARCHAR(20) NOT NULL DEFAULT 'NORMATIVO',
    ADD COLUMN aulas_semanais_total_min INT,
    ADD COLUMN aulas_semanais_total_max INT;

ALTER TABLE matriz_componente
    ADD COLUMN aulas_semanais_min INT,
    ADD COLUMN aulas_semanais_max INT;

UPDATE matriz_curricular
SET modo_validacao = 'NORMATIVO'
WHERE id = '13131313-1313-1313-1313-131313131313';

-- Matriz flexivel (diretrizes RJ) para 3 Ano
INSERT INTO matriz_curricular (
    id,
    escola_id,
    serie_id,
    codigo,
    nome,
    etapa,
    modalidade,
    modo_validacao,
    aulas_semanais_total,
    aulas_semanais_total_min,
    aulas_semanais_total_max,
    minutos_aula,
    horas_anuais_minimas,
    normativa_ref
)
VALUES (
    '14141414-1414-1414-1414-141414141414',
    '11111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444444',
    'ef-ai-diretrizes-rj',
    'EF Anos Iniciais — Diretrizes RJ (flexivel)',
    'ANOS_INICIAIS',
    'DIRETRIZES_RJ',
    'DIRETRIZES',
    20,
    16,
    25,
    50,
    800,
    'DOC RJ 2019; BNCC; LDB 800h/ano — carga distribuivel'
);

INSERT INTO matriz_componente (
    id, matriz_id, componente, area, aulas_semanais, aulas_semanais_min, aulas_semanais_max,
    base_nacional_comum, ordem
)
VALUES
    ('14201420-1420-1420-1420-142014201420', '14141414-1414-1414-1414-141414141414', 'Lingua Portuguesa', 'Linguagens', 5, 4, 6, TRUE, 1),
    ('14211421-1421-1421-1421-142114211421', '14141414-1414-1414-1414-141414141414', 'Matematica', 'Matematica', 5, 4, 6, TRUE, 2),
    ('14221422-1422-1422-1422-142214221422', '14141414-1414-1414-1414-141414141414', 'Ciencias', 'Ciencias da Natureza', 3, 2, 4, TRUE, 3),
    ('14231423-1423-1423-1423-142314231423', '14141414-1414-1414-1414-141414141414', 'Historia', 'Ciencias Humanas', 2, 1, 3, TRUE, 4),
    ('14241424-1424-1424-1424-142414241424', '14141414-1414-1414-1414-141414141414', 'Geografia', 'Ciencias Humanas', 2, 1, 3, TRUE, 5),
    ('14251425-1425-1425-1425-142514251425', '14141414-1414-1414-1414-141414141414', 'Arte', 'Linguagens', 2, 1, 3, TRUE, 6),
    ('14261426-1426-1426-1426-142614261426', '14141414-1414-1414-1414-141414141414', 'Educacao Fisica', 'Linguagens', 1, 1, 2, TRUE, 7);
