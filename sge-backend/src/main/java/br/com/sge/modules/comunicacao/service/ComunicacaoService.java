package br.com.sge.modules.comunicacao.service;

import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.comunicacao.dto.AtualizarComunicadoRequest;
import br.com.sge.modules.comunicacao.dto.AtualizarEventoAgendaRequest;
import br.com.sge.modules.comunicacao.dto.CriarCardapioRequest;
import br.com.sge.modules.comunicacao.dto.CriarComunicadoRequest;
import br.com.sge.modules.comunicacao.dto.CriarEventoAgendaRequest;
import br.com.sge.modules.comunicacao.entity.Cardapio;
import br.com.sge.modules.comunicacao.entity.Comunicado;
import br.com.sge.modules.comunicacao.entity.EventoAgenda;
import br.com.sge.modules.comunicacao.repository.CardapioRepository;
import br.com.sge.modules.comunicacao.repository.ComunicadoRepository;
import br.com.sge.modules.comunicacao.repository.EventoAgendaRepository;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunicacaoService {

    private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

    private final ComunicadoRepository comunicadoRepository;
    private final CardapioRepository cardapioRepository;
    private final EventoAgendaRepository eventoAgendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final NotificacaoService notificacaoService;

    public ComunicacaoService(
            ComunicadoRepository comunicadoRepository,
            CardapioRepository cardapioRepository,
            EventoAgendaRepository eventoAgendaRepository,
            UsuarioRepository usuarioRepository,
            TurmaRepository turmaRepository,
            NotificacaoService notificacaoService) {
        this.comunicadoRepository = comunicadoRepository;
        this.cardapioRepository = cardapioRepository;
        this.eventoAgendaRepository = eventoAgendaRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarComunicados(String audiencia, UUID turmaId) {
        List<Comunicado> lista;
        if (audiencia == null || audiencia.isBlank()) {
            lista = comunicadoRepository.findAllDetalhados();
        } else if (turmaId != null) {
            lista = comunicadoRepository.findVisiveisParaAudienciaETurma(audiencia.trim().toUpperCase(), turmaId);
        } else {
            lista = comunicadoRepository.findVisiveisParaAudiencia(audiencia.trim().toUpperCase());
        }
        return lista.stream().map(this::toComunicadoMap).toList();
    }

    @Transactional
    public Map<String, Object> criarComunicado(CriarComunicadoRequest req, String emailAutor) {
        Usuario autor = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(emailAutor)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        Comunicado c = new Comunicado();
        c.setTitulo(req.titulo().trim());
        c.setConteudo(req.conteudo().trim());
        c.setPublicadoPor(autor);
        c.setPublicadoEm(Instant.now());
        c.setVisivelPara(req.visivelPara().trim().toUpperCase());

        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            c.setTurma(turma);
        }

        Comunicado saved = comunicadoRepository.save(c);
        notificacaoService.notificarComunicado(
                saved.getVisivelPara(),
                saved.getTurma() != null ? saved.getTurma().getId() : null,
                saved.getTitulo(),
                saved.getId());
        return toComunicadoMap(saved);
    }

    @Transactional
    public Map<String, Object> atualizarComunicado(UUID id, AtualizarComunicadoRequest req) {
        Comunicado c = comunicadoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunicado nao encontrado"));
        c.setTitulo(req.titulo().trim());
        c.setConteudo(req.conteudo().trim());
        c.setVisivelPara(req.visivelPara().trim().toUpperCase());
        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            c.setTurma(turma);
        } else {
            c.setTurma(null);
        }
        return toComunicadoMap(comunicadoRepository.save(c));
    }

    @Transactional
    public void excluirComunicado(UUID id) {
        if (!comunicadoRepository.existsById(id)) {
            throw new IllegalArgumentException("Comunicado nao encontrado");
        }
        comunicadoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarCardapio(LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now(ZONA_BR);
        return cardapioRepository.findByDataRefeicao(dia).stream()
                .map(this::toCardapioMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> criarCardapio(CriarCardapioRequest req, String emailNutricionista) {
        Usuario nutri = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(emailNutricionista)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        Cardapio c = new Cardapio();
        c.setDataRefeicao(req.dataRefeicao());
        c.setTipoRefeicao(req.tipoRefeicao());
        c.setDescricao(req.descricao().trim());
        c.setCalorias(req.calorias());
        c.setNutricionista(nutri);

        return toCardapioMap(cardapioRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAgenda(Instant inicio, Instant fim, UUID turmaId) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Parametros inicio e fim sao obrigatorios");
        }
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("fim deve ser posterior a inicio");
        }

        List<EventoAgenda> eventos = turmaId != null
                ? eventoAgendaRepository.findNoPeriodoParaTurma(inicio, fim, turmaId)
                : eventoAgendaRepository.findNoPeriodo(inicio, fim);

        return eventos.stream().map(this::toEventoMap).toList();
    }

    @Transactional
    public Map<String, Object> criarEventoAgenda(CriarEventoAgendaRequest req) {
        EventoAgenda e = new EventoAgenda();
        e.setTitulo(req.titulo().trim());
        e.setDescricao(req.descricao());
        e.setDataInicio(req.dataInicio());
        e.setDataFim(req.dataFim());
        e.setTipo(req.tipo());

        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            e.setTurma(turma);
        }

        return toEventoMap(eventoAgendaRepository.save(e));
    }

    @Transactional
    public Map<String, Object> atualizarEventoAgenda(UUID id, AtualizarEventoAgendaRequest req) {
        EventoAgenda e = eventoAgendaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento nao encontrado"));
        e.setTitulo(req.titulo().trim());
        e.setDescricao(req.descricao());
        e.setDataInicio(req.dataInicio());
        e.setDataFim(req.dataFim());
        e.setTipo(req.tipo());
        if (req.turmaId() != null) {
            Turma turma = turmaRepository
                    .findById(req.turmaId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
            e.setTurma(turma);
        } else {
            e.setTurma(null);
        }
        return toEventoMap(eventoAgendaRepository.save(e));
    }

    @Transactional
    public void excluirEventoAgenda(UUID id) {
        if (!eventoAgendaRepository.existsById(id)) {
            throw new IllegalArgumentException("Evento nao encontrado");
        }
        eventoAgendaRepository.deleteById(id);
    }

    @Transactional
    public void excluirCardapio(UUID id) {
        if (!cardapioRepository.existsById(id)) {
            throw new IllegalArgumentException("Item de cardapio nao encontrado");
        }
        cardapioRepository.deleteById(id);
    }

    private Map<String, Object> toComunicadoMap(Comunicado c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("titulo", c.getTitulo());
        m.put("conteudo", c.getConteudo());
        m.put("publicadoEm", c.getPublicadoEm().toString());
        m.put("visivelPara", c.getVisivelPara());
        m.put("turmaId", c.getTurma() != null ? c.getTurma().getId() : null);
        m.put("turmaNome", c.getTurma() != null ? c.getTurma().getNome() : null);
        if (c.getPublicadoPor() != null && c.getPublicadoPor().getPessoa() != null) {
            m.put("publicadoPorNome", c.getPublicadoPor().getPessoa().getNome());
        } else {
            m.put("publicadoPorNome", null);
        }
        return m;
    }

    private Map<String, Object> toCardapioMap(Cardapio c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("dataRefeicao", c.getDataRefeicao().toString());
        m.put("tipoRefeicao", c.getTipoRefeicao().name());
        m.put("descricao", c.getDescricao());
        m.put("calorias", c.getCalorias());
        if (c.getNutricionista() != null && c.getNutricionista().getPessoa() != null) {
            m.put("nutricionistaNome", c.getNutricionista().getPessoa().getNome());
        } else {
            m.put("nutricionistaNome", null);
        }
        return m;
    }

    private Map<String, Object> toEventoMap(EventoAgenda e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("titulo", e.getTitulo());
        m.put("descricao", e.getDescricao());
        m.put("dataInicio", e.getDataInicio().toString());
        m.put("dataFim", e.getDataFim() != null ? e.getDataFim().toString() : null);
        m.put("tipo", e.getTipo() != null ? e.getTipo().name() : null);
        m.put("turmaId", e.getTurma() != null ? e.getTurma().getId() : null);
        m.put("turmaNome", e.getTurma() != null ? e.getTurma().getNome() : null);
        return m;
    }
}
