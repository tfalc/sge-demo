-- Psicologa: psico@sge.com / admin123
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        '66000000-0066-0066-0066-000000000061',
        'Carla Psicologa',
        NULL,
        'psico@sge.com',
        NULL,
        '1988-11-05'
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '66000000-0066-0066-0066-000000000062',
        '66000000-0066-0066-0066-000000000061',
        'psico@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'PSICOLOGA',
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

-- Coordenacao: coord@sge.com / admin123
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        '77000000-0077-0077-0077-000000000071',
        'Paula Coordenadora',
        NULL,
        'coord@sge.com',
        NULL,
        '1982-04-12'
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '77000000-0077-0077-0077-000000000072',
        '77000000-0077-0077-0077-000000000071',
        'coord@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'COORDENADOR',
        TRUE
    );

-- Direcao: diretor@sge.com / admin123
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        '88000000-0088-0088-0088-000000000081',
        'Marcos Diretor',
        NULL,
        'diretor@sge.com',
        NULL,
        '1975-09-30'
    );

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        '88000000-0088-0088-0088-000000000082',
        '88000000-0088-0088-0088-000000000081',
        'diretor@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'DIRETOR',
        TRUE
    );

INSERT INTO agendamento_saude (id, aluno_id, profissional_id, data_hora, status, observacoes, privado)
VALUES (
        '99000000-0099-0099-0099-000000000091',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '66000000-0066-0066-0066-000000000063',
        (CURRENT_DATE + INTERVAL '3 day')::date + TIME '14:00',
        'AGENDADO',
        'Primeira conversa com responsavel — sigilo clinico.',
        TRUE
    );
