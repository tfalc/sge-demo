-- Professor de teste: prof@sge.com / admin123
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        '11000000-0011-0011-0011-000000000011',
        'Roberto Professor',
        NULL,
        'prof@sge.com',
        NULL,
        '1985-03-15'
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '11000000-0011-0011-0011-000000000012',
        '11000000-0011-0011-0011-000000000011',
        'prof@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'PROFESSOR',
        TRUE
    );

INSERT INTO professor (id, pessoa_id, usuario_id, registro_mec)
VALUES (
        '60606060-6060-6060-6060-606060606060',
        '11000000-0011-0011-0011-000000000011',
        '11000000-0011-0011-0011-000000000012',
        'MEC-12345'
    );

INSERT INTO disciplina (id, nome, codigo)
VALUES ('70707070-7070-7070-7070-707070707070', 'Matematica', 'MAT'),
    ('80808080-8080-8080-8080-808080808080', 'Portugues', 'POR');

INSERT INTO turma_disciplina_professor (id, turma_id, disciplina_id, professor_id, ano_letivo_id)
VALUES (
        '90909090-9090-9090-9090-909090909090',
        '55555555-5555-5555-5555-555555555555',
        '70707070-7070-7070-7070-707070707070',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        'a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0',
        '55555555-5555-5555-5555-555555555555',
        '80808080-8080-8080-8080-808080808080',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    );

INSERT INTO periodo_avaliacao (id, ano_letivo_id, nome, data_inicio, data_fim)
VALUES (
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        '22222222-2222-2222-2222-222222222222',
        '1 Bimestre',
        '2026-02-01',
        '2026-04-30'
    ),
    (
        'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2',
        '22222222-2222-2222-2222-222222222222',
        '2 Bimestre',
        '2026-05-01',
        '2026-07-31'
    );

-- Notas iniciais do Joao Silva (1 bimestre)
INSERT INTO nota (id, aluno_id, turma_disciplina_professor_id, periodo_id, valor, tipo, observacao)
VALUES (
        'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        7.50,
        'PROVA',
        NULL
    ),
    (
        'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        8.00,
        'TRABALHO',
        NULL
    ),
    (
        'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        9.00,
        'PROVA',
        NULL
    );

-- Frequencia inicial (Matematica, 5 de 6 aulas)
INSERT INTO presenca (id, aluno_id, turma_disciplina_professor_id, data_aula, presente, justificativa)
VALUES (
        'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-03',
        TRUE,
        NULL
    ),
    (
        'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-10',
        TRUE,
        NULL
    ),
    (
        'd3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-17',
        FALSE,
        'Consulta medica'
    ),
    (
        'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-24',
        TRUE,
        NULL
    ),
    (
        'd5d5d5d5-d5d5-d5d5-d5d5-d5d5d5d5d5d5',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-31',
        TRUE,
        NULL
    ),
    (
        'd6d6d6d6-d6d6-d6d6-d6d6-d6d6d6d6d6d6',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        '2026-04-07',
        TRUE,
        NULL
    );
