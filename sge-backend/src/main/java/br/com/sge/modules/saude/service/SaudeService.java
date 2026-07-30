package br.com.sge.modules.saude.service;

import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.saude.dto.CriarAgendamentoSaudeRequest;
import br.com.sge.modules.saude.entity.AgendamentoSaude;
import br.com.sge.modules.saude.entity.ProfissionalSaude;
import br.com.sge.modules.saude.repository.AgendamentoSaudeRepository;
import br.com.sge.modules.saude.repository.ProfissionalSaudeRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaudeService {

    private final AgendamentoSaudeRepository agendamentoRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final AlunoRepository alunoRepository;
    private final NotificacaoService notificacaoService;

    public SaudeService(
            AgendamentoSaudeRepository agendamentoRepository,
            ProfissionalSaudeRepository profissionalRepository,
            AlunoRepository alunoRepository,
            NotificacaoService notificacaoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.profissionalRepository = profissionalRepository;
        this.alunoRepository = alunoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAgendaProfissional(UUID profissionalId) {
        return agendamentoRepository.findByProfissionalId(profissionalId).stream()
                .map(a -> toAgendamentoMap(a, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarHistoricoAluno(UUID alunoId, boolean incluirPrivado) {
        return agendamentoRepository.findByAlunoId(alunoId).stream()
                .map(a -> toAgendamentoMap(a, incluirPrivado))
                .toList();
    }

    @Transactional
    public Map<String, Object> criarAgendamento(CriarAgendamentoSaudeRequest req, UUID profissionalId) {
        ProfissionalSaude profissional = profissionalRepository
                .findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional nao encontrado"));
        Aluno aluno = alunoRepository
                .findById(req.alunoId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));

        AgendamentoSaude a = new AgendamentoSaude();
        a.setProfissional(profissional);
        a.setAluno(aluno);
        a.setDataHora(req.dataHora());
        a.setObservacoes(req.observacoes());
        a.setPrivado(req.privado() == null || req.privado());
        a.setStatus("AGENDADO");

        AgendamentoSaude saved = agendamentoRepository.save(a);
        if (!Boolean.TRUE.equals(saved.getPrivado())) {
            notificacaoService.notificarResponsaveisDoAluno(
                    aluno.getId(),
                    TipoNotificacao.AGENDAMENTO_SAUDE,
                    "Agendamento de saude",
                    "Novo agendamento para " + aluno.getPessoa().getNome() + " em " + saved.getDataHora() + ".",
                    "/pais/saude",
                    saved.getId());
        }
        return toAgendamentoMap(saved, true);
    }

    private Map<String, Object> toAgendamentoMap(AgendamentoSaude a, boolean incluirPrivado) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("alunoId", a.getAluno().getId());
        m.put("alunoNome", a.getAluno().getPessoa().getNome());
        m.put("dataHora", a.getDataHora().toString());
        m.put("status", a.getStatus());
        m.put("privado", a.getPrivado());
        if (incluirPrivado || !Boolean.TRUE.equals(a.getPrivado())) {
            m.put("observacoes", a.getObservacoes());
        } else {
            m.put("observacoes", null);
        }
        return m;
    }
}
