package br.com.sge.modules.academico.service;

import br.com.sge.modules.academico.entity.Disciplina;
import br.com.sge.modules.academico.entity.HorarioAula;
import br.com.sge.modules.academico.entity.MatrizComponente;
import br.com.sge.modules.academico.entity.MatrizCurricular;
import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.academico.repository.HorarioAulaRepository;
import br.com.sge.modules.academico.repository.MatrizCurricularRepository;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatrizCurricularService {

  private final MatrizCurricularRepository matrizRepository;
  private final TurmaRepository turmaRepository;
  private final TurmaDisciplinaProfessorRepository tdpRepository;
  private final HorarioAulaRepository horarioRepository;

  public MatrizCurricularService(
      MatrizCurricularRepository matrizRepository,
      TurmaRepository turmaRepository,
      TurmaDisciplinaProfessorRepository tdpRepository,
      HorarioAulaRepository horarioRepository) {
    this.matrizRepository = matrizRepository;
    this.turmaRepository = turmaRepository;
    this.tdpRepository = tdpRepository;
    this.horarioRepository = horarioRepository;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> listar() {
    return matrizRepository.findAllAtivas().stream().map(this::toResumo).toList();
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obter(UUID id) {
    MatrizCurricular matriz =
        matrizRepository
            .findDetalhadaById(id)
            .orElseThrow(() -> new IllegalArgumentException("Matriz curricular nao encontrada"));
    return toDetalhe(matriz);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obterPorSerie(UUID serieId) {
    MatrizCurricular matriz =
        matrizRepository
            .findAtivaBySerieId(serieId)
            .orElseThrow(
                () -> new IllegalArgumentException("Matriz nao definida para esta serie"));
    return toDetalhe(matriz);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> validarTurma(UUID turmaId, UUID matrizId) {
    Turma turma =
        turmaRepository
            .findDetalhadaById(turmaId)
            .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));

    MatrizCurricular matriz;
    if (matrizId != null) {
      matriz =
          matrizRepository
              .findDetalhadaById(matrizId)
              .orElseThrow(() -> new IllegalArgumentException("Matriz curricular nao encontrada"));
    } else {
      matriz =
          matrizRepository
              .findAtivaBySerieId(turma.getSerie().getId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Matriz curricular nao configurada para a serie "
                              + turma.getSerie().getNome()));
    }

    boolean diretrizes = "DIRETRIZES".equalsIgnoreCase(matriz.getModoValidacao());

    List<TurmaDisciplinaProfessor> vinculos = tdpRepository.findByTurmaId(turmaId, null);
    List<HorarioAula> horarios = horarioRepository.findByTurmaId(turmaId);

    Map<String, Long> horariosPorDisciplina =
        horarios.stream()
            .collect(
                Collectors.groupingBy(
                    h -> normalize(h.getDisciplina().getNome()), Collectors.counting()));

    List<Map<String, Object>> itens = new ArrayList<>();
    boolean conforme = true;

    for (MatrizComponente comp : matriz.getComponentes()) {
      Disciplina disciplinaVinculada = encontrarDisciplina(vinculos, comp.getComponente());
      long aulasGrade = horariosPorDisciplina.getOrDefault(normalize(comp.getComponente()), 0L);
      if (disciplinaVinculada != null) {
        aulasGrade =
            Math.max(
                aulasGrade,
                horarios.stream()
                    .filter(h -> h.getDisciplina().getId().equals(disciplinaVinculada.getId()))
                    .count());
      }

      int minAulas = comp.getAulasSemanaisMin() != null ? comp.getAulasSemanaisMin() : comp.getAulasSemanais();
      int maxAulas = comp.getAulasSemanaisMax() != null ? comp.getAulasSemanaisMax() : comp.getAulasSemanais();

      boolean vinculoOk = disciplinaVinculada != null;
      boolean gradeOk =
          diretrizes
              ? (aulasGrade == 0 || (aulasGrade >= minAulas && aulasGrade <= maxAulas))
              : aulasGrade == comp.getAulasSemanais();
      boolean itemConforme =
          diretrizes
              ? (aulasGrade == 0 ? !comp.isBaseNacionalComum() : gradeOk)
              : (vinculoOk && gradeOk);

      if (!itemConforme) {
        conforme = false;
      }

      Map<String, Object> item = new LinkedHashMap<>();
      item.put("componente", comp.getComponente());
      item.put("area", comp.getArea());
      item.put("aulasEsperadas", comp.getAulasSemanais());
      item.put("aulasMinimas", minAulas);
      item.put("aulasMaximas", maxAulas);
      item.put("aulasNaGrade", aulasGrade);
      item.put("vinculoDisciplina", vinculoOk);
      item.put("disciplinaNome", disciplinaVinculada != null ? disciplinaVinculada.getNome() : null);
      item.put("conforme", itemConforme);
      item.put("observacao", diretrizes && !vinculoOk ? "Sugestao BNCC — vinculo opcional" : null);
      itens.add(item);
    }

    long totalGrade = horarios.size();
    boolean totalOk;
    if (diretrizes) {
      int totalMin =
          matriz.getAulasSemanaisTotalMin() != null
              ? matriz.getAulasSemanaisTotalMin()
              : matriz.getAulasSemanaisTotal();
      int totalMax =
          matriz.getAulasSemanaisTotalMax() != null
              ? matriz.getAulasSemanaisTotalMax()
              : matriz.getAulasSemanaisTotal();
      totalOk = totalGrade >= totalMin && totalGrade <= totalMax;
    } else {
      totalOk = totalGrade == matriz.getAulasSemanaisTotal();
    }
    if (!totalOk) {
      conforme = false;
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("turmaId", turmaId);
    out.put("turmaNome", turma.getNome());
    out.put("serieNome", turma.getSerie().getNome());
    out.put("matrizId", matriz.getId());
    out.put("matrizNome", matriz.getNome());
    out.put("modoValidacao", matriz.getModoValidacao());
    out.put("aulasSemanaisEsperadas", matriz.getAulasSemanaisTotal());
    out.put("aulasSemanaisMinimas", matriz.getAulasSemanaisTotalMin());
    out.put("aulasSemanaisMaximas", matriz.getAulasSemanaisTotalMax());
    out.put("aulasSemanaisNaGrade", totalGrade);
    out.put("minutosAula", matriz.getMinutosAula());
    out.put("normativaRef", matriz.getNormativaRef());
    out.put("conforme", conforme && totalOk);
    out.put("itens", itens);
    return out;
  }

  private Disciplina encontrarDisciplina(
      List<TurmaDisciplinaProfessor> vinculos, String componente) {
    String alvo = normalize(componente);
    for (TurmaDisciplinaProfessor v : vinculos) {
      String nome = normalize(v.getDisciplina().getNome());
      if (nome.equals(alvo) || nome.contains(alvo) || alvo.contains(nome)) {
        return v.getDisciplina();
      }
    }
    return null;
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]", "");
  }

  private Map<String, Object> toResumo(MatrizCurricular m) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", m.getId());
    map.put("codigo", m.getCodigo());
    map.put("nome", m.getNome());
    map.put("etapa", m.getEtapa());
    map.put("modalidade", m.getModalidade());
    map.put("modoValidacao", m.getModoValidacao());
    map.put("aulasSemanaisTotal", m.getAulasSemanaisTotal());
    map.put("aulasSemanaisTotalMin", m.getAulasSemanaisTotalMin());
    map.put("aulasSemanaisTotalMax", m.getAulasSemanaisTotalMax());
    map.put("minutosAula", m.getMinutosAula());
    map.put("normativaRef", m.getNormativaRef());
    if (m.getSerie() != null) {
      map.put("serieId", m.getSerie().getId());
      map.put("serieNome", m.getSerie().getNome());
    }
    return map;
  }

  private Map<String, Object> toDetalhe(MatrizCurricular m) {
    Map<String, Object> map = toResumo(m);
    List<Map<String, Object>> componentes =
        m.getComponentes().stream()
            .sorted((a, b) -> Integer.compare(a.getOrdem(), b.getOrdem()))
            .map(
                c -> {
                  Map<String, Object> cm = new HashMap<>();
                  cm.put("id", c.getId());
                  cm.put("componente", c.getComponente());
                  cm.put("area", c.getArea());
                  cm.put("aulasSemanais", c.getAulasSemanais());
                  cm.put("aulasSemanaisMin", c.getAulasSemanaisMin());
                  cm.put("aulasSemanaisMax", c.getAulasSemanaisMax());
                  cm.put("baseNacionalComum", c.isBaseNacionalComum());
                  cm.put("ordem", c.getOrdem());
                  return cm;
                })
            .toList();
    map.put("componentes", componentes);
    map.put("horasAnuaisMinimas", m.getHorasAnuaisMinimas());
    return map;
  }
}
