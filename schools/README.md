# Pacotes de escola (SGE)

Cada escola atendida pelo SGE pode ter um **pacote** nesta pasta e uma **branch** `school/<slug>`.

## Pacote ativo (distribuicao / demo)

| Slug | Uso |
|------|-----|
| `escola-demo` | Build portable, desenvolvimento e apresentacoes genericas |

## Dados de escola real (privado)

Pacotes com dados identificaveis ficam em `schools/_private/`. Ver [`_private/CEM_RESTAURACAO.md`](_private/CEM_RESTAURACAO.md) para restaurar o CEM Monnerat.

## Estrutura de um pacote

```
schools/<slug>/
  README.md
  NORMATIVAS.md
  escola.yaml
  curriculo/
```

O backend carrega via `app.school.package-id` e copia YAML no JAR (`pom.xml`).

## Adicionar nova escola

1. Copiar `escola-demo/` como modelo.
2. Criar branch `school/<novo-slug>`.
3. Definir `SCHOOL_PACKAGE_ID` ou `app.school.package-id`.
4. Ajustar seed/migration com dados da escola.
