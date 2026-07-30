CREATE TABLE rematricula_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ano_letivo_id UUID REFERENCES ano_letivo (id),
    habilitada BOOLEAN NOT NULL DEFAULT FALSE,
    titulo VARCHAR(200) NOT NULL DEFAULT 'Rematricula',
    pdf_modelo_nome VARCHAR(255),
    pdf_modelo_conteudo BYTEA,
    formulario_json JSONB NOT NULL DEFAULT '{"secoes":[]}'::jsonb,
    sugestoes_extracao_json JSONB,
    publicado_em TIMESTAMPTZ,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rematricula_submissao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES aluno (id),
    responsavel_id UUID NOT NULL REFERENCES responsavel (id),
    ano_letivo_id UUID NOT NULL REFERENCES ano_letivo (id),
    status VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    respostas_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    pdf_preenchido BYTEA,
    enviado_em TIMESTAMPTZ,
    validado_secretaria_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (aluno_id, ano_letivo_id)
);

CREATE INDEX idx_rematricula_submissao_status ON rematricula_submissao (status);

INSERT INTO rematricula_config (id, ano_letivo_id, habilitada, titulo, formulario_json)
VALUES (
    'a0010000-0000-0000-0000-000000000001',
    '22222222-2222-2222-2222-222222222222',
    FALSE,
    'Rematricula 2026',
    '{
      "secoes": [
        {
          "id": "sec-interesse",
          "titulo": "Interesse na rematricula",
          "ordem": 1,
          "campos": [
            {
              "id": "campo-interesse",
              "rotulo": "Desejo rematricular meu filho(a) para o proximo ano letivo",
              "tipo": "BOOLEAN",
              "obrigatorio": true,
              "ordem": 1,
              "opcoes": null
            }
          ]
        },
        {
          "id": "sec-dados",
          "titulo": "Confirmacao de dados",
          "ordem": 2,
          "campos": [
            {
              "id": "campo-endereco",
              "rotulo": "Endereco residencial atualizado",
              "tipo": "TEXTO",
              "obrigatorio": true,
              "ordem": 1,
              "opcoes": null
            },
            {
              "id": "campo-telefone",
              "rotulo": "Telefone para contato",
              "tipo": "TEXTO",
              "obrigatorio": true,
              "ordem": 2,
              "opcoes": null
            },
            {
              "id": "campo-email",
              "rotulo": "E-mail do responsavel",
              "tipo": "TEXTO",
              "obrigatorio": false,
              "ordem": 3,
              "opcoes": null
            }
          ]
        },
        {
          "id": "sec-autorizacao",
          "titulo": "Autorizacoes",
          "ordem": 3,
          "campos": [
            {
              "id": "campo-imagem",
              "rotulo": "Autorizo uso de imagem do aluno em materiais institucionais",
              "tipo": "SELECAO",
              "obrigatorio": true,
              "ordem": 1,
              "opcoes": ["Sim", "Nao"]
            },
            {
              "id": "campo-observacoes",
              "rotulo": "Observacoes adicionais",
              "tipo": "TEXTO_LONGO",
              "obrigatorio": false,
              "ordem": 2,
              "opcoes": null
            }
          ]
        }
      ]
    }'::jsonb
);
