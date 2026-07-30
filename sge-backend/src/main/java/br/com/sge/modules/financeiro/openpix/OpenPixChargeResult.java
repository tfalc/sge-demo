package br.com.sge.modules.financeiro.openpix;

public record OpenPixChargeResult(String transactionId, String brCode, String qrCodeImageUrl, String status) {}
