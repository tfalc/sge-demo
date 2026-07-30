package br.com.sge.modules.academico.service;

import br.com.sge.modules.academico.dto.AtualizarNotaRequest;
import br.com.sge.modules.academico.dto.LancamentoPresencaRequest;
import br.com.sge.modules.academico.dto.LancarNotaRequest;
import br.com.sge.modules.academico.dto.MatrizPresencaCelulaRequest;
import br.com.sge.modules.academico.dto.PresencaItemRequest;
import br.com.sge.modules.academico.dto.SalvarMatrizPresencaRequest;
import br.com.sge.modules.academico.entity.DiarioFrequenciaMeta;
import br.com.sge.modules.academico.entity.Nota;
import br.com.sge.modules.academico.entity.PeriodoAvaliacao;
import br.com.sge.modules.academico.entity.Presenca;
import br.com.sge.modules.academico.entity.TipoNota;
import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.academico.repository.DiarioFrequenciaMetaRepository;
import br.com.sge.modules.academico.repository.NotaRepository;
import br.com.sge.modules.academico.repository.PeriodoAvaliacaoRepository;
import br.com.sge.modules.academico.repository.PresencaRepository;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.Escola;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.EscolaRepository;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicoService {

    private static final double NOTA_MINIMA_PADRAO = 6.0;
    private static final double FREQUENCIA_MINIMA_PADRAO = 75.0;

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final EscolaRepository escolaRepository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final PeriodoAvaliacaoRepository periodoRepository;
    private final NotaRepository notaRepository;
    private final PresencaRepository presencaRepository;
    private final DiarioFrequenciaMetaRepository diarioFrequenciaMetaRepository;
    private final NotificacaoService notificacaoService;

    public AcademicoService(
            TurmaRepository turmaRepository,
            AlunoRepository alunoRepository,
            EscolaRepository escolaRepository,
            TurmaDisciplinaProfessorRepository tdpRepository,
            PeriodoAvaliacaoRepository periodoRepository,
            NotaRepository notaRepository,
            PresencaRepository presencaRepository,
            DiarioFrequenciaMetaRepository diarioFrequenciaMetaRepository,
            NotificacaoService notificacaoService) {
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.escolaRepository = escolaRepository;
        this.tdpRepository = tdpRepository;
        this.periodoRepository = periodoRepository;
        this.notaRepository = notaRepository;
        this.presencaRepository = presencaRepository;
        this.diarioFrequenciaMetaRepository = diarioFrequenciaMetaRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarTurmas(UUID professorId) {
        List<Turma> turmas =
                professorId != null ? turmaRepository.findByProfessorId(professorId) : turmaRepository.findAllDetalhadas();
        return turmas.stream().map(this::toTurmaMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAlunosDaTurma(UUID turmaId) {
        return alunoRepository.findAtivosByTurmaId(turmaId).stream()
                .map(this::toAlunoMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarVinculosDisciplina(UUID turmaId, UUID professorId) {
        return tdpRepository.findByTurmaId(turmaId, professorId).stream()
                .map(tdp -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", tdp.getId());
                    m.put("disciplinaId", tdp.getDisciplina().getId());
                    m.put("disciplinaNome", tdp.getDisciplina().getNome());
                    m.put("professorNome", tdp.getProfessor().getPessoa().getNome());
                    return m;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPeriodos() {
        return periodoRepository.findAllOrdered().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("nome", p.getNome());
                    m.put("dataInicio", p.getDataInicio() != null ? p.getDataInicio().toString() : null);
                    m.put("dataFim", p.getDataFim() != null ? p.getDataFim().toString() : null);
                    return m;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarDiarioNotas(UUID turmaDisciplinaProfessorId) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(turmaDisciplinaProfessorId)
                .orElseThrow(() -> new IllegalArgumentException("Vinculo turma/disciplina nao encontrado"));

        UUID turmaId = tdp.getTurma().getId();
        List<Map<String, Object>> periodos = listarPeriodos();
        PeriodoAvaliacao periodoComplemento = resolverPeriodoComplemento(periodoRepository.findAllOrdered());
        UUID periodoComplementoId = periodoComplemento != null ? periodoComplemento.getId() : null;

        List<Nota> notas = notaRepository.findByTurmaDisciplinaProfessorId(turmaDisciplinaProfessorId);
        List<Map<String, Object>> alunos = listarAlunosDaTurma(turmaId);

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Map<String, Object> alunoResumo : alunos) {
            UUID alunoId = (UUID) alunoResumo.get("id");
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("alunoId", alunoId);
            linha.put("nome", alunoResumo.get("nome"));
            linha.put("matricula", alunoResumo.get("matricula"));

            List<Map<String, Object>> celulasPeriodo = new ArrayList<>();
            for (Map<String, Object> periodo : periodos) {
                UUID periodoId = (UUID) periodo.get("id");
                celulasPeriodo.add(resolverCelulaPeriodo(alunoId, periodoId, notas));
            }
            linha.put("periodos", celulasPeriodo);

            Map<String, Object> complemento = new LinkedHashMap<>();
            complemento.put("periodoId", periodoComplementoId);
            complemento.put("periodoNome", periodoComplemento != null ? periodoComplemento.getNome() : null);
            if (periodoComplementoId != null) {
                Optional<Nota> compl = notas.stream()
                        .filter(n -> n.getAluno().getId().equals(alunoId)
                                && n.getPeriodo().getId().equals(periodoComplementoId)
                                && n.getTipo() == TipoNota.COMPLEMENTAR)
                        .findFirst();
                if (compl.isPresent()) {
                    complemento.put("notaId", compl.get().getId());
                    complemento.put("valor", compl.get().getValor().doubleValue());
                } else {
                    complemento.put("notaId", null);
                    complemento.put("valor", null);
                }
            }
            linha.put("complemento", complemento);
            linhas.add(linha);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("turmaDisciplinaProfessorId", turmaDisciplinaProfessorId);
        out.put("disciplinaNome", tdp.getDisciplina().getNome());
        out.put("periodos", periodos);
        out.put("periodoComplementoId", periodoComplementoId);
        out.put("alunos", linhas);
        return out;
    }

    private Map<String, Object> resolverCelulaPeriodo(UUID alunoId, UUID periodoId, List<Nota> notas) {
        List<Nota> doPeriodo = notas.stream()
                .filter(n -> n.getAluno().getId().equals(alunoId)
                        && n.getPeriodo().getId().equals(periodoId))
                .toList();

        Map<String, Object> celula = new LinkedHashMap<>();
        celula.put("periodoId", periodoId);

        Optional<Nota> finalNota = doPeriodo.stream()
                .filter(n -> n.getTipo() == TipoNota.FINAL)
                .findFirst();
        if (finalNota.isPresent()) {
            celula.put("notaId", finalNota.get().getId());
            celula.put("valor", finalNota.get().getValor().doubleValue());
            celula.put("origem", "FINAL");
            return celula;
        }

        List<Nota> legado = doPeriodo.stream()
                .filter(n -> n.getTipo() != TipoNota.COMPLEMENTAR)
                .toList();
        if (legado.size() == 1) {
            celula.put("notaId", legado.get(0).getId());
            celula.put("valor", legado.get(0).getValor().doubleValue());
            celula.put("origem", legado.get(0).getTipo().name());
            return celula;
        }
        if (!legado.isEmpty()) {
            double media = legado.stream()
                    .mapToDouble(n -> n.getValor().doubleValue())
                    .average()
                    .orElse(0);
            celula.put("notaId", null);
            celula.put("valor", round(media));
            celula.put("origem", "MEDIA");
            return celula;
        }

        celula.put("notaId", null);
        celula.put("valor", null);
        celula.put("origem", null);
        return celula;
    }

    private PeriodoAvaliacao resolverPeriodoComplemento(List<PeriodoAvaliacao> periodos) {
        if (periodos.isEmpty()) {
            return null;
        }
        LocalDate hoje = LocalDate.now();
        for (PeriodoAvaliacao p : periodos) {
            if (p.getDataInicio() != null
                    && p.getDataFim() != null
                    && !hoje.isBefore(p.getDataInicio())
                    && !hoje.isAfter(p.getDataFim())) {
                return p;
            }
        }
        return periodos.get(periodos.size() - 1);
    }

    @Transactional
    public Map<String, Object> lancarNota(LancarNotaRequest req) {
        Aluno aluno = alunoRepository
                .findById(req.alunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(req.turmaDisciplinaProfessorId())
                .orElseThrow(() -> new IllegalArgumentException("Vinculo turma/disciplina nao encontrado"));
        PeriodoAvaliacao periodo = periodoRepository
                .findById(req.periodoId())
                .orElseThrow(() -> new IllegalArgumentException("Periodo nao encontrado"));

        Nota nota = notaRepository
                .findByAlunoIdAndTurmaDisciplinaProfessorIdAndPeriodoIdAndTipo(
                        req.alunoId(), req.turmaDisciplinaProfessorId(), req.periodoId(), req.tipo())
                .orElseGet(Nota::new);

        nota.setAluno(aluno);
        nota.setTurmaDisciplinaProfessor(tdp);
        nota.setPeriodo(periodo);
        nota.setValor(req.valor());
        nota.setTipo(req.tipo());
        nota.setObservacao(req.observacao());
        if (nota.getLancadoEm() == null) {
            nota.setLancadoEm(Instant.now());
        }

        Nota saved = notaRepository.save(nota);
        String disciplina = tdp.getDisciplina().getNome();
        notificacaoService.notificarResponsaveisDoAluno(
                aluno.getId(),
                TipoNotificacao.NOTA_LANCADA,
                "Nova nota lancada",
                disciplina + " — nota " + req.valor() + " (" + periodo.getNome() + ").",
                "/pais/desempenho",
                saved.getId());
        return toNotaMap(saved);
    }

    @Transactional
    public Map<String, Object> atualizarNota(UUID notaId, AtualizarNotaRequest req) {
        Nota nota = notaRepository
                .findById(notaId)
                .orElseThrow(() -> new IllegalArgumentException("Nota nao encontrada"));
        nota.setValor(req.valor());
        nota.setObservacao(req.observacao());
        return toNotaMap(notaRepository.save(nota));
    }

    @Transactional
    public Map<String, Object> lancarPresencas(LancamentoPresencaRequest req) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(req.turmaDisciplinaProfessorId())
                .orElseThrow(() -> new IllegalArgumentException("Vinculo turma/disciplina nao encontrado"));

        int criadas = 0;
        int atualizadas = 0;
        for (PresencaItemRequest item : req.presencas()) {
            Aluno aluno = alunoRepository
                    .findById(item.alunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado: " + item.alunoId()));

            Presenca presenca = presencaRepository
                    .findByAlunoIdAndTurmaDisciplinaProfessorIdAndDataAula(
                            item.alunoId(), req.turmaDisciplinaProfessorId(), req.dataAula())
                    .orElseGet(Presenca::new);

            boolean nova = presenca.getId() == null;
            presenca.setAluno(aluno);
            presenca.setTurmaDisciplinaProfessor(tdp);
            presenca.setDataAula(req.dataAula());
            presenca.setPresente(item.presente());
            presenca.setJustificativa(item.justificativa());
            presencaRepository.save(presenca);
            if (nova) {
                criadas++;
            } else {
                atualizadas++;
            }
            if (!item.presente()) {
                notificacaoService.notificarResponsaveisDoAluno(
                        aluno.getId(),
                        TipoNotificacao.FALTA_REGISTRADA,
                        "Falta registrada",
                        tdp.getDisciplina().getNome() + " — aula em " + req.dataAula() + ".",
                        "/pais/desempenho",
                        presenca.getId());
            }
        }

        return Map.of(
                "dataAula", req.dataAula().toString(),
                "disciplina", tdp.getDisciplina().getNome(),
                "criadas", criadas,
                "atualizadas", atualizadas,
                "total", req.presencas().size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterBoletim(UUID alunoId) {
        Aluno aluno = alunoRepository
                .findDetalhadoById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));

        List<Nota> notas = notaRepository.findByAlunoId(alunoId);
        Map<UUID, Map<String, Object>> periodosMap = new LinkedHashMap<>();

        for (Nota nota : notas) {
            UUID periodoId = nota.getPeriodo().getId();
            Map<String, Object> periodoEntry = periodosMap.computeIfAbsent(periodoId, id -> {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("periodoId", id);
                p.put("periodoNome", nota.getPeriodo().getNome());
                p.put("disciplinas", new ArrayList<Map<String, Object>>());
                return p;
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) periodoEntry.get("disciplinas");
            UUID disciplinaId = nota.getTurmaDisciplinaProfessor().getDisciplina().getId();

            Map<String, Object> disciplinaEntry = disciplinas.stream()
                    .filter(d -> disciplinaId.equals(d.get("disciplinaId")))
                    .findFirst()
                    .orElseGet(() -> {
                        Map<String, Object> d = new LinkedHashMap<>();
                        d.put("disciplinaId", disciplinaId);
                        d.put("disciplinaNome", nota.getTurmaDisciplinaProfessor().getDisciplina().getNome());
                        d.put("notas", new ArrayList<Map<String, Object>>());
                        disciplinas.add(d);
                        return d;
                    });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> notasList = (List<Map<String, Object>>) disciplinaEntry.get("notas");
            Map<String, Object> notaItem = new LinkedHashMap<>();
            notaItem.put("id", nota.getId());
            notaItem.put("tipo", nota.getTipo().name());
            notaItem.put("valor", nota.getValor().doubleValue());
            notaItem.put("observacao", nota.getObservacao());
            notasList.add(notaItem);
        }

        List<Map<String, Object>> periodos = new ArrayList<>();
        double notaMinima = obterNotaMinimaAprovacao();
        for (Map<String, Object> periodoEntry : periodosMap.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) periodoEntry.get("disciplinas");
            List<Double> mediasDisciplinas = new ArrayList<>();

            for (Map<String, Object> disciplina : disciplinas) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> notasList = (List<Map<String, Object>>) disciplina.get("notas");
                double media = notasList.stream()
                        .mapToDouble(n -> ((Number) n.get("valor")).doubleValue())
                        .average()
                        .orElse(0.0);
                double mediaArredondada = round(media);
                disciplina.put("media", mediaArredondada);
                disciplina.put("aprovado", mediaArredondada >= notaMinima);
                mediasDisciplinas.add(mediaArredondada);
            }

            double mediaGeral = mediasDisciplinas.isEmpty()
                    ? 0.0
                    : round(mediasDisciplinas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            periodoEntry.put("mediaGeral", mediaGeral);
            periodoEntry.put("aprovado", mediaGeral >= notaMinima);
            periodos.add(periodoEntry);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alunoId", aluno.getId());
        out.put("alunoNome", aluno.getPessoa().getNome());
        out.put("turmaNome", aluno.getTurma() != null ? aluno.getTurma().getNome() : null);
        out.put("notaMinimaAprovacao", notaMinima);
        out.put("periodos", periodos);
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterFrequencia(UUID alunoId) {
        alunoRepository
                .findDetalhadoById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));

        List<Presenca> presencas = presencaRepository.findByAlunoId(alunoId);
        Map<UUID, Map<String, Object>> porDisciplina = new LinkedHashMap<>();
        int totalGeral = 0;
        int presentesGeral = 0;

        for (Presenca presenca : presencas) {
            UUID disciplinaId = presenca.getTurmaDisciplinaProfessor().getDisciplina().getId();
            Map<String, Object> entry = porDisciplina.computeIfAbsent(disciplinaId, id -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("disciplinaId", id);
                m.put("disciplinaNome", presenca.getTurmaDisciplinaProfessor().getDisciplina().getNome());
                m.put("totalAulas", 0);
                m.put("presencas", 0);
                m.put("faltas", 0);
                return m;
            });

            entry.put("totalAulas", ((int) entry.get("totalAulas")) + 1);
            totalGeral++;
            if (Boolean.TRUE.equals(presenca.getPresente())) {
                entry.put("presencas", ((int) entry.get("presencas")) + 1);
                presentesGeral++;
            } else {
                entry.put("faltas", ((int) entry.get("faltas")) + 1);
            }
        }

        double frequenciaMinima = obterFrequenciaMinima();
        List<Map<String, Object>> disciplinas = new ArrayList<>();
        for (Map<String, Object> entry : porDisciplina.values()) {
            int total = (int) entry.get("totalAulas");
            int presentes = (int) entry.get("presencas");
            double percentual = total == 0 ? 0.0 : round((presentes * 100.0) / total);
            entry.put("percentual", percentual);
            entry.put("aprovado", percentual >= frequenciaMinima);
            disciplinas.add(entry);
        }

        double percentualGeral = totalGeral == 0 ? 0.0 : round((presentesGeral * 100.0) / totalGeral);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alunoId", alunoId);
        out.put("percentualGeral", percentualGeral);
        out.put("frequenciaMinima", frequenciaMinima);
        out.put("aprovadoFrequencia", percentualGeral >= frequenciaMinima);
        out.put("porDisciplina", disciplinas);
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPresencasDaAula(UUID tdpId, LocalDate dataAula) {
        return presencaRepository.findByTurmaDisciplinaProfessorIdAndDataAula(tdpId, dataAula).stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("alunoId", p.getAluno().getId());
                    m.put("alunoNome", p.getAluno().getPessoa().getNome());
                    m.put("presente", p.getPresente());
                    m.put("justificativa", p.getJustificativa());
                    return m;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterMatrizPresencas(UUID tdpId, UUID periodoId) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(tdpId)
                .orElseThrow(() -> new IllegalArgumentException("Vinculo nao encontrado"));
        PeriodoAvaliacao periodo = periodoRepository
                .findById(periodoId)
                .orElseThrow(() -> new IllegalArgumentException("Periodo nao encontrado"));

        LocalDate inicio = periodo.getDataInicio() != null ? periodo.getDataInicio() : LocalDate.of(2000, 1, 1);
        LocalDate fim = periodo.getDataFim() != null ? periodo.getDataFim() : LocalDate.of(2100, 12, 31);

        List<Presenca> presencas =
                presencaRepository.findByTurmaDisciplinaProfessorIdAndDataAulaBetween(tdpId, inicio, fim);
        Set<LocalDate> datas = new TreeSet<>(presencaRepository.findDatasDistintasByTdpAndPeriodo(tdpId, inicio, fim));

        Map<String, Map<String, Object>> celulas = new HashMap<>();
        for (Presenca p : presencas) {
            String key = p.getAluno().getId() + "|" + p.getDataAula();
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("presente", p.getPresente());
            c.put("justificativa", p.getJustificativa());
            celulas.put(key, c);
        }

        List<Aluno> alunos = alunoRepository.findAtivosByTurmaId(tdp.getTurma().getId());
        List<Map<String, Object>> linhas = new ArrayList<>();
        int ordem = 1;
        for (Aluno aluno : alunos) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("ordem", ordem++);
            linha.put("alunoId", aluno.getId());
            linha.put("alunoNome", aluno.getPessoa().getNome());
            linha.put("matricula", aluno.getMatricula());

            Map<String, Object> porData = new LinkedHashMap<>();
            int faltas = 0;
            for (LocalDate data : datas) {
                Map<String, Object> cel = celulas.get(aluno.getId() + "|" + data);
                if (cel != null) {
                    porData.put(data.toString(), cel);
                    if (!Boolean.TRUE.equals(cel.get("presente"))) {
                        faltas++;
                    }
                }
            }
            linha.put("presencasPorData", porData);
            linha.put("totalFaltas", faltas);
            linhas.add(linha);
        }

        Optional<DiarioFrequenciaMeta> metaOpt =
                diarioFrequenciaMetaRepository.findByTurmaDisciplinaProfessorIdAndPeriodo_Id(tdpId, periodoId);
        int aulasDadas = datas.size();
        Integer aulasPrevistas = metaOpt.map(DiarioFrequenciaMeta::getAulasPrevistas).orElse(null);
        Instant assinaturaEm = metaOpt.map(DiarioFrequenciaMeta::getAssinaturaEm).orElse(null);

        String escolaNome = escolaRepository
                .findFirstByOrderByCriadoEmAsc()
                .map(Escola::getNome)
                .orElse("Escola");

        Map<String, Object> cabecalho = new LinkedHashMap<>();
        cabecalho.put("escolaNome", escolaNome);
        cabecalho.put("disciplinaNome", tdp.getDisciplina().getNome());
        cabecalho.put("turmaNome", tdp.getTurma().getNome());
        cabecalho.put("serieNome", tdp.getTurma().getSerie().getNome());
        cabecalho.put(
                "professorNome",
                tdp.getProfessor().getPessoa() != null ? tdp.getProfessor().getPessoa().getNome() : null);
        cabecalho.put("anoLetivo", tdp.getAnoLetivo().getAno());
        cabecalho.put("periodoNome", periodo.getNome());
        cabecalho.put("periodoInicio", periodo.getDataInicio() != null ? periodo.getDataInicio().toString() : null);
        cabecalho.put("periodoFim", periodo.getDataFim() != null ? periodo.getDataFim().toString() : null);
        cabecalho.put("aulasDadas", aulasDadas);
        cabecalho.put("aulasPrevistas", aulasPrevistas);
        cabecalho.put("assinaturaEm", assinaturaEm != null ? assinaturaEm.toString() : null);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cabecalho", cabecalho);
        out.put("datas", datas.stream().map(LocalDate::toString).toList());
        out.put("alunos", linhas);
        return out;
    }

    @Transactional
    public Map<String, Object> salvarMatrizPresencas(SalvarMatrizPresencaRequest req) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(req.turmaDisciplinaProfessorId())
                .orElseThrow(() -> new IllegalArgumentException("Vinculo nao encontrado"));
        PeriodoAvaliacao periodo = periodoRepository
                .findById(req.periodoId())
                .orElseThrow(() -> new IllegalArgumentException("Periodo nao encontrado"));

        Set<LocalDate> datasNovas = new LinkedHashSet<>();
        for (MatrizPresencaCelulaRequest cel : req.celulas()) {
            datasNovas.add(cel.dataAula());
            Aluno aluno = alunoRepository
                    .findById(cel.alunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
            Presenca presenca = presencaRepository
                    .findByAlunoIdAndTurmaDisciplinaProfessorIdAndDataAula(
                            cel.alunoId(), req.turmaDisciplinaProfessorId(), cel.dataAula())
                    .orElseGet(Presenca::new);
            presenca.setAluno(aluno);
            presenca.setTurmaDisciplinaProfessor(tdp);
            presenca.setDataAula(cel.dataAula());
            presenca.setPresente(cel.presente());
            presenca.setJustificativa(
                    !Boolean.TRUE.equals(cel.presente()) && cel.justificativa() != null
                            ? cel.justificativa().trim()
                            : null);
            presencaRepository.save(presenca);
        }

        if (req.aulasPrevistas() != null) {
            DiarioFrequenciaMeta meta = diarioFrequenciaMetaRepository
                    .findByTurmaDisciplinaProfessorIdAndPeriodo_Id(
                            req.turmaDisciplinaProfessorId(), req.periodoId())
                    .orElseGet(() -> {
                        DiarioFrequenciaMeta m = new DiarioFrequenciaMeta();
                        m.setTurmaDisciplinaProfessor(tdp);
                        m.setPeriodo(periodo);
                        return m;
                    });
            meta.setAulasPrevistas(req.aulasPrevistas());
            diarioFrequenciaMetaRepository.save(meta);
        }

        return obterMatrizPresencas(req.turmaDisciplinaProfessorId(), req.periodoId());
    }

    @Transactional
    public Map<String, Object> assinarMatrizPresencas(UUID tdpId, UUID periodoId) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(tdpId)
                .orElseThrow(() -> new IllegalArgumentException("Vinculo nao encontrado"));
        PeriodoAvaliacao periodo = periodoRepository
                .findById(periodoId)
                .orElseThrow(() -> new IllegalArgumentException("Periodo nao encontrado"));

        DiarioFrequenciaMeta meta = diarioFrequenciaMetaRepository
                .findByTurmaDisciplinaProfessorIdAndPeriodo_Id(tdpId, periodoId)
                .orElseGet(() -> {
                    DiarioFrequenciaMeta m = new DiarioFrequenciaMeta();
                    m.setTurmaDisciplinaProfessor(tdp);
                    m.setPeriodo(periodo);
                    return m;
                });
        meta.setAssinaturaEm(Instant.now());
        diarioFrequenciaMetaRepository.save(meta);
        return obterMatrizPresencas(tdpId, periodoId);
    }

    private Map<String, Object> toTurmaMap(Turma turma) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", turma.getId());
        m.put("nome", turma.getNome());
        m.put("serieId", turma.getSerie().getId());
        m.put("serieNome", turma.getSerie().getNome());
        m.put("nivelNome", turma.getSerie().getNivel().getNome());
        m.put("anoLetivo", turma.getAnoLetivo().getAno());
        return m;
    }

    private Map<String, Object> toAlunoMap(Aluno aluno) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", aluno.getId());
        m.put("nome", aluno.getPessoa().getNome());
        m.put("matricula", aluno.getMatricula());
        return m;
    }

    private Map<String, Object> toNotaMap(Nota nota) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", nota.getId());
        m.put("alunoId", nota.getAluno().getId());
        m.put("periodoId", nota.getPeriodo().getId());
        m.put("turmaDisciplinaProfessorId", nota.getTurmaDisciplinaProfessor().getId());
        m.put("valor", nota.getValor().doubleValue());
        m.put("tipo", nota.getTipo().name());
        m.put("observacao", nota.getObservacao());
        m.put("lancadoEm", nota.getLancadoEm().toString());
        return m;
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double obterNotaMinimaAprovacao() {
        return escolaRepository
                .findFirstByOrderByCriadoEmAsc()
                .map(Escola::getNotaMinimaAprovacao)
                .map(BigDecimal::doubleValue)
                .orElse(NOTA_MINIMA_PADRAO);
    }

    private double obterFrequenciaMinima() {
        return escolaRepository
                .findFirstByOrderByCriadoEmAsc()
                .map(Escola::getFrequenciaMinima)
                .map(BigDecimal::doubleValue)
                .orElse(FREQUENCIA_MINIMA_PADRAO);
    }
}
