-- Seed demo: garante pelo menos uma cobrança PAGA no mês corrente (KPI "Recebido no mês")
UPDATE cobranca
SET status = 'PAGO',
    pago_em = date_trunc('month', CURRENT_DATE) + INTERVAL '5 days'
WHERE id = (
    SELECT c.id
    FROM cobranca c
    ORDER BY
        CASE WHEN c.status = 'PAGO' AND c.pago_em >= date_trunc('month', CURRENT_DATE) THEN 0 ELSE 1 END,
        c.competencia DESC NULLS LAST
    LIMIT 1
);

-- Marca um álbum (se existir) exigindo consentimento LGPD — demo do filtro
UPDATE galeria_album
SET exigir_consentimento_imagem = TRUE
WHERE id IN (SELECT id FROM galeria_album ORDER BY publicado_em DESC LIMIT 1);
