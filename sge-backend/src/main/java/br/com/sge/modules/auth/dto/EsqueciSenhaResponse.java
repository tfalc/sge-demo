package br.com.sge.modules.auth.dto;

public record EsqueciSenhaResponse(String email, String mensagem, boolean simulado) {}
