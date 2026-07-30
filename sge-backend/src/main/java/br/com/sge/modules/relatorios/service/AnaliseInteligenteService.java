package br.com.sge.modules.relatorios.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Motor pedagogico embutido (regras + heuristica), sem API externa.
 * Inspirado no padrao SmartAnalysisService da CalculadoraFII.
 */
@Service
public class AnaliseInteligenteService {

    public Map<String, Object> analisar(
            String alunoNome,
            String turmaNome,
            double mediaGeral,
            double percentualFrequencia,
            double notaMinima,
            double frequenciaMinima,
            List<Map<String, Object>> disciplinasNotas,
            List<Map<String, Object>> disciplinasFrequencia) {

        List<String> tags = new ArrayList<>();
        List<String> pontosFortes = new ArrayList<>();
        List<String> pontosAtencao = new ArrayList<>();
        List<String> sugestoesCoordenacao = new ArrayList<>();
        List<String> sugestoesFamilia = new ArrayList<>();

        int score = 50;

        boolean riscoNota = mediaGeral > 0 && mediaGeral < notaMinima;
        boolean riscoFreq = percentualFrequencia > 0 && percentualFrequencia < frequenciaMinima;
        boolean semDados = mediaGeral <= 0 && percentualFrequencia <= 0;

        if (semDados) {
            tags.add("Dados insuficientes");
            pontosAtencao.add("Ainda nao ha notas ou frequencia registradas para analise completa.");
            sugestoesCoordenacao.add("Aguardar lancamentos do professor ou conferir vinculos da turma.");
        }

        if (mediaGeral >= notaMinima + 1.5) {
            score += 20;
            pontosFortes.add("Media geral acima da expectativa da escola.");
            tags.add("Desempenho solido");
        } else if (mediaGeral >= notaMinima && mediaGeral > 0) {
            score += 10;
            pontosFortes.add("Media geral dentro do minimo para aprovacao.");
        } else if (riscoNota) {
            score -= 25;
            pontosAtencao.add(
                    String.format("Media geral %.2f abaixo do minimo %.2f.", mediaGeral, notaMinima));
            tags.add("Risco academico");
            sugestoesCoordenacao.add("Convocar reuniao pedagogica e mapear reforco por disciplina.");
            sugestoesFamilia.add("Reforcar rotina de estudos em casa e acompanhar tarefas diarias.");
        }

        if (percentualFrequencia >= frequenciaMinima + 10 && percentualFrequencia > 0) {
            score += 15;
            pontosFortes.add("Frequencia escolar regular.");
            tags.add("Presenca regular");
        } else if (percentualFrequencia >= frequenciaMinima && percentualFrequencia > 0) {
            score += 5;
        } else if (riscoFreq) {
            score -= 20;
            pontosAtencao.add(
                    String.format(
                            "Frequencia %.1f%% abaixo do minimo %.1f%%.",
                            percentualFrequencia, frequenciaMinima));
            tags.add("Risco de evasao");
            sugestoesCoordenacao.add("Verificar faltas repetidas e acionar secretaria para contato familiar.");
            sugestoesFamilia.add("Priorizar comparecimento — frequencia minima e requisito legal (LDB).");
        }

        List<Map<String, Object>> fracasNotas = disciplinasAbaixoNota(disciplinasNotas, notaMinima);
        for (Map<String, Object> d : fracasNotas) {
            score -= 5;
            String nome = (String) d.get("disciplinaNome");
            double media = ((Number) d.get("media")).doubleValue();
            pontosAtencao.add(String.format("%s com media %.2f.", nome, media));
            sugestoesCoordenacao.add("Reforco em " + nome + " com professor responsavel.");
        }
        if (!fracasNotas.isEmpty()) {
            tags.add("Disciplinas criticas");
        }

        List<Map<String, Object>> fracasFreq = disciplinasAbaixoFrequencia(disciplinasFrequencia, frequenciaMinima);
        for (Map<String, Object> d : fracasFreq) {
            String nome = (String) d.get("disciplinaNome");
            double pct = ((Number) d.get("percentual")).doubleValue();
            if (pct > 0) {
                pontosAtencao.add(String.format("Frequencia baixa em %s (%.1f%%).", nome, pct));
            }
        }

        List<Map<String, Object>> fortesNotas = disciplinasAcima(disciplinasNotas, notaMinima + 1.0);
        for (Map<String, Object> d : fortesNotas.stream().limit(2).toList()) {
            pontosFortes.add(
                    String.format(
                            "Destaque em %s (media %.2f).",
                            d.get("disciplinaNome"), ((Number) d.get("media")).doubleValue()));
        }

        score = Math.max(0, Math.min(100, score));
        String situacao = score >= 75 ? "OTIMO" : score >= 50 ? "ATENCAO" : "CRITICO";
        boolean emRisco = riscoNota || riscoFreq || score < 50;

        if (sugestoesCoordenacao.isEmpty() && !semDados) {
            sugestoesCoordenacao.add("Manter acompanhamento bimestral e registrar evolucao no diario.");
        }
        if (sugestoesFamilia.isEmpty() && !semDados) {
            sugestoesFamilia.add("Incentivar leitura e participacao nas atividades escolares.");
        }

        String relatorio =
                montarRelatorio(
                        alunoNome,
                        turmaNome,
                        mediaGeral,
                        percentualFrequencia,
                        score,
                        situacao,
                        tags,
                        pontosFortes,
                        pontosAtencao,
                        sugestoesCoordenacao,
                        sugestoesFamilia,
                        fracasNotas);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("score", score);
        out.put("situacao", situacao);
        out.put("tags", tags);
        out.put("pontosFortes", pontosFortes);
        out.put("pontosAtencao", pontosAtencao);
        out.put("disciplinasNotas", disciplinasNotas);
        out.put("disciplinasFrequencia", disciplinasFrequencia);
        out.put("disciplinasCriticas", fracasNotas);
        out.put("sugestoesCoordenacao", sugestoesCoordenacao);
        out.put("sugestoesFamilia", sugestoesFamilia);
        out.put("relatorio", relatorio);
        out.put("modo", "EMBUTIDA");
        out.put("emRisco", emRisco);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extrairDisciplinasUltimoPeriodo(Map<String, Object> boletim) {
        List<Map<String, Object>> periodos = (List<Map<String, Object>>) boletim.get("periodos");
        if (periodos == null || periodos.isEmpty()) {
            return List.of();
        }
        Map<String, Object> ultimo = periodos.get(periodos.size() - 1);
        List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) ultimo.get("disciplinas");
        return disciplinas != null ? disciplinas : List.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extrairDisciplinasFrequencia(Map<String, Object> frequencia) {
        List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) frequencia.get("porDisciplina");
        if (disciplinas == null) {
            disciplinas = (List<Map<String, Object>>) frequencia.get("disciplinas");
        }
        return disciplinas != null ? disciplinas : List.of();
    }

