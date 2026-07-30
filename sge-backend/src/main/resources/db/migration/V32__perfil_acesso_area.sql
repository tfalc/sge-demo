-- Matriz de menus superiores por perfil (configurável pelo ADMIN)
CREATE TABLE perfil_acesso_area (
    perfil VARCHAR(30) NOT NULL,
    area VARCHAR(40) NOT NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (perfil, area)
);

COMMENT ON TABLE perfil_acesso_area IS
    'Quais areas do menu superior cada perfil ve/acessa (pais, aluno, professor, secretaria, direcao, coordenacao, nutricao, psicologia)';

-- Defaults (ADMIN com todas as areas)
INSERT INTO perfil_acesso_area (perfil, area, habilitado) VALUES
    -- ADMIN: todas
    ('ADMIN', 'pais', TRUE),
    ('ADMIN', 'aluno', TRUE),
    ('ADMIN', 'professor', TRUE),
    ('ADMIN', 'secretaria', TRUE),
    ('ADMIN', 'direcao', TRUE),
    ('ADMIN', 'coordenacao', TRUE),
    ('ADMIN', 'nutricao', TRUE),
    ('ADMIN', 'psicologia', TRUE),
    -- DIRETOR
    ('DIRETOR', 'direcao', TRUE),
    ('DIRETOR', 'coordenacao', TRUE),
    -- COORDENADOR
    ('COORDENADOR', 'coordenacao', TRUE),
    -- PROFESSOR
    ('PROFESSOR', 'professor', TRUE),
    -- SECRETARIA
    ('SECRETARIA', 'secretaria', TRUE),
    -- PAI
    ('PAI', 'pais', TRUE),
    -- ALUNO
    ('ALUNO', 'aluno', TRUE),
    -- NUTRICIONISTA
    ('NUTRICIONISTA', 'nutricao', TRUE),
    -- PSICOLOGA
    ('PSICOLOGA', 'psicologia', TRUE);
