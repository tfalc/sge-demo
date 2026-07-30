package br.com.sge.modules.school;

import br.com.sge.config.SchoolProperties;
import br.com.sge.modules.cadastro.entity.Escola;
import br.com.sge.modules.cadastro.repository.EscolaRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolConfigService {

  private final SchoolProperties schoolProperties;
  private final SchoolPackageLoader packageLoader;
  private final EscolaRepository escolaRepository;

  public SchoolConfigService(
      SchoolProperties schoolProperties,
      SchoolPackageLoader packageLoader,
      EscolaRepository escolaRepository) {
    this.schoolProperties = schoolProperties;
    this.packageLoader = packageLoader;
    this.escolaRepository = escolaRepository;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> obterConfiguracao() {
    String packageId = schoolProperties.packageId();
    Map<String, Object> pacote = packageLoader.loadEscolaYaml(packageId);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("packageId", packageId);
    out.put("id", pacote.get("id"));
    out.put("nome", pacote.get("nome"));
    out.put("nomeCurto", pacote.get("nome_curto"));
    out.put("siglaSge", pacote.get("sigla_sge"));
    out.put("municipio", pacote.get("municipio"));
    out.put("uf", pacote.get("uf"));
    out.put("tipo", pacote.get("tipo"));
    out.put("normativa", pacote.get("normativa"));
    out.put("regrasAcademicas", pacote.get("regras_academicas"));
    out.put("regrasFinanceiras", pacote.get("regras_financeiras"));
    out.put("branding", pacote.get("branding"));

    escolaRepository
        .findFirstByOrderByCriadoEmAsc()
        .ifPresent(escola -> out.put("escola", toEscolaResumo(escola)));

    return out;
  }

  private Map<String, Object> toEscolaResumo(Escola e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", e.getId());
    m.put("nome", e.getNome());
    m.put("cnpj", e.getCnpj());
    m.put("slug", e.getSlug());
    m.put("municipio", e.getMunicipio());
    m.put("uf", e.getUf());
    m.put("notaMinimaAprovacao", e.getNotaMinimaAprovacao());
    m.put("frequenciaMinima", e.getFrequenciaMinima());
    return m;
  }
}
