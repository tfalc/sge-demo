package br.com.sge.modules.admin.service;

import br.com.sge.modules.admin.dto.AtualizarAcessosMenuRequest;
import br.com.sge.modules.admin.entity.PerfilAcessoArea;
import br.com.sge.modules.admin.repository.PerfilAcessoAreaRepository;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilAcessoMenuService {

    public static final List<String> AREAS = List.of(
            "pais",
            "aluno",
            "professor",
            "secretaria",
            "direcao",
            "coordenacao",
            "nutricao",
            "psicologia");

    /** Defaults alinhados ao seed V32 / navConfig histórico. */
    public static final Map<String, List<String>> DEFAULTS = Map.of(
            "ADMIN", List.copyOf(AREAS),
            "DIRETOR", List.of("direcao", "coordenacao"),
            "COORDENADOR", List.of("coordenacao"),
            "PROFESSOR", List.of("professor"),
            "SECRETARIA", List.of("secretaria"),
            "PAI", List.of("pais"),
            "ALUNO", List.of("aluno"),
            "NUTRICIONISTA", List.of("nutricao"),
            "PSICOLOGA", List.of("psicologia"));

    private final PerfilAcessoAreaRepository repository;

    public PerfilAcessoMenuService(PerfilAcessoAreaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterMatriz() {
        Map<String, List<String>> acessos = new LinkedHashMap<>();
        for (PerfilUsuario perfil : PerfilUsuario.values()) {
            acessos.put(perfil.name(), areasHabilitadas(perfil.name()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("areas", AREAS);
        out.put("perfis", List.of(PerfilUsuario.values()).stream().map(Enum::name).toList());
        out.put("acessos", acessos);
        out.put("defaults", DEFAULTS);
        return out;
    }

    @Transactional(readOnly = true)
    public List<String> areasHabilitadas(String perfil) {
        if (perfil == null || perfil.isBlank()) {
            return List.of();
        }
        String key = perfil.trim().toUpperCase();
        List<PerfilAcessoArea> rows = repository.findByPerfil(key);
        if (rows.isEmpty()) {
            return new ArrayList<>(DEFAULTS.getOrDefault(key, List.of()));
        }
        return rows.stream()
                .filter(PerfilAcessoArea::isHabilitado)
                .map(PerfilAcessoArea::getArea)
                .filter(AREAS::contains)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public Map<String, Object> salvarMatriz(AtualizarAcessosMenuRequest request) {
        if (request == null || request.acessos() == null) {
            throw new IllegalArgumentException("Payload de acessos obrigatorio");
        }

        List<PerfilAcessoArea> toSave = new ArrayList<>();
        for (PerfilUsuario perfilEnum : PerfilUsuario.values()) {
            String perfil = perfilEnum.name();
            List<String> areasReq = request.acessos().getOrDefault(perfil, List.of());
            Set<String> habilitadas = new LinkedHashSet<>();
            for (String a : areasReq) {
                if (a == null) continue;
                String area = a.trim().toLowerCase();
                if (AREAS.contains(area)) {
                    habilitadas.add(area);
                }
            }
            for (String area : AREAS) {
                toSave.add(new PerfilAcessoArea(perfil, area, habilitadas.contains(area)));
            }
        }
        repository.deleteAllInBatch();
        repository.saveAll(toSave);
        return obterMatriz();
    }

    @Transactional
    public Map<String, Object> restaurarDefaults() {
        AtualizarAcessosMenuRequest req = new AtualizarAcessosMenuRequest(new LinkedHashMap<>(DEFAULTS));
        return salvarMatriz(req);
    }
}
