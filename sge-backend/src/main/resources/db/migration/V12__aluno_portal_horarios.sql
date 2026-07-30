ALTER TABLE aluno ADD COLUMN usuario_id UUID REFERENCES usuario(id);

CREATE TABLE horario_aula (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turma_id UUID NOT NULL REFERENCES turma(id),
    dia_semana SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 5),
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    disciplina_id UUID NOT NULL REFERENCES disciplina(id),
    professor_id UUID REFERENCES professor(id)
);

CREATE INDEX idx_horario_turma ON horario_aula(turma_id);
CREATE INDEX idx_horario_professor ON horario_aula(professor_id);

UPDATE pessoa SET email = 'aluno@sge.com' WHERE id = 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee';

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '12121212-1212-1212-1212-121212121212',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'aluno@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'ALUNO',
        TRUE
    );

UPDATE aluno
SET usuario_id = '12121212-1212-1212-1212-121212121212'
WHERE id = 'ffffffff-ffff-ffff-ffff-ffffffffffff';

INSERT INTO horario_aula (id, turma_id, dia_semana, hora_inicio, hora_fim, disciplina_id, professor_id)
VALUES (
        'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1',
        '55555555-5555-5555-5555-555555555555',
        1,
        '08:00',
        '09:30',
        '70707070-7070-7070-7070-707070707070',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2',
        '55555555-5555-5555-5555-555555555555',
        1,
        '09:30',
        '11:00',
        '80808080-8080-8080-8080-808080808080',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3',
        '55555555-5555-5555-5555-555555555555',
        3,
        '08:00',
        '09:30',
        '70707070-7070-7070-7070-707070707070',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4',
        '55555555-5555-5555-5555-555555555555',
        5,
        '08:00',
        '09:30',
        '80808080-8080-8080-8080-808080808080',
        '60606060-6060-6060-6060-606060606060'
    );
