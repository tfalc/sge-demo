-- Hibernate valida uf como VARCHAR(2), nao CHAR(2)
ALTER TABLE escola
    ALTER COLUMN uf TYPE VARCHAR(2) USING uf::VARCHAR;
