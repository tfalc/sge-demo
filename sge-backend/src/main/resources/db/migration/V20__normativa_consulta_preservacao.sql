-- Rastreia consulta normativa e matrizes importadas do pacote (sem sobrescrever existentes).

ALTER TABLE escola
    ADD COLUMN normativa_consultada_em TIMESTAMPTZ;

ALTER TABLE matriz_curricular
    ADD COLUMN sincronizada_normativa_em TIMESTAMPTZ;
