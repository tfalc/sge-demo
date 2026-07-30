package br.com.sge.modules.admin.dto;

import java.util.List;
import java.util.Map;

/** Body do PUT: perfil -> lista de areas habilitadas. */
public record AtualizarAcessosMenuRequest(Map<String, List<String>> acessos) {}
