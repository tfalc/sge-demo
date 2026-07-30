# Escola Modelo Demo

Pacote generico de demonstracao do SGE — sem dados reais de escola.

- **Slug:** `escola-demo`
- **Uso:** build portable, desenvolvimento e apresentacoes genericas

## Normativa aplicada

Ver [NORMATIVAS.md](./NORMATIVAS.md). As matrizes em `curriculo/` seguem referencia BNCC + exemplo estadual (RJ).

## Configuracao runtime

```yaml
app:
  school:
    package-id: escola-demo
```

Para restaurar dados de uma escola real (ex.: CEM), consulte `schools/_private/CEM_RESTAURACAO.md`.