    private static List<Map<String, Object>> disciplinasAbaixoNota(
            List<Map<String, Object>> disciplinas, double notaMinima) {
        return disciplinas.stream()
                .filter(
                        d -> {
                            double m = ((Number) d.getOrDefault("media", 0)).doubleValue();
                            return m > 0 && m < notaMinima;
                        })
                .sorted(
                        Comparator.comparingDouble(
                                d -> ((Number) d.get("media")).doubleValue()))
                .toList();
    }

    private static List<Map<String, Object>> disciplinasAcima(
            List<Map<String, Object>> disciplinas, double limite) {
        return disciplinas.stream()
                .filter(
                        d -> {
                            double m = ((Number) d.getOrDefault("media", 0)).doubleValue();
                            return m >= limite;
                        })
                .sorted(
                        Comparator.comparingDouble(
                                        (Map<String, Object> d) -> ((Number) d.get("media")).doubleValue())
                                .reversed())
                .toList();
    }

    private static List<Map<String, Object>> disciplinasAbaixoFrequencia(
            List<Map<String, Object>> disciplinas, double freqMinima) {
        return disciplinas.stream()
                .filter(
                        d -> {
                            double p = ((Number) d.getOrDefault("percentual", 0)).doubleValue();
                            return p > 0 && p < freqMinima;
                        })
                .toList();
    }

    private static String montarRelatorio(
            String nome,
            String turma,
            double media,
            double freq,
            int score,
            String situacao,
            List<String> tags,
            List<String> fortes,
            List<String> atencao,
            List<String> sugCoord,
            List<String> sugFamilia,
            List<Map<String, Object>> fracasNotas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analise inteligente — ").append(nome);
        if (turma != null && !turma.isBlank()) {
            sb.append(" (turma ").append(turma).append(")");
        }
        sb.append("\n\n");
        sb.append("Indice pedagogico: ").append(score).append("/100 — ").append(rotuloSituacao(situacao)).append("\n");
        if (!tags.isEmpty()) {
            sb.append("Tags: ").append(String.join(", ", tags)).append("\n");
        }
        sb.append("\nResumo quantitativo\n");
        sb.append("• Media geral recente: ").append(media > 0 ? String.format("%.2f", media) : "sem lancamento").append("\n");
        sb.append("• Frequencia: ").append(freq > 0 ? String.format("%.1f%%", freq) : "sem registro").append("\n");

        if (!fortes.isEmpty()) {
            sb.append("\nPontos fortes\n");
            for (String f : fortes) {
                sb.append("• ").append(f).append("\n");
            }
        }
        if (!atencao.isEmpty()) {
            sb.append("\nPontos de atencao\n");
            for (String a : atencao) {
                sb.append("• ").append(a).append("\n");
            }
        }
        if (!fracasNotas.isEmpty()) {
            sb.append("\nDisciplinas com media abaixo do minimo\n");
            for (Map<String, Object> d : fracasNotas) {
                sb.append("• ")
                        .append(d.get("disciplinaNome"))
                        .append(": ")
                        .append(String.format("%.2f", ((Number) d.get("media")).doubleValue()))
                        .append("\n");
            }
        }
        sb.append("\nSugestoes para coordenacao\n");
        for (String s : sugCoord) {
            sb.append("• ").append(s).append("\n");
        }
        sb.append("\nSugestoes para a familia\n");
        for (String s : sugFamilia) {
            sb.append("• ").append(s).append("\n");
        }
        sb.append("\n— Analise gerada pelo motor embutido do SGE (sem nuvem).");
        return sb.toString();
    }

    private static String rotuloSituacao(String situacao) {
        return switch (situacao) {
            case "OTIMO" -> "Dentro do esperado";
            case "CRITICO" -> "Atencao urgente";
            default -> "Requer acompanhamento";
        };
    }
}
