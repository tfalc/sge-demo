# PIX com OpenPix — configuracao generica

O SGE aceita **dois modos** de pagamento PIX:

| Modo | Quando | O que acontece |
|------|--------|----------------|
| **SIMULACAO** | `OPENPIX_APP_ID` vazio (padrao em dev) | QR ficticio + botao "Simular pagamento" |
| **OPENPIX** | `OPENPIX_APP_ID` configurado | Cobranca real na API OpenPix |

A chave PIX da escola e cadastrada no painel OpenPix, nao no codigo.

## Variaveis de ambiente

```bash
OPENPIX_APP_ID=seu_app_id_aqui
OPENPIX_WEBHOOK_SECRET=segredo_opcional
OPENPIX_BASE_URL=https://api.openpix.com.br
```

Webhook: `POST https://SEU_DOMINIO/api/financeiro/webhook/pix`
