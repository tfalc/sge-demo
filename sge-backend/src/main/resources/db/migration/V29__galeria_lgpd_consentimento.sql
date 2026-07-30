-- Consentimento de uso de imagem (LGPD) na galeria escolar
ALTER TABLE galeria_album
    ADD COLUMN IF NOT EXISTS exigir_consentimento_imagem BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS autoriza_uso_imagem BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN galeria_album.exigir_consentimento_imagem IS
    'Se true, album so deve exibir alunos/familias com autoriza_uso_imagem=true (filtro de publicacao)';
COMMENT ON COLUMN aluno.autoriza_uso_imagem IS
    'Autorizacao LGPD/familia para uso de imagem em galeria e materiais da escola';
