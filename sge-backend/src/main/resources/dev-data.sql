-- Mesmo conteudo do V2__seed.sql, com datas fixas (sem date_trunc) para H2 em modo dev
INSERT INTO escola (id, nome, cnpj, slug, municipio, uf, package_id, nota_minima_aprovacao, frequencia_minima, criado_em)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Escola Modelo Demo',
    '12.345.678/0001-90',
    'escola-demo',
    'Cidade Exemplo',
    'SP',
    'escola-demo',
    6.00,
    75.00,
    CURRENT_TIMESTAMP
);

INSERT INTO ano_letivo (id, escola_id, ano, data_inicio, data_fim)
VALUES (
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        2026,
        '2026-02-01',
        '2026-12-20'
    );

INSERT INTO nivel_ensino (id, nome, descricao)
VALUES ('33333333-3333-3333-3333-333333333333', 'Ensino Fundamental I', NULL);

INSERT INTO serie (id, nivel_id, nome, ordem)
VALUES ('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', '3 Ano', 3);

INSERT INTO turma (id, serie_id, ano_letivo_id, nome, capacidade_max)
VALUES (
        '55555555-5555-5555-5555-555555555555',
        '44444444-4444-4444-4444-444444444444',
        '22222222-2222-2222-2222-222222222222',
        '3A',
        30
    );

INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento, criado_em)
VALUES (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Admin SGE',
        NULL,
        'admin@sge.com',
        NULL,
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Maria Responsavel',
        NULL,
        'pai@sge.com',
        NULL,
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'Joao Silva',
        NULL,
        'aluno@sge.com',
        NULL,
        '2018-05-10',
        CURRENT_TIMESTAMP
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'admin@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'ADMIN',
        TRUE
    ),
    (
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'pai@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'PAI',
        TRUE
    ),
    (
        '12121212-1212-1212-1212-121212121212',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'aluno@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'ALUNO',
        TRUE
    );

INSERT INTO aluno (id, pessoa_id, matricula, turma_id, usuario_id, status)
VALUES (
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'MAT-2026-001',
        '55555555-5555-5555-5555-555555555555',
        '12121212-1212-1212-1212-121212121212',
        'ATIVO'
    );

INSERT INTO responsavel (id, pessoa_id, usuario_id, grau_parentesco)
VALUES (
        '00000000-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'Mae'
    );

INSERT INTO aluno_responsavel (aluno_id, responsavel_id)
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', '00000000-0000-0000-0000-000000000001');

INSERT INTO plano_pagamento (id, nome, valor_mensalidade, dia_vencimento)
VALUES (
        '10101010-1010-1010-1010-101010101010',
        'Mensalidade integral',
        850.00,
        10
    );

INSERT INTO contrato (id, aluno_id, plano_id, ano_letivo_id, data_inicio, status)
VALUES (
        '20202020-2020-2020-2020-202020202020',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '10101010-1010-1010-1010-101010101010',
        '22222222-2222-2222-2222-222222222222',
        '2026-02-01',
        'ATIVO'
    );

INSERT INTO cobranca (id, contrato_id, competencia, valor, vencimento, status, pix_txid, pix_qrcode, pago_em)
VALUES (
        '30303030-3030-3030-3030-303030303030',
        '20202020-2020-2020-2020-202020202020',
        '2026-01-01',
        850.00,
        '2026-01-10',
        'PENDENTE',
        NULL,
        '00020126450014BR.GOV.BCB.PIX0114+5511999999995204000053039865406850.005802BR5920ESCOLA EXEMPLO LTDA6009SAO PAULO62070503***6304ABCD',
        NULL
    );

INSERT INTO cobranca (id, contrato_id, competencia, valor, vencimento, status, pix_txid, pix_qrcode, pago_em)
VALUES (
        '40404040-4040-4040-4040-404040404040',
        '20202020-2020-2020-2020-202020202020',
        '2026-04-01',
        850.00,
        '2026-04-09',
        'PAGO',
        'tx-seed-1',
        NULL,
        CURRENT_TIMESTAMP
    );

-- Academico (mesmos IDs do V4__academico_seed.sql)
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento, criado_em)
VALUES (
        '11000000-0011-0011-0011-000000000011',
        'Roberto Professor',
        NULL,
        'prof@sge.com',
        NULL,
        '1985-03-15',
        CURRENT_TIMESTAMP
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

INSERT INTO nota (id, aluno_id, turma_disciplina_professor_id, periodo_id, valor, tipo, observacao, lancado_em)
VALUES (
        'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        7.50,
        'PROVA',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '90909090-9090-9090-9090-909090909090',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        8.00,
        'TRABALHO',
        NULL,
        CURRENT_TIMESTAMP
    ),
    (
        'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        9.00,
        'PROVA',
        NULL,
        CURRENT_TIMESTAMP
    );

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

-- Comunicacao (V6 seed adaptado para H2)
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento, criado_em)
VALUES (
        '22000000-0022-0022-0022-000000000022',
        'Ana Nutricionista',
        NULL,
        'nutri@sge.com',
        NULL,
        '1990-08-20',
        CURRENT_TIMESTAMP
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '22000000-0022-0022-0022-000000000023',
        '22000000-0022-0022-0022-000000000022',
        'nutri@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'NUTRICIONISTA',
        TRUE
    );

