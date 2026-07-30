ALTER TABLE escola
    ADD COLUMN nota_minima_aprovacao NUMERIC(4, 2) NOT NULL DEFAULT 6.00;

ALTER TABLE escola
    ADD COLUMN frequencia_minima NUMERIC(5, 2) NOT NULL DEFAULT 75.00;
