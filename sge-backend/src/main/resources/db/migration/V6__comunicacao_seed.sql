-- Nutricionista de teste: nutri@sge.com / admin123
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        '22000000-0022-0022-0022-000000000022',
        'Ana Nutricionista',
        NULL,
        'nutri@sge.com',
        NULL,
        '1990-08-20'
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
        NOW() - INTERVAL '2 day',
        'PAIS',
        '55555555-5555-5555-5555-555555555555'
    ),
    (
        '33000000-0033-0033-0033-000000000032',
        'Feriado municipal',
        'Nao ha aula na proxima segunda-feira (feriado municipal).',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        NOW() - INTERVAL '1 day',
        'TODOS',
        NULL
    ),
    (
        '33000000-0033-0033-0033-000000000033',
        'Calendario de provas do 2 bimestre',
        'Professores: o calendario de provas do 2 bimestre esta disponivel na agenda escolar.',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        NOW(),
        'PROFESSORES',
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
    ),
    (
        '44000000-0044-0044-0044-000000000043',
        CURRENT_DATE + 1,
        'ALMOCO',
        'Macarrao ao sugo, carne moida, legumes cozidos e sobremesa de fruta.',
        490,
        '22000000-0022-0022-0022-000000000023'
    );

INSERT INTO evento_agenda (id, titulo, descricao, data_inicio, data_fim, tipo, turma_id)
VALUES (
        '55000000-0055-0055-0055-000000000051',
        'Prova de Matematica — 3A',
        'Avaliacao do 2 bimestre de Matematica.',
        (CURRENT_DATE + INTERVAL '7 day')::date + TIME '08:00',
        (CURRENT_DATE + INTERVAL '7 day')::date + TIME '10:00',
        'PROVA',
        '55555555-5555-5555-5555-555555555555'
    ),
    (
        '55000000-0055-0055-0055-000000000052',
        'Reuniao de pais',
        'Encontro geral com responsaveis no auditorio.',
        (CURRENT_DATE + INTERVAL '10 day')::date + TIME '19:00',
        (CURRENT_DATE + INTERVAL '10 day')::date + TIME '21:00',
        'REUNIAO',
        NULL
    ),
    (
        '55000000-0055-0055-0055-000000000053',
        'Feriado — Corpus Christi',
        'Nao ha expediente escolar.',
        (CURRENT_DATE + INTERVAL '14 day')::date + TIME '00:00',
        (CURRENT_DATE + INTERVAL '14 day')::date + TIME '23:59',
        'FERIADO',
        NULL
    );
