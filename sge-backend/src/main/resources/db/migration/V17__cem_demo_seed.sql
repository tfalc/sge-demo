-- Seed enriquecido para demo CEM Monnerat (direcao do colegio)
-- Turma 3A alinhada a matriz EF anos iniciais; cobrancas e alunos para narrativa da demo.

-- Identidade da escola
UPDATE escola
SET
    cnpj = '28.456.789/0001-42',
    nota_minima_aprovacao = 6.00,
    frequencia_minima = 75.00
WHERE id = '11111111-1111-1111-1111-111111111111';

UPDATE plano_pagamento
SET nome = 'Mensalidade CEM — Ensino Fundamental'
WHERE id = '10101010-1010-1010-1010-101010101010';

-- Nomes e contexto Monnerat / Nova Friburgo
UPDATE pessoa SET nome = 'Maria Oliveira' WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccccc';
UPDATE pessoa SET nome = 'Prof. Roberto Mendes' WHERE id = '11000000-0011-0011-0011-000000000011';

-- Vinculos turma 3A — componentes da matriz (alem de Matematica e Portugues)
INSERT INTO turma_disciplina_professor (id, turma_id, disciplina_id, professor_id, ano_letivo_id)
VALUES
    (
        'e9090909-e909-e909-e909-e90909090909',
        '55555555-5555-5555-5555-555555555555',
        '71717171-7171-7171-7171-717171717171',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        'e0a0a0a0-e0a0-e0a0-e0a0-e0a0a0a0a0a0',
        '55555555-5555-5555-5555-555555555555',
        '72727272-7272-7272-7272-727272727272',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        'e1b1b1b1-e1b1-e1b1-e1b1-e1b1b1b1b1b1',
        '55555555-5555-5555-5555-555555555555',
        '73737373-7373-7373-7373-737373737373',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        'e2c2c2c2-e2c2-e2c2-e2c2-e2c2c2c2c2c2',
        '55555555-5555-5555-5555-555555555555',
        '74747474-7474-7474-7474-747474747474',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        'e3d3d3d3-e3d3-e3d3-e3d3-e3d3d3d3d3d3',
        '55555555-5555-5555-5555-555555555555',
        '75757575-7575-7575-7575-757575757575',
        '60606060-6060-6060-6060-606060606060',
        '22222222-2222-2222-2222-222222222222'
    );

-- Grade horaria 3A (complemento — Ciencias, Historia, Geografia, Arte, Ed. Fisica)
INSERT INTO horario_aula (id, turma_id, dia_semana, hora_inicio, hora_fim, disciplina_id, professor_id)
VALUES
    (
        'a1010101-a101-a101-a101-a10101010101',
        '55555555-5555-5555-5555-555555555555',
        2,
        '08:00',
        '09:30',
        '72727272-7272-7272-7272-727272727272',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'a2020202-a202-a202-a202-a20202020202',
        '55555555-5555-5555-5555-555555555555',
        2,
        '09:30',
        '11:00',
        '71717171-7171-7171-7171-717171717171',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'a3030303-a303-a303-a303-a30303030303',
        '55555555-5555-5555-5555-555555555555',
        4,
        '08:00',
        '09:30',
        '73737373-7373-7373-7373-737373737373',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'a4040404-a404-a404-a404-a40404040404',
        '55555555-5555-5555-5555-555555555555',
        4,
        '09:30',
        '11:00',
        '74747474-7474-7474-7474-747474747474',
        '60606060-6060-6060-6060-606060606060'
    ),
    (
        'a5050505-a505-a505-a505-a50505050505',
        '55555555-5555-5555-5555-555555555555',
        5,
        '09:30',
        '11:00',
        '75757575-7575-7575-7575-757575757575',
        '60606060-6060-6060-6060-606060606060'
    );

-- Segunda aluna da familia Oliveira (coordenacao — aluno em risco)
INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
    '18181818-1818-1818-1818-181818181818',
    'Ana Beatriz Oliveira',
    NULL,
    NULL,
    NULL,
    '2018-09-22'
);

INSERT INTO aluno (id, pessoa_id, matricula, turma_id, status)
VALUES (
    '19191919-1919-1919-1919-191919191919',
    '18181818-1818-1818-1818-181818181818',
    'MAT-2026-002',
    '55555555-5555-5555-5555-555555555555',
    'ATIVO'
);

INSERT INTO aluno_responsavel (aluno_id, responsavel_id)
VALUES ('19191919-1919-1919-1919-191919191919', '00000000-0000-0000-0000-000000000001');

