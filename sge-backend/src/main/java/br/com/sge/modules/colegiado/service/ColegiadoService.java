package br.com.sge.modules.colegiado.service;

import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.colegiado.dto.AtualizarEncaminhamentoRequest;
import br.com.sge.modules.colegiado.dto.AtualizarReuniaoColegiadoRequest;
import br.com.sge.modules.colegiado.dto.CriarEncaminhamentoRequest;
import br.com.sge.modules.colegiado.dto.CriarReuniaoColegiadoRequest;
import br.com.sge.modules.colegiado.entity.ColegiadoEncaminhamento;
import br.com.sge.modules.colegiado.entity.ColegiadoParticipante;
import br.com.sge.modules.colegiado.entity.ColegiadoReuniao;
import br.com.sge.modules.colegiado.entity.StatusEncaminhamentoColegiado;
import br.com.sge.modules.colegiado.entity.StatusReuniaoColegiado;
import br.com.sge.modules.colegiado.repository.ColegiadoEncaminhamentoRepository;
import br.com.sge.modules.colegiado.repository.ColegiadoParticipanteRepository;
import br.com.sge.modules.colegiado.repository.ColegiadoReuniaoRepository;
import br.com.sge.modules.convivencia.service.OcorrenciaService;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.modules.relatorios.service.RelatorioService;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ColegiadoService {

    private final ColegiadoReuniaoRepository reuniaoRepository;
    private final ColegiadoParticipanteRepository participanteRepository;
    private final ColegiadoEncaminhamentoRepository encaminhamentoRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RelatorioService relatorioService;
    private final OcorrenciaService ocorrenciaService;
    private final NotificacaoService notificacaoService;

    public ColegiadoService(
            ColegiadoReuniaoRepository reuniaoRepository,
            ColegiadoParticipanteRepository participanteRepository,
            ColegiadoEncaminhamentoRepository encaminhamentoRepository,
            TurmaRepository turmaRepository,
            UsuarioRepository usuarioRepository,
            RelatorioService relatorioService,
            OcorrenciaService ocorrenciaService,
            NotificacaoService notificacaoService) {
        this.reuniaoRepository = reuniaoRepository;
        this.participanteRepository = participanteRepository;
        this.encaminhamentoRepository = encaminhamentoRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
        this.relatorioService = relatorioService;
        this.ocorrenciaService = ocorrenciaService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarReunioes(UUID turmaId) {
        return reuniaoRepository.findAllComTurma(turmaId).stream().map(this::toReuniaoMapResumo).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterReuniao(UUID id) {
        ColegiadoReuniao reuniao = reuniaoRepository
                .findDetalhadaById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reuniao nao encontrada"));
        Map<String, Object> m = toReuniaoMapDetalhe(reuniao);
        m.put(
                "participantes",
                participanteRepository.findByReuniaoId(id).stream().map(this::toParticipanteMap).toList());
        m.put(
                "encaminhamentos",
                encaminhamentoRepository.findByReuniaoId(id).stream().map(this::toEncaminhamentoMap).toList());
        return m;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> painelDados(UUID reuniaoId) {
        ColegiadoReuniao reuniao = reuniaoRepository
                .findDetalhadaById(reuniaoId)
                .orElseThrow(() -> new IllegalArgumentException("Reuniao nao encontrada"));
        if (reuniao.getTurma() == null) {
            Map<String, Object> vazio = new LinkedHashMap<>();
            vazio.put("mensagem", "Selecione uma turma na reuniao para carregar indicadores.");
            vazio.put("alunosEmRiscoNota", List.of());
            vazio.put("alunosEmRiscoFrequencia", List.of());
            vazio.put("ocorrenciasRecentes", List.of());
            return vazio;
        }
        UUID turmaId = reuniao.getTurma().getId();
        Map<String, Object> desempenho = relatorioService.desempenhoTurma(turmaId);
        Map<String, Object> frequencia = relatorioService.frequenciaTurma(turmaId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alunosNota = (List<Map<String, Object>>) desempenho.get("alunos");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alunosFreq = (List<Map<String, Object>>) frequencia.get("alunos");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("turmaId", turmaId);
        out.put("turmaNome", reuniao.getTurma().getNome());
        out.put("mediaTurma", desempenho.get("mediaTurma"));
        out.put(
                "alunosEmRiscoNota",
                alunosNota.stream().filter(a -> Boolean.TRUE.equals(a.get("emRisco"))).toList());
        out.put(
                "alunosEmRiscoFrequencia",
                alunosFreq.stream().filter(a -> Boolean.TRUE.equals(a.get("emRisco"))).toList());
        out.put("ocorrenciasRecentes", ocorrenciaService.listarPorTurma(turmaId).stream().limit(15).toList());
        out.put("encaminhamentosPendentes", listarEncaminhamentosPendentes(turmaId));
        return out;
    }

    @Transactional
    public Map<String, Object> criarReuniao(CriarReuniaoColegiadoRequest req) {
        ColegiadoReuniao reuniao = new ColegiadoReuniao();
        reuniao.setTitulo(req.titulo().trim());
        reuniao.setTipo(req.tipo() != null && !req.tipo().isBlank() ? req.tipo().trim().toUpperCase() : "PEDAGOGICO");
        reuniao.setDataReuniao(req.dataReuniao());
        reuniao.setHoraReuniao(parseHora(req.horaReuniao()));
        reuniao.setPauta(trimOrNull(req.pauta()));
        reuniao.setStatus(StatusReuniaoColegiado.AGENDADA);

        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            reuniao.setTurma(turma);
        }

        ColegiadoReuniao saved = reuniaoRepository.save(reuniao);
        salvarParticipantes(saved, req.participanteUsuarioIds());

        notificarParticipantes(
                saved,
                TipoNotificacao.COLEGIADO_REUNIAO,
                "Reuniao de colegiado agendada",
                saved.getTitulo() + " em " + saved.getDataReuniao());

        return obterReuniao(saved.getId());
    }

    @Transactional
    public Map<String, Object> atualizarReuniao(UUID id, AtualizarReuniaoColegiadoRequest req) {
        ColegiadoReuniao reuniao = reuniaoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reuniao nao encontrada"));

        if (req.titulo() != null && !req.titulo().isBlank()) {
            reuniao.setTitulo(req.titulo().trim());
        }
        if (req.tipo() != null && !req.tipo().isBlank()) {
            reuniao.setTipo(req.tipo().trim().toUpperCase());
        }
        if (req.dataReuniao() != null) {
            reuniao.setDataReuniao(req.dataReuniao());
        }
        if (req.horaReuniao() != null) {
            reuniao.setHoraReuniao(parseHora(req.horaReuniao()));
        }
        if (req.pauta() != null) {
            reuniao.setPauta(trimOrNull(req.pauta()));
        }
        if (req.ataTexto() != null) {
            reuniao.setAtaTexto(trimOrNull(req.ataTexto()));
        }
        if (req.status() != null && !req.status().isBlank()) {
            StatusReuniaoColegiado status = StatusReuniaoColegiado.valueOf(req.status().trim().toUpperCase());
            reuniao.setStatus(status);
            if (status == StatusReuniaoColegiado.REALIZADA && reuniao.getConcluidaEm() == null) {
                reuniao.setConcluidaEm(Instant.now());
            }
        }

        reuniaoRepository.save(reuniao);
        return obterReuniao(id);
    }

    @Transactional
    public Map<String, Object> concluirReuniao(UUID id, String ataTexto) {
        ColegiadoReuniao reuniao = reuniaoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reuniao nao encontrada"));
        reuniao.setStatus(StatusReuniaoColegiado.REALIZADA);
        reuniao.setConcluidaEm(Instant.now());
        if (ataTexto != null && !ataTexto.isBlank()) {
            reuniao.setAtaTexto(ataTexto.trim());
        } else if (reuniao.getAtaTexto() == null || reuniao.getAtaTexto().isBlank()) {
            reuniao.setAtaTexto(gerarAtaBasica(reuniao));
        }
        reuniaoRepository.save(reuniao);

        notificarParticipantes(
                reuniao,
                TipoNotificacao.COLEGIADO_REUNIAO,
                "Ata de colegiado disponivel",
                reuniao.getTitulo());

        return obterReuniao(id);
    }

    @Transactional
    public Map<String, Object> criarEncaminhamento(UUID reuniaoId, CriarEncaminhamentoRequest req) {
        ColegiadoReuniao reuniao = reuniaoRepository
                .findById(reuniaoId)
                .orElseThrow(() -> new IllegalArgumentException("Reuniao nao encontrada"));

        ColegiadoEncaminhamento enc = new ColegiadoEncaminhamento();
        enc.setReuniao(reuniao);
        enc.setDescricao(req.descricao().trim());
        enc.setPrazo(req.prazo());
        enc.setStatus(StatusEncaminhamentoColegiado.PENDENTE);

        if (req.responsavelUsuarioId() != null) {
            Usuario resp = usuarioRepository
                    .findById(req.responsavelUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Responsavel nao encontrado"));
            enc.setResponsavelUsuario(resp);
            enc.setResponsavelNome(
                    resp.getPessoa() != null ? resp.getPessoa().getNome() : resp.getEmail());
            notificacaoService.criar(
                    resp,
                    TipoNotificacao.COLEGIADO_ENCAMINHAMENTO,
                    "Encaminhamento de colegiado",
                    enc.getDescricao(),
                    "/coordenacao",
                    reuniaoId);
        } else if (req.responsavelNome() != null && !req.responsavelNome().isBlank()) {
            enc.setResponsavelNome(req.responsavelNome().trim());
        }

        ColegiadoEncaminhamento saved = encaminhamentoRepository.save(enc);
        return toEncaminhamentoMap(saved);
    }

    @Transactional
    public Map<String, Object> atualizarEncaminhamento(UUID id, AtualizarEncaminhamentoRequest req) {
        ColegiadoEncaminhamento enc = encaminhamentoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encaminhamento nao encontrado"));
        if (req.status() == null || req.status().isBlank()) {
            throw new IllegalArgumentException("Status obrigatorio");
        }
        StatusEncaminhamentoColegiado status =
                StatusEncaminhamentoColegiado.valueOf(req.status().trim().toUpperCase());
        enc.setStatus(status);
        if (status == StatusEncaminhamentoColegiado.CONCLUIDO) {
            enc.setConcluidoEm(Instant.now());
        }
        return toEncaminhamentoMap(encaminhamentoRepository.save(enc));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarEncaminhamentosPendentes(UUID turmaId) {
        return encaminhamentoRepository
                .findPendentes(StatusEncaminhamentoColegiado.PENDENTE, turmaId)
                .stream()
                .map(this::toEncaminhamentoMap)
                .toList();
    }

    private void salvarParticipantes(ColegiadoReuniao reuniao, List<UUID> usuarioIds) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            return;
        }
        Set<UUID> unicos = new HashSet<>(usuarioIds);
        for (UUID usuarioId : unicos) {
            Usuario usuario = usuarioRepository
                    .findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Participante nao encontrado: " + usuarioId));
            ColegiadoParticipante p = new ColegiadoParticipante();
            p.setReuniao(reuniao);
            p.setUsuario(usuario);
            p.setNomeExibicao(
                    usuario.getPessoa() != null ? usuario.getPessoa().getNome() : usuario.getEmail());
            p.setPerfil(usuario.getPerfil().name());
            participanteRepository.save(p);
        }
    }

    private void notificarParticipantes(
            ColegiadoReuniao reuniao, TipoNotificacao tipo, String titulo, String mensagem) {
        List<ColegiadoParticipante> participantes = participanteRepository.findByReuniaoId(reuniao.getId());
        Set<UUID> enviados = new HashSet<>();
        for (ColegiadoParticipante p : participantes) {
            if (p.getUsuario() != null && enviados.add(p.getUsuario().getId())) {
                notificacaoService.criar(
                        p.getUsuario(), tipo, titulo, mensagem, "/coordenacao", reuniao.getId());
            }
        }
        notificarPorPerfil(PerfilUsuario.COORDENADOR, tipo, titulo, mensagem, reuniao.getId());
        notificarPorPerfil(PerfilUsuario.DIRETOR, tipo, titulo, mensagem, reuniao.getId());
    }

    private void notificarPorPerfil(
            PerfilUsuario perfil, TipoNotificacao tipo, String titulo, String mensagem, UUID refId) {
        notificacaoService.notificarPorPerfil(perfil, tipo, titulo, mensagem, "/coordenacao", refId);
    }

    private String gerarAtaBasica(ColegiadoReuniao reuniao) {
        StringBuilder sb = new StringBuilder();
        sb.append("ATA DE REUNIAO DE COLEGIADO\n\n");
        sb.append("Titulo: ").append(reuniao.getTitulo()).append("\n");
        sb.append("Data: ").append(reuniao.getDataReuniao()).append("\n");
        if (reuniao.getTurma() != null) {
            sb.append("Turma: ").append(reuniao.getTurma().getNome()).append("\n");
        }
        if (reuniao.getPauta() != null) {
            sb.append("\nPauta:\n").append(reuniao.getPauta()).append("\n");
        }
        List<Map<String, Object>> encs = encaminhamentoRepository.findByReuniaoId(reuniao.getId()).stream()
                .map(this::toEncaminhamentoMap)
                .toList();
        if (!encs.isEmpty()) {
            sb.append("\nEncaminhamentos:\n");
            for (Map<String, Object> e : encs) {
                sb.append("- ")
                        .append(e.get("descricao"))
                        .append(" (resp.: ")
                        .append(e.get("responsavelNome"))
                        .append(")\n");
            }
        }
        return sb.toString();
    }

    private LocalTime parseHora(String hora) {
        if (hora == null || hora.isBlank()) {
            return null;
        }
        return LocalTime.parse(hora.length() == 5 ? hora : hora.substring(0, 5));
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Map<String, Object> toReuniaoMapResumo(ColegiadoReuniao r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("titulo", r.getTitulo());
        m.put("tipo", r.getTipo());
        m.put("dataReuniao", r.getDataReuniao().toString());
        m.put("horaReuniao", r.getHoraReuniao() != null ? r.getHoraReuniao().toString().substring(0, 5) : null);
        m.put("status", r.getStatus().name());
        if (r.getTurma() != null) {
            m.put("turmaId", r.getTurma().getId());
            m.put("turmaNome", r.getTurma().getNome());
            if (r.getTurma().getSerie() != null) {
                m.put("serieNome", r.getTurma().getSerie().getNome());
            }
        }
        return m;
    }

    private Map<String, Object> toReuniaoMapDetalhe(ColegiadoReuniao r) {
        Map<String, Object> m = toReuniaoMapResumo(r);
        m.put("pauta", r.getPauta());
        m.put("ataTexto", r.getAtaTexto());
        m.put("criadoEm", r.getCriadoEm().toString());
        m.put("concluidaEm", r.getConcluidaEm() != null ? r.getConcluidaEm().toString() : null);
        return m;
    }

    private Map<String, Object> toParticipanteMap(ColegiadoParticipante p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("usuarioId", p.getUsuario() != null ? p.getUsuario().getId() : null);
        m.put("nomeExibicao", p.getNomeExibicao());
        m.put("perfil", p.getPerfil());
        return m;
    }

    private Map<String, Object> toEncaminhamentoMap(ColegiadoEncaminhamento e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("reuniaoId", e.getReuniao().getId());
        m.put("descricao", e.getDescricao());
        m.put(
                "responsavelUsuarioId",
                e.getResponsavelUsuario() != null ? e.getResponsavelUsuario().getId() : null);
        m.put("responsavelNome", e.getResponsavelNome());
        m.put("prazo", e.getPrazo() != null ? e.getPrazo().toString() : null);
        m.put("status", e.getStatus().name());
        m.put("criadoEm", e.getCriadoEm().toString());
        m.put("concluidoEm", e.getConcluidoEm() != null ? e.getConcluidoEm().toString() : null);
        if (e.getReuniao().getTurma() != null) {
            m.put("turmaId", e.getReuniao().getTurma().getId());
        }
        return m;
    }
}
