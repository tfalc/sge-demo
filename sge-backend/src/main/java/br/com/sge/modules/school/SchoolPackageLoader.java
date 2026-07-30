package br.com.sge.modules.school;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class SchoolPackageLoader {

  private static final List<String> CURRICULO_FILES =
      List.of(
          "ef-anos-iniciais-regular-parcial.yaml",
          "ef-anos-finais-regular-parcial.yaml",
          "ef-anos-iniciais-diretrizes.yaml");

  private final Yaml yaml = new Yaml();

  public List<String> listCurriculoFiles() {
    return CURRICULO_FILES;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> loadEscolaYaml(String packageId) {
    String path = "schools/" + packageId + "/escola.yaml";
    try (InputStream in = new ClassPathResource(path).getInputStream()) {
      Object loaded = yaml.load(in);
      if (loaded instanceof Map<?, ?> map) {
        return new LinkedHashMap<>((Map<String, Object>) map);
      }
      throw new IllegalStateException("Formato invalido em " + path);
    } catch (Exception e) {
      throw new IllegalStateException("Pacote da escola nao encontrado: " + packageId, e);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> loadCurriculoYaml(String packageId, String fileName) {
    String path = "schools/" + packageId + "/curriculo/" + fileName;
    try (InputStream in = new ClassPathResource(path).getInputStream()) {
      Object loaded = yaml.load(in);
      if (loaded instanceof Map<?, ?> map) {
        return new LinkedHashMap<>((Map<String, Object>) map);
      }
      throw new IllegalStateException("Formato invalido em " + path);
    } catch (Exception e) {
      throw new IllegalStateException("Curriculo nao encontrado: " + path, e);
    }
  }
}
