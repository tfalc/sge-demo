package br.com.sge.modules.relatorios.service;

import br.com.sge.modules.academico.service.AcademicoService;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.financeiro.service.FinanceiroService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {

    private static final double NOTA_RISCO = 6.0;
    private static final double FREQ_RISCO = 75.0;

    private final AcademicoService academicoService;
    private final FinanceiroService financeiroService;
    private final AlunoRepository alunoRepository;
    private final AnaliseInteligenteService analiseInteligenteService;

    public RelatorioService(
            AcademicoService academicoService,
            FinanceiroService financeiroService,
            AlunoRepository alunoRepository,
            AnaliseInteligenteService analiseInteligenteService) {
        this.academicoService = academicoService;
        this.financeiroService = financeiroService;
        this.alunoRepository = alunoRepository;
        this.analiseInteligenteService = analiseInteligenteService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> desempenhoTurma(UUID turmaId) {
        List<Aluno> alunos = alunoRepository.findAtivosByTurmaId(turmaId);
        List<Map<String, Object>> itens = new ArrayList<>();
        double somaMedias = 0;
        int comNota = 0;

        for (Aluno aluno : alunos) {
            Map<String, Object> boletim = academicoService.obterBoletim(aluno.getId());
            double media = extrairUltimaMediaGeral(boletim);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("alunoId", aluno.getId());
            item.put("alunoNome", aluno.getPessoa().getNome());
            item.put("mediaGeral", media);
            item.put("emRisco", media > 0 && media < NOTA_RISCO);
            itens.add(item);
            if (media > 0) {
                somaMedias += media;
                comNota++;
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("turmaId", turmaId);
        out.put("mediaTurma", comNota == 0 ? 0.0 : Math.round((somaMedias / comNota) * 100.0) / 100.0);
        out.put("alunos", itens);
        out.put("alunosEmRisco", itens.stream().filter(i -> Boolean.TRUE.equals(i.get("emRisco"))).count());
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> frequenciaTurma(UUID turmaId) {
        List<Aluno> alunos = alunoRepository.findAtivosByTurmaId(turmaId);
        List<Map<String, Object>> itens = new ArrayList<>();

        for (Aluno aluno : alunos) {
            Map<String, Object> freq = academicoService.obterFrequencia(aluno.getId());
            double pct = ((Number) freq.get("percentualGeral")).doubleValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("alunoId", aluno.getId());
            item.put("alunoNome", aluno.getPessoa().getNome());
            item.put("percentual", pct);
            item.put("emRisco", pct > 0 && pct < FREQ_RISCO);
            itens.add(item);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("turmaId", turmaId);
        out.put("alunos", itens);
        out.put("alunosEmRisco", itens.stream().filter(i -> Boolean.TRUE.equals(i.get("emRisco"))).count());
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> analiseAluno(UUID alunoId) {
        Aluno aluno = alunoRepository
                .findDetalhadoById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));

        Map<String, Object> boletim = academicoService.obterBoletim(alunoId);
        Map<String, Object> freq = academicoService.obterFrequencia(alunoId);
        double media = extrairUltimaMediaGeral(boletim);
        double percentual = ((Number) freq.get("percentualGeral")).doubleValue();
        double notaMinima = ((Number) boletim.get("notaMinimaAprovacao")).doubleValue();
        double freqMinima = ((Number) freq.get("frequenciaMinima")).doubleValue();

        List<Map<String, Object>> disciplinasNotas =
                AnaliseInteligenteService.extrairDisciplinasUltimoPeriodo(boletim);
        List<Map<String, Object>> disciplinasFrequencia =
                AnaliseInteligenteService.extrairDisciplinasFrequencia(freq);

        Map<String, Object> inteligente =
                analiseInteligenteService.analisar(
                        aluno.getPessoa().getNome(),
                        aluno.getTurma() != null ? aluno.getTurma().getNome() : null,
                        media,
                        percentual,
                        notaMinima,
                        freqMinima,
                        disciplinasNotas,
                        disciplinasFrequencia);

        List<String> alertas = new ArrayList<>();
        if (media > 0 && media < notaMinima) {
            alertas.add("Media abaixo do minimo (" + notaMinima + ")");
        }
        if (percentual > 0 && percentual < freqMinima) {
            alertas.add("Frequencia abaixo do minimo (" + freqMinima + "%)");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alunoId", alunoId);
        out.put("alunoNome", aluno.getPessoa().getNome());
        out.put("turmaNome", aluno.getTurma() != null ? aluno.getTurma().getNome() : null);
        out.put("mediaGeral", media);
        out.put("percentualFrequencia", percentual);
        out.put("emRisco", inteligente.get("emRisco"));
        out.put("alertas", alertas);
        out.putAll(inteligente);
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> inadimplenciaEscola() {
        Map<String, Object> mensal = financeiroService.relatorioMensal();
        List<Map<String, Object>> inadimplentes = financeiroService.listarInadimplentes();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mes", mensal.get("mes"));
        out.put("totalVencido", mensal.get("totalVencido"));
        out.put("totalPendente", mensal.get("totalPendente"));
        out.put("totalRecebido", mensal.get("totalRecebido"));
        out.put("quantidadeInadimplentes", inadimplentes.size());
        out.put("inadimplentes", inadimplentes);
        return out;
    }

    private static double extrairUltimaMediaGeral(Map<String, Object> boletim) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> periodos = (List<Map<String, Object>>) boletim.get("periodos");
        if (periodos == null || periodos.isEmpty()) {
            return 0.0;
        }
        Map<String, Object> ultimo = periodos.get(periodos.size() - 1);
        return ((Number) ultimo.get("mediaGeral")).doubleValue();
    }
}
