-- Normalizacao idempotente para portable (H2) e bancos que ja tinham dados CEM.
-- Restauracao CEM: schools/_private/CEM_RESTAURACAO.md

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
   OR cnpj = '28.456.789/0001-42';

UPDATE plano_pagamento
SET nome = 'Mensalidade — Ensino Fundamental'
WHERE nome LIKE '%CEM%';

UPDATE pessoa SET nome = 'Maria Responsavel'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccccc'
  AND nome LIKE '%Oliveira%';

UPDATE pessoa SET nome = 'Roberto Professor'
WHERE id = '11000000-0011-0011-0011-000000000011'
  AND nome LIKE '%Mendes%';

UPDATE pessoa SET nome = 'Ana Silva'
WHERE id = '18181818-1818-1818-1818-181818181818'
  AND nome LIKE '%Beatriz%';

UPDATE comunicado
SET
    titulo = 'Reuniao de pais — turma 3A',
    conteudo = 'Convidamos os responsaveis da turma 3A para reuniao dia 15/06 as 19h no auditorio.'
WHERE titulo LIKE '%CEM%' OR conteudo LIKE '%Monnerat%' OR conteudo LIKE '%Nova Friburgo%';

UPDATE comunicado
SET
    titulo = 'Feriado municipal',
    conteudo = 'Nao ha aula na proxima segunda-feira (feriado municipal).'
WHERE titulo LIKE '%Nova Friburgo%';

UPDATE evento_agenda
SET titulo = 'Prova de Matematica — 3A'
WHERE titulo LIKE '%CEM%';

UPDATE evento_agenda
SET
    titulo = 'Reuniao de pais',
    descricao = 'Encontro geral com responsaveis no auditorio.'
WHERE titulo LIKE '%CEM%' OR titulo LIKE '%Monnerat%';
