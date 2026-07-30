-- Neutraliza dados especificos do CEM Monnerat (V13/V17) para ambiente generico de demo.
-- Restauracao: ver schools/_private/CEM_RESTAURACAO.md

UPDATE escola
SET
    nome = 'Escola Modelo Demo',
    slug = 'escola-demo',
    municipio = 'Cidade Exemplo',
    uf = 'SP',
    package_id = 'escola-demo',
    cnpj = '12.345.678/0001-90'
WHERE package_id = 'cem-monnerat'
   OR slug = 'cem-monnerat'
   OR nome LIKE '%Monnerat%'
   OR nome LIKE '%CEM%'
   OR cnpj = '28.456.789/0001-42'
   OR id = '11111111-1111-1111-1111-111111111111';

UPDATE plano_pagamento
SET nome = 'Mensalidade — Ensino Fundamental'
WHERE nome LIKE '%CEM%';

UPDATE pessoa SET nome = 'Maria Responsavel'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccccc';

UPDATE pessoa SET nome = 'Roberto Professor'
WHERE id = '11000000-0011-0011-0011-000000000011';

UPDATE pessoa SET nome = 'Ana Silva'
WHERE id = '18181818-1818-1818-1818-181818181818';

UPDATE comunicado
SET
    titulo = 'Reuniao de pais — turma 3A',
    conteudo = 'Convidamos os responsaveis da turma 3A para reuniao dia 15/06 as 19h no auditorio.'
WHERE id = '33000000-0033-0033-0033-000000000031'
   OR titulo LIKE '%CEM%'
   OR conteudo LIKE '%Monnerat%';

UPDATE comunicado
SET
    titulo = 'Feriado municipal',
    conteudo = 'Nao ha aula na proxima segunda-feira (feriado municipal).'
WHERE id = '33000000-0033-0033-0033-000000000032'
   OR titulo LIKE '%Nova Friburgo%';

UPDATE evento_agenda
SET titulo = 'Prova de Matematica — 3A'
WHERE id = '55000000-0055-0055-0055-000000000051'
   OR titulo LIKE '%CEM%';

UPDATE evento_agenda
SET
    titulo = 'Reuniao de pais',
    descricao = 'Encontro geral com responsaveis no auditorio.'
WHERE id = '55000000-0055-0055-0055-000000000052'
   OR titulo LIKE '%CEM Monnerat%';
