package br.com.sge.modules.nutricao.service;

import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.nutricao.dto.CriarRestricaoAlimentarRequest;
import br.com.sge.modules.nutricao.entity.RestricaoAlimentar;
import br.com.sge.modules.nutricao.repository.RestricaoAlimentarRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestricaoAlimentarService {

    private final RestricaoAlimentarRepository restricaoRepository;
    private final AlunoRepository alunoRepository;

    public RestricaoAlimentarService(
            RestricaoAlimentarRepository restricaoRepository, AlunoRepository alunoRepository) {
        this.restricaoRepository = restricaoRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(UUID alunoId) {
        List<RestricaoAlimentar> lista =
                alunoId != null ? restricaoRepository.findByAlunoId(alunoId) : restricaoRepository.findAllComAluno();
        return lista.stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> criar(CriarRestricaoAlimentarRequest req) {
        Aluno aluno = alunoRepository
                .findById(req.alunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        RestricaoAlimentar r = new RestricaoAlimentar();
        r.setAluno(aluno);
        r.setDescricao(req.descricao().trim());
        r.setSeveridade(normalizarSeveridade(req.severidade()));
        return toMap(restricaoRepository.save(r));
    }

    @Transactional
    public void excluir(UUID id) {
        if (!restricaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Restricao nao encontrada");
        }
        restricaoRepository.deleteById(id);
    }

    private String normalizarSeveridade(String severidade) {
        if (severidade == null || severidade.isBlank()) {
            return "MODERADA";
        }
        return severidade.trim().toUpperCase();
    }

    private Map<String, Object> toMap(RestricaoAlimentar r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("alunoId", r.getAluno().getId());
        m.put("alunoNome", r.getAluno().getPessoa().getNome());
        m.put("descricao", r.getDescricao());
        m.put("severidade", r.getSeveridade());
        m.put("criadoEm", r.getCriadoEm());
        return m;
    }
}
