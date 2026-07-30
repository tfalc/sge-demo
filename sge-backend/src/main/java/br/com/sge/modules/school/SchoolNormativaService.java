package br.com.sge.modules.school;

import br.com.sge.config.SchoolProperties;
import br.com.sge.modules.academico.entity.MatrizComponente;
import br.com.sge.modules.academico.entity.MatrizCurricular;
import br.com.sge.modules.academico.repository.MatrizComponenteRepository;
import br.com.sge.modules.academico.repository.MatrizCurricularRepository;
import br.com.sge.modules.cadastro.entity.Escola;
import br.com.sge.modules.cadastro.entity.Serie;
import br.com.sge.modules.cadastro.repository.EscolaRepository;
import br.com.sge.modules.cadastro.repository.SerieRepository;
import br.com.sge.modules.financeiro.entity.PlanoPagamento;
import br.com.sge.modules.financeiro.repository.PlanoPagamentoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolNormativaService {

  private final SchoolProperties schoolProperties;
  private final SchoolPackageLoader packageLoader;
  private final EscolaRepository escolaRepository;
  private final PlanoPagamentoRepository planoPagamentoRepository;
  private final MatrizCurricularRepository matrizRepository;
  private final MatrizComponenteRepository matrizComponenteRepository;
  private final SerieRepository serieRepository;

  public SchoolNormativaService(
      SchoolProperties schoolProperties,
      SchoolPackageLoader packageLoader,
      EscolaRepository escolaRepository,
      PlanoPagamentoRepository planoPagamentoRepository,
      MatrizCurricularRepository matrizRepository,
      MatrizComponenteRepository matrizComponenteRepository,
      SerieRepository serieRepository) {
    this.schoolProperties = schoolProperties;
    this.packageLoader = packageLoader;
    this.escolaRepository = escolaRepository;
    this.planoPagamentoRepository = planoPagamentoRepository;
    this.matrizRepository = matrizRepository;
    this.matrizComponenteRepository = matrizComponenteRepository;
    this.serieRepository = serieRepository;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obterNormativa() {
    String packageId = schoolProperties.packageId();
    Map<String, Object> pacote = packageLoader.loadEscolaYaml(packageId);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("packageId", packageId);
    out.put("fonte", "schools/" + packageId + "/escola.yaml");
    out.put(
        "avisoPreservacao",
        "Consulta somente leitura — nenhuma matriz cadastrada e alterada. "
            + "A aplicacao posterior so inclui matrizes ausentes, apos confirmacao.");
    out.put("normativa", pacote.get("normativa"));
    out.put("regrasAcademicas", pacote.get("regras_academicas"));
    out.put("regrasFinanceiras", pacote.get("regras_financeiras"));
    out.put(
        "resumo",
        List.of(
            "Nacional: BNCC e LDB (800 h/ano no EF, frequencia minima 75%)",
            "Estadual: conforme referencias em escola.yaml do pacote",
            "Municipal (referencia): conforme pacote da escola, se configurado",
            "Modo normativo: carga exata da Res. 4746",
            "Modo diretrizes: faixas de aulas por componente dentro dos limites RJ/BNCC"));
    out.put("matrizesPacote", listarMatrizesPacote(packageId));
    return out;
  }

  @Transactional
  public Map<String, Object> previewAplicar() {
    Escola escola = escolaObrigatoria();
    Instant consultadaEm = Instant.now();
    escola.setNormativaConsultadaEm(consultadaEm);
    escolaRepository.save(escola);

    Map<String, Object> pacote = packageLoader.loadEscolaYaml(schoolProperties.packageId());
    Map<String, Object> regrasAcad = map(pacote.get("regras_academicas"));
    Map<String, Object> regrasFin = map(pacote.get("regras_financeiras"));

    List<Map<String, Object>> alteracoes = new ArrayList<>();

    BigDecimal notaAlvo = decimal(regrasAcad.get("nota_minima_aprovacao"), "6.00");
    BigDecimal freqAlvo = decimal(regrasAcad.get("frequencia_minima_percentual"), "75.00");
    if (escola.getNotaMinimaAprovacao().compareTo(notaAlvo) != 0) {
      alteracoes.add(
          alteracao(
              "ESCOLA",
              "Nota minima de aprovacao",
              escola.getNotaMinimaAprovacao(),
              notaAlvo));
    }
    if (escola.getFrequenciaMinima().compareTo(freqAlvo) != 0) {
      alteracoes.add(
          alteracao(
              "ESCOLA",
              "Frequencia minima (%)",
              escola.getFrequenciaMinima(),
              freqAlvo));
    }

    int diaVencimento = intVal(regrasFin.get("dia_vencimento_padrao"), 10);
    for (PlanoPagamento plano : planoPagamentoRepository.findAllByOrderByNomeAsc()) {
      if (plano.getDiaVencimento() != diaVencimento) {
        alteracoes.add(
            alteracao(
                "FINANCEIRO",
                "Plano \"" + plano.getNome() + "\" — dia vencimento",
                plano.getDiaVencimento(),
                diaVencimento));
      }
    }

    for (Map<String, Object> matrizPacote : listarMatrizesPacote(schoolProperties.packageId())) {
      String codigo = str(matrizPacote.get("codigo"));
      var existente = matrizRepository.findByEscolaIdAndCodigo(escola.getId(), codigo);
      if (existente.isEmpty()) {
        alteracoes.add(
            Map.of(
                "area",
                "MATRIZ",
                "campo",
                "Matriz " + codigo,
                "atual",
                "(nao cadastrada)",
                "novo",
                "Criar do pacote da escola (vigente apos consulta)"));
      } else {
        alteracoes.add(
            Map.of(
                "area",
                "MATRIZ",
                "campo",
                "Matriz " + codigo,
                "atual",
                "Cadastrada — preservada",
                "novo",
                "Sem alteracao"));
      }
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("alteracoes", alteracoes);
    out.put(
        "temAlteracoes",
        alteracoes.stream().anyMatch(a -> !"Sem alteracao".equals(String.valueOf(a.get("novo")))));
    out.put("packageId", schoolProperties.packageId());
    out.put(
        "preservacaoMatrizes",
        "Matrizes ja salvas no sistema nao serao sobrescritas pela normativa do pacote.");
    out.put("consultadaEm", consultadaEm);
    return out;
  }

  @Transactional
  public Map<String, Object> aplicarNormativa() {
    Escola escola = escolaObrigatoria();
    if (escola.getNormativaConsultadaEm() == null) {
      throw new IllegalStateException(
          "Gere o preview da normativa antes de aplicar. Matrizes ja cadastradas permanecem intactas.");
    }
    Instant consultadaEm = escola.getNormativaConsultadaEm();

    String packageId = schoolProperties.packageId();
    Map<String, Object> pacote = packageLoader.loadEscolaYaml(packageId);
    Map<String, Object> regrasAcad = map(pacote.get("regras_academicas"));
    Map<String, Object> regrasFin = map(pacote.get("regras_financeiras"));

    escola.setNotaMinimaAprovacao(
        decimal(regrasAcad.get("nota_minima_aprovacao"), "6.00"));
    escola.setFrequenciaMinima(
        decimal(regrasAcad.get("frequencia_minima_percentual"), "75.00"));
    escolaRepository.save(escola);

    int diaVencimento = intVal(regrasFin.get("dia_vencimento_padrao"), 10);
    int planosAtualizados = 0;
    for (PlanoPagamento plano : planoPagamentoRepository.findAllByOrderByNomeAsc()) {
      if (plano.getDiaVencimento() != diaVencimento) {
        plano.setDiaVencimento(diaVencimento);
        planoPagamentoRepository.save(plano);
        planosAtualizados++;
      }
    }

    int matrizesCriadas = 0;
    int matrizesPreservadas = 0;
    for (String file : packageLoader.listCurriculoFiles()) {
      if (criarMatrizSeAusente(escola, packageId, file, consultadaEm)) {
        matrizesCriadas++;
      } else {
        matrizesPreservadas++;
      }
    }

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("aplicado", true);
    out.put("escolaId", escola.getId());
    out.put("planosAtualizados", planosAtualizados);
    out.put("matrizesCriadas", matrizesCriadas);
    out.put("matrizesPreservadas", matrizesPreservadas);
    out.put("matrizesSincronizadas", matrizesCriadas);
    out.put("consultadaEm", consultadaEm);
    out.put("diaVencimentoPadrao", diaVencimento);
    return out;
  }

  private boolean criarMatrizSeAusente(
      Escola escola, String packageId, String fileName, Instant consultadaEm) {
    Map<String, Object> yaml = packageLoader.loadCurriculoYaml(packageId, fileName);
    String codigo = str(yaml.get("id"));

    if (matrizRepository.findByEscolaIdAndCodigo(escola.getId(), codigo).isPresent()) {
      return false;
    }

    Instant vigencia = Instant.now();
    if (vigencia.isBefore(consultadaEm)) {
      vigencia = consultadaEm;
    }

    MatrizCurricular matriz = new MatrizCurricular();
    matriz.setEscola(escola);
    matriz.setCodigo(codigo);
    matriz.setNome(str(yaml.get("nome")));
    matriz.setEtapa(str(yaml.get("etapa")));
    matriz.setModalidade(str(yaml.get("modalidade")));
    matriz.setModoValidacao(strOrDefault(yaml.get("modo_validacao"), "NORMATIVO"));
    matriz.setAulasSemanaisTotal(intVal(yaml.get("aulas_semanais_total"), 20));
    matriz.setAulasSemanaisTotalMin(intOrNull(yaml.get("aulas_semanais_total_min")));
    matriz.setAulasSemanaisTotalMax(intOrNull(yaml.get("aulas_semanais_total_max")));
    matriz.setMinutosAula(intVal(yaml.get("minutos_aula"), 50));
    matriz.setHorasAnuaisMinimas(intVal(yaml.get("horas_anuais_minimas"), 800));
    matriz.setNormativaRef(str(yaml.get("normativa_ref")));
    matriz.setAtivo(true);
    matriz.setSerie(resolverSerie(yaml));
    matriz.setCriadoEm(vigencia);
    matriz.setSincronizadaNormativaEm(vigencia);

    MatrizCurricular salva = matrizRepository.save(matriz);

    List<Map<String, Object>> componentes = list(yaml.get("componentes"));
    int ordem = 1;
    for (Map<String, Object> c : componentes) {
      MatrizComponente comp = new MatrizComponente();
      comp.setMatriz(salva);
      comp.setComponente(str(c.get("componente")));
      comp.setArea(str(c.get("area")));
      int ref = intVal(c.get("aulas_semanais"), 1);
      comp.setAulasSemanais(ref);
      comp.setAulasSemanaisMin(intOrNull(c.get("aulas_semanais_min")));
      comp.setAulasSemanaisMax(intOrNull(c.get("aulas_semanais_max")));
      comp.setBaseNacionalComum(bool(c.get("base_nacional_comum")));
      comp.setOrdem(ordem++);
      matrizComponenteRepository.save(comp);
    }
    return true;
  }

  private Serie resolverSerie(Map<String, Object> yaml) {
    List<String> nomes = stringList(yaml.get("serie_nomes"));
    if (nomes.size() != 1) {
      return null;
    }
    List<Serie> series = serieRepository.findAllComNivel();
    String alvo = nomes.get(0).trim();
    for (Serie s : series) {
      if (s.getNome().equalsIgnoreCase(alvo)) {
        return s;
      }
    }
    return null;
  }

  private List<Map<String, Object>> listarMatrizesPacote(String packageId) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (String file : packageLoader.listCurriculoFiles()) {
      Map<String, Object> yaml = packageLoader.loadCurriculoYaml(packageId, file);
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("arquivo", file);
      m.put("codigo", yaml.get("id"));
      m.put("nome", yaml.get("nome"));
      m.put("modoValidacao", yaml.getOrDefault("modo_validacao", "NORMATIVO"));
      m.put("normativaRef", yaml.get("normativa_ref"));
      out.add(m);
    }
    return out;
  }

  private Escola escolaObrigatoria() {
    return escolaRepository
        .findFirstByOrderByCriadoEmAsc()
        .orElseThrow(() -> new IllegalArgumentException("Escola nao configurada"));
  }

  private static Map<String, Object> alteracao(String area, String campo, Object atual, Object novo) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("area", area);
    m.put("campo", campo);
    m.put("atual", atual);
    m.put("novo", novo);
    return m;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> map(Object value) {
    if (value instanceof Map<?, ?> m) {
      return (Map<String, Object>) m;
    }
    return Map.of();
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> list(Object value) {
    if (value instanceof List<?> l) {
      return l.stream().filter(Map.class::isInstance).map(m -> (Map<String, Object>) m).toList();
    }
    return List.of();
  }

  @SuppressWarnings("unchecked")
  private static List<String> stringList(Object value) {
    if (value instanceof List<?> l) {
      return l.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }
    return List.of();
  }

  private static String str(Object value) {
    return value == null ? "" : value.toString();
  }

  private static String strOrDefault(Object value, String fallback) {
    String s = str(value);
    return s.isBlank() ? fallback : s;
  }

  private static int intVal(Object value, int fallback) {
    if (value instanceof Number n) {
      return n.intValue();
    }
    try {
      return value == null ? fallback : Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static Integer intOrNull(Object value) {
    if (value == null) {
      return null;
    }
    return intVal(value, 0);
  }

  private static boolean bool(Object value) {
    if (value instanceof Boolean b) {
      return b;
    }
    return value != null && Boolean.parseBoolean(value.toString());
  }

  private static BigDecimal decimal(Object value, String fallback) {
    if (value == null) {
      return new BigDecimal(fallback);
    }
    try {
      return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      return new BigDecimal(fallback);
    }
  }
}