INSERT INTO contrato (id, aluno_id, plano_id, ano_letivo_id, data_inicio, status)
VALUES (
    '21212121-2121-2121-2121-212121212121',
    '19191919-1919-1919-1919-191919191919',
    '10101010-1010-1010-1010-101010101010',
    '22222222-2222-2222-2222-222222222222',
    '2026-02-01',
    'ATIVO'
);

-- Notas Ana Beatriz — desempenho fraco em Matematica (analise coordenacao)
INSERT INTO nota (id, aluno_id, turma_disciplina_professor_id, periodo_id, valor, tipo, observacao)
VALUES
    (
        'f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1',
        '19191919-1919-1919-1919-191919191919',
        '90909090-9090-9090-9090-909090909090',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        4.50,
        'PROVA',
        'Dificuldade em operacoes basicas'
    ),
    (
        'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2',
        '19191919-1919-1919-1919-191919191919',
        'a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0',
        'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1',
        5.00,
        'PROVA',
        NULL
    );

INSERT INTO presenca (id, aluno_id, turma_disciplina_professor_id, data_aula, presente, justificativa)
VALUES
    (
        'f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3',
        '19191919-1919-1919-1919-191919191919',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-03',
        FALSE,
        NULL
    ),
    (
        'f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4',
        '19191919-1919-1919-1919-191919191919',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-10',
        FALSE,
        NULL
    ),
    (
        'f5f5f5f5-f5f5-f5f5-f5f5-f5f5f5f5f5f5',
        '19191919-1919-1919-1919-191919191919',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-17',
        TRUE,
        NULL
    ),
    (
        'f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6',
        '19191919-1919-1919-1919-191919191919',
        '90909090-9090-9090-9090-909090909090',
        '2026-03-24',
        FALSE,
        'Falta nao justificada'
    );

-- Financeiro demo: historico + pendente atual + inadimplencia
UPDATE cobranca
SET status = 'VENCIDO'
WHERE id = '30303030-3030-3030-3030-303030303030';

UPDATE cobranca
SET
    competencia = (date_trunc('month', CURRENT_DATE) - INTERVAL '1 month')::date,
    vencimento = ((date_trunc('month', CURRENT_DATE) - INTERVAL '1 month')::date + 9),
    pago_em = (date_trunc('month', CURRENT_DATE) - INTERVAL '1 month')::date + TIME '14:30'
WHERE id = '40404040-4040-4040-4040-404040404040';

INSERT INTO cobranca (id, contrato_id, competencia, valor, vencimento, status, pix_txid, pix_qrcode, pago_em)
SELECT
    '50505050-5050-5050-5050-505050505050',
    '20202020-2020-2020-2020-202020202020',
    date_trunc('month', CURRENT_DATE)::date,
    850.00,
    (date_trunc('month', CURRENT_DATE)::date + 9),
    'PENDENTE',
    NULL,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM cobranca c
    WHERE c.contrato_id = '20202020-2020-2020-2020-202020202020'
      AND c.competencia = date_trunc('month', CURRENT_DATE)::date
      AND c.status IN ('PENDENTE', 'VENCIDO')
);

INSERT INTO cobranca (id, contrato_id, competencia, valor, vencimento, status, pix_txid, pix_qrcode, pago_em)
SELECT
    '51515151-5151-5151-5151-515151515151',
    '21212121-2121-2121-2121-212121212121',
    date_trunc('month', CURRENT_DATE)::date,
    850.00,
    (date_trunc('month', CURRENT_DATE)::date + 9),
    'PENDENTE',
    NULL,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM cobranca c
    WHERE c.contrato_id = '21212121-2121-2121-2121-212121212121'
      AND c.competencia = date_trunc('month', CURRENT_DATE)::date
);

-- Comunicados com identidade CEM
UPDATE comunicado
SET
    titulo = 'Reuniao de pais — 3º ano A (CEM Monnerat)',
    conteudo = 'Prezados responsaveis da turma 3A: reuniao pedagógica dia 15/06, as 19h, no auditório do Centro Educacional Monnerat (Nova Friburgo).'
WHERE id = '33000000-0033-0033-0033-000000000031';

UPDATE comunicado
SET
    titulo = 'Calendario letivo — feriado em Nova Friburgo',
    conteudo = 'Informamos que nao ha expediente escolar na proxima segunda-feira, conforme calendario municipal de Nova Friburgo.'
WHERE id = '33000000-0033-0033-0033-000000000032';

UPDATE evento_agenda
SET titulo = 'Prova de Matematica — 3º A (CEM)'
WHERE id = '55000000-0055-0055-0055-000000000051';

UPDATE evento_agenda
SET
    titulo = 'Reuniao geral de pais — CEM Monnerat',
    descricao = 'Encontro com responsaveis no auditorio da unidade Centro.'
WHERE id = '55000000-0055-0055-0055-000000000052';
