-- IDs fixos para ambiente local / testes (responsavel 000...001 usado pelo frontend MVP)
INSERT INTO escola (id, nome, cnpj)
VALUES ('11111111-1111-1111-1111-111111111111', 'Escola Exemplo', '12.345.678/0001-90');

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

INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Admin SGE',
        NULL,
        'admin@sge.com',
        NULL,
        NULL
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Maria Responsavel',
        NULL,
        'pai@sge.com',
        NULL,
        NULL
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'Joao Silva',
        NULL,
        NULL,
        NULL,
        '2018-05-10'
    );

-- senha: admin123 (BCrypt strength 12)
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
    );

INSERT INTO aluno (id, pessoa_id, matricula, turma_id, status)
VALUES (
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'MAT-2026-001',
        '55555555-5555-5555-5555-555555555555',
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

-- Cobranca em atraso (inadimplente)
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

-- Cobranca paga no mes corrente (relatorio)
INSERT INTO cobranca (id, contrato_id, competencia, valor, vencimento, status, pix_txid, pix_qrcode, pago_em)
VALUES (
        '40404040-4040-4040-4040-404040404040',
        '20202020-2020-2020-2020-202020202020',
        date_trunc('month', CURRENT_DATE)::date,
        850.00,
        (date_trunc('month', CURRENT_DATE) + INTERVAL '9 day')::date,
        'PAGO',
        'tx-seed-1',
        NULL,
        NOW()
    );
