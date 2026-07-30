-- Usuário operacional da secretaria (separado do ADMIN dono da escola)
-- Senha: admin123 (mesmo hash BCrypt dos demais seeds)

INSERT INTO pessoa (id, nome, cpf, email, telefone, data_nascimento)
VALUES (
        'a1000000-a100-a100-a100-000000000001',
        'Secretaria Escolar',
        NULL,
        'secretaria@sge.com',
        NULL,
        NULL
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuario (id, pessoa_id, email, senha_hash, perfil, ativo)
VALUES (
        'a1000000-a100-a100-a100-000000000002',
        'a1000000-a100-a100-a100-000000000001',
        'secretaria@sge.com',
        '$2b$12$0w3OI.uUozjlOi/EagABP.B8pkU2JSxyXCSzPpwMDUaYqH4NqXpEK',
        'SECRETARIA',
        TRUE
    )
ON CONFLICT (id) DO NOTHING;

COMMENT ON TABLE usuario IS 'Perfis demo: admin@sge.com=ADMIN (donos); secretaria@sge.com=SECRETARIA (operacional)';
