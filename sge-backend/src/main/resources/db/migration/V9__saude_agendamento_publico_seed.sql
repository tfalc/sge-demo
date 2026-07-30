-- Agendamento visivel aos pais (sem sigilo clinico)
INSERT INTO agendamento_saude (id, aluno_id, profissional_id, data_hora, status, observacoes, privado)
VALUES (
        'aa000000-00aa-00aa-00aa-0000000000aa',
        'ffffffff-ffff-ffff-ffff-ffffffffffff',
        '66000000-0066-0066-0066-000000000063',
        (CURRENT_DATE + INTERVAL '7 day')::date + TIME '10:00',
        'AGENDADO',
        'Conversa de acolhimento com a familia — horario confirmado.',
        FALSE
    );