INSERT INTO comunicado (id, titulo, conteudo, publicado_por, publicado_em, visivel_para, turma_id)
VALUES (
        '33000000-0033-0033-0033-000000000031',
        'Reuniao de pais — turma 3A',
        'Convidamos os responsaveis da turma 3A para reuniao dia 15/06 as 19h no auditorio.',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        CURRENT_TIMESTAMP,
        'PAIS',
        '55555555-5555-5555-5555-555555555555'
    ),
    (
        '33000000-0033-0033-0033-000000000032',
        'Feriado municipal',
        'Nao ha aula na proxima segunda-feira (feriado municipal).',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        CURRENT_TIMESTAMP,
        'TODOS',
        NULL
    );

INSERT INTO cardapio (id, data_refeicao, tipo_refeicao, descricao, calorias, nutricionista_id)
VALUES (
        '44000000-0044-0044-0044-000000000041',
        CURRENT_DATE,
        'ALMOCO',
        'Arroz, feijao, frango grelhado, salada de alface e tomate, suco de laranja.',
        520,
        '22000000-0022-0022-0022-000000000023'
    ),
    (
        '44000000-0044-0044-0044-000000000042',
        CURRENT_DATE,
        'LANCHE',
        'Banana, biscoito integral e agua.',
        180,
        '22000000-0022-0022-0022-000000000023'
    );

INSERT INTO evento_agenda (id, titulo, descricao, data_inicio, data_fim, tipo, turma_id)
VALUES (
        '55000000-0055-0055-0055-000000000051',
        'Prova de Matematica — 3A',
        'Avaliacao do 2 bimestre de Matematica.',
        TIMESTAMP '2026-06-16 08:00:00',
        TIMESTAMP '2026-06-16 10:00:00',
        'PROVA',
        '55555555-5555-5555-5555-555555555555'
    ),
    (
        '55000000-0055-0055-0055-000000000052',
        'Reuniao de pais',
        'Encontro geral com responsaveis no auditorio.',
        TIMESTAMP '2026-06-19 19:00:00',
        TIMESTAMP '2026-06-19 21:00:00',
        'REUNIAO',
        NULL
    );

-- Saude, coordenacao e direcao (V8 seed adaptado para H2)
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento, criado_em)
VALUES (
        '66000000-0066-0066-0066-000000000061',
        'Carla Psicologa',
        NULL,
        'psico@sge.com',
        NULL,
        '1988-11-05',
        CURRENT_TIMESTAMP
    ),
    (
        '77000000-0077-0077-0077-000000000071',
        'Paula Coordenadora',
        NULL,
        'coord@sge.com',
        NULL,
        '1982-04-12',
        CURRENT_TIMESTAMP
    ),
    (
        '88000000-0088-0088-0088-000000000081',
        'Marcos Diretor',
        NULL,
        'diretor@sge.com',
        NULL,
        '1975-09-30',
        CURRENT_TIMESTAMP
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '66000000-0066-0066-0066-000000000062',
        '66000000-0066-0066-0066-000000000061',
        'psico@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'PSICOLOGA',
        TRUE
    ),
    (
        '77000000-0077-0077-0077-000000000072',
        '77000000-0077-0077-0077-000000000071',
        'coord@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'COORDENADOR',
        TRUE
    ),
    (
        '88000000-0088-0088-0088-000000000082',
        '88000000-0088-0088-0088-000000000081',
        'diretor@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'DIRETOR',
        TRUE
    );

INSERT INTO profissional_saude (id, pessoa_id, usuario_id, especialidade, registro_conselho)
VALUES (
        '66000000-0066-0066-0066-000000000063',
        '66000000-0066-0066-0066-000000000061',
        '66000000-0066-0066-0066-000000000062',
        'PSICOLOGA',
        'CRP-12345'
    );

INSERT INTO agendamento_saude (id, aluno_id, profissional_id, data_hora, status, observacoes, privado)
VALUES (
        '99000000-0099-0099-0099-000000000091',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '66000000-0066-0066-0066-000000000063',
        TIMESTAMPADD('DAY', 3, CURRENT_TIMESTAMP),
        'AGENDADO',
        'Primeira conversa com responsavel — sigilo clinico.',
        TRUE
    ),
    (
        'aa000000-00aa-00aa-00aa-0000000000aa',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '66000000-0066-0066-0066-000000000063',
        TIMESTAMPADD('DAY', 7, CURRENT_TIMESTAMP),
        'AGENDADO',
        'Conversa de acolhimento com a familia — horario confirmado.',
        FALSE
    );

INSERT INTO horario_aula (id, turma_id, dia_semana, hora_inicio, hora_fim, disciplina_id, professor_id)
VALUES (
        'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1',
        '55555555-5555-5555-5555-555555555555',
        1,
        '08:00:00',
        '09:30:00',
        '70707070-7070-7070-7070-707070707070',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2',
        '55555555-5555-5555-5555-555555555555',
        1,
        '09:30:00',
        '11:00:00',
        '80808080-8080-8080-8080-808080808080',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3',
        '55555555-5555-5555-5555-555555555555',
        3,
        '08:00:00',
        '09:30:00',
        '70707070-7070-7070-7070-707070707070',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4',
        '55555555-5555-5555-5555-555555555555',
        5,
        '08:00:00',
        '09:30:00',
        '80808080-8080-8080-8080-808080808080',
        '60606060-6060-6060-6060-606060606060'
    );
