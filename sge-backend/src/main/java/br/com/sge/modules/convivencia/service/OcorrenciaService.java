package br.com.sge.modules.convivencia.service;

import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.convivencia.dto.RegistrarOcorrenciaRequest;
import br.com.sge.modules.convivencia.entity.OcorrenciaDisciplinar;
import br.com.sge.modules.convivencia.entity.StatusOcorrencia;
import br.com.sge.modules.convivencia.repository.OcorrenciaDisciplinarRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OcorrenciaService {

    private final OcorrenciaDisciplinarRepository ocorrenciaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final NotificacaoService notificacaoService;

    public OcorrenciaService(
            OcorrenciaDisciplinarRepository ocorrenciaRepository,
            AlunoRepository alunoRepository,
            TurmaDisciplinaProfessorRepository tdpRepository,
            NotificacaoService notificacaoService) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.alunoRepository = alunoRepository;
        this.tdpRepository = tdpRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Map<String, Object> registrar(RegistrarOcorrenciaRequest req) {
        Aluno aluno = alunoRepository
                .findDetalhadoById(req.alunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findDetalhadoById(req.turmaDisciplinaProfessorId())
                .orElseThrow(() -> new IllegalArgumentException("Vinculo turma/disciplina nao encontrado"));

        OcorrenciaDisciplinar o = new OcorrenciaDisciplinar();
        o.setAluno(aluno);
        o.setTurmaDisciplinaProfessor(tdp);
        o.setDataOcorrencia(req.dataOcorrencia());
        o.setTipo(req.tipo());
        o.setDescricao(req.descricao().trim());
        o.setStatus(StatusOcorrencia.REGISTRADA);
        o.setCriadoEm(Instant.now());

        OcorrenciaDisciplinar saved = ocorrenciaRepository.save(o);
        String disciplina = tdp.getDisciplina().getNome();
        notificacaoService.notificarPorPerfil(
                PerfilUsuario.COORDENADOR,
                TipoNotificacao.OCORRENCIA_REGISTRADA,
                "Nova ocorrencia disciplinar",
                aluno.getPessoa().getNome() + " — " + disciplina + " (" + req.tipo().name() + ").",
                "/coordenacao",
                saved.getId());
        return toMap(saved);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorVinculo(UUID tdpId) {
        return ocorrenciaRepository
                .findByTurmaDisciplinaProfessorIdOrderByDataOcorrenciaDescCriadoEmDesc(tdpId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorTurma(UUID turmaId) {
        return ocorrenciaRepository.findByTurmaId(turmaId).stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> marcarVista(UUID id) {
        OcorrenciaDisciplinar o = ocorrenciaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrencia nao encontrada"));
        o.setStatus(StatusOcorrencia.VISTA);
        return toMap(ocorrenciaRepository.save(o));
    }

    private Map<String, Object> toMap(OcorrenciaDisciplinar o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("alunoId", o.getAluno().getId());
        m.put("alunoNome", o.getAluno().getPessoa().getNome());
        m.put("turmaDisciplinaProfessorId", o.getTurmaDisciplinaProfessor().getId());
        m.put("disciplinaNome", o.getTurmaDisciplinaProfessor().getDisciplina().getNome());
        m.put("dataOcorrencia", o.getDataOcorrencia().toString());
        m.put("tipo", o.getTipo().name());
        m.put("descricao", o.getDescricao());
        m.put("status", o.getStatus().name());
        m.put("criadoEm", o.getCriadoEm().toString());
        return m;
    }
}
