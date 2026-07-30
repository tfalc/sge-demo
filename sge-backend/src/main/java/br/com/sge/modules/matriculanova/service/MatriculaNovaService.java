package br.com.sge.modules.matriculanova.service;

import br.com.sge.modules.cadastro.dto.CriarAlunoRequest;
import br.com.sge.modules.cadastro.dto.VincularResponsavelRequest;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.AnoLetivo;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.AnoLetivoRepository;
import br.com.sge.modules.cadastro.repository.ResponsavelRepository;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.cadastro.service.CadastroService;
import br.com.sge.modules.matriculanova.dto.AtualizarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.dto.CriarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.dto.RejeitarMatriculaProcessoRequest;
import br.com.sge.modules.matriculanova.entity.MatriculaDocumento;
import br.com.sge.modules.matriculanova.entity.MatriculaProcesso;
import br.com.sge.modules.matriculanova.entity.StatusMatriculaProcesso;
import br.com.sge.modules.matriculanova.entity.TipoDocumentoMatricula;
import br.com.sge.modules.matriculanova.repository.MatriculaDocumentoRepository;
import br.com.sge.modules.matriculanova.repository.MatriculaProcessoRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.shared.storage.DocumentStorageService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MatriculaNovaService {

    private static final long MAX_DOC_BYTES = 10 * 1024 * 1024;

    private final MatriculaProcessoRepository processoRepository;
    private final MatriculaDocumentoRepository documentoRepository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final TurmaRepository turmaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final CadastroService cadastroService;
    private final DocumentStorageService documentStorage;
    private final NotificacaoService notificacaoService;

    public MatriculaNovaService(
            MatriculaProcessoRepository processoRepository,
            MatriculaDocumentoRepository documentoRepository,
            AnoLetivoRepository anoLetivoRepository,
            TurmaRepository turmaRepository,
            ResponsavelRepository responsavelRepository,
            UsuarioRepository usuarioRepository,
            AlunoRepository alunoRepository,
            CadastroService cadastroService,
            DocumentStorageService documentStorage,
            NotificacaoService notificacaoService) {
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.anoLetivoRepository = anoLetivoRepository;
        this.turmaRepository = turmaRepository;
        this.responsavelRepository = responsavelRepository;
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.cadastroService = cadastroService;
        this.documentStorage = documentStorage;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAnosLetivos() {
        return anoLetivoRepository.findAllByOrderByAnoDesc().stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("ano", a.getAno());
                    return m;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarProcessos(String statusFiltro) {
        List<MatriculaProcesso> lista;
        if (statusFiltro != null && !statusFiltro.isBlank()) {
            StatusMatriculaProcesso status = StatusMatriculaProcesso.valueOf(statusFiltro);
            lista = processoRepository.findByStatusOrderByCriadoEmDesc(status);
        } else {
            lista = processoRepository.findAllByOrderByCriadoEmDesc();
        }
        return lista.stream().map(this::toResumoMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterProcesso(UUID id) {
        MatriculaProcesso p = processoRepository
                .findDetalhadoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo nao encontrado"));
        Map<String, Object> m = toResumoMap(p);
        m.put("documentos", documentoRepository.findByProcessoIdOrderByEnviadoEmDesc(id).stream()
                .map(this::toDocumentoMap)
                .toList());
        return m;
    }

    @Transactional
    public Map<String, Object> criarProcesso(CriarMatriculaProcessoRequest req) {
        MatriculaProcesso p = new MatriculaProcesso();
        p.setAnoLetivo(resolveAnoLetivo(req.anoLetivoId()));
        p.setTurmaPretendida(resolveTurma(req.turmaPretendidaId()));
        p.setResponsavel(resolveResponsavel(req.responsavelId()));
        aplicarDados(p, req.candidatoNome(), req.matriculaSugerida(), req.responsavelNome(), req.responsavelEmail(),
                req.responsavelTelefone(), req.observacoes());
        p.setCriadoPorUsuario(usuarioLogado());
        p.setStatus(StatusMatriculaProcesso.RASCUNHO);
        return toResumoMap(processoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> atualizarProcesso(UUID id, AtualizarMatriculaProcessoRequest req) {
        MatriculaProcesso p = carregarEditavel(id);
        p.setTurmaPretendida(resolveTurma(req.turmaPretendidaId()));
        p.setResponsavel(resolveResponsavel(req.responsavelId()));
        aplicarDados(p, req.candidatoNome(), req.matriculaSugerida(), req.responsavelNome(), req.responsavelEmail(),
                req.responsavelTelefone(), req.observacoes());
        p.setAtualizadoEm(Instant.now());
        return toResumoMap(processoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> enviar(UUID id) {
        MatriculaProcesso p = carregarEditavel(id);
        p.setStatus(StatusMatriculaProcesso.EM_ANALISE);
        p.setEnviadoEm(Instant.now());
        p.setAtualizadoEm(Instant.now());
        MatriculaProcesso saved = processoRepository.save(p);
        notificacaoService.notificarPorPerfil(
                PerfilUsuario.SECRETARIA,
                TipoNotificacao.MATRICULA_NOVA_ENVIADA,
                "Nova matricula para analise",
                saved.getCandidatoNome() + " aguarda analise.",
                "/secretaria/matricula-nova",
                saved.getId());
        return toResumoMap(saved);
    }

    @Transactional
    public Map<String, Object> aprovar(UUID id) {
        MatriculaProcesso p = carregarEmAnalise(id);
        p.setStatus(StatusMatriculaProcesso.APROVADO);
        p.setAprovadoEm(Instant.now());
        p.setAtualizadoEm(Instant.now());
        return toResumoMap(processoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> rejeitar(UUID id, RejeitarMatriculaProcessoRequest req) {
        MatriculaProcesso p = carregarEmAnalise(id);
        p.setStatus(StatusMatriculaProcesso.REJEITADO);
        p.setMotivoRejeicao(req.motivo().trim());
        p.setRejeitadoEm(Instant.now());
        p.setAtualizadoEm(Instant.now());
        return toResumoMap(processoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> concluir(UUID id) {
        MatriculaProcesso p = processoRepository
                .findDetalhadoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo nao encontrado"));
        if (p.getStatus() != StatusMatriculaProcesso.APROVADO) {
            throw new IllegalArgumentException("Processo precisa estar aprovado para concluir");
        }
        if (p.getTurmaPretendida() == null) {
            throw new IllegalArgumentException("Informe a turma pretendida antes de concluir");
        }
        String matricula = p.getMatriculaSugerida();
        if (matricula == null || matricula.isBlank()) {
            matricula = gerarMatricula(p.getAnoLetivo().getAno());
        }
        Map<String, Object> alunoCriado = cadastroService.criarAluno(new CriarAlunoRequest(
                p.getCandidatoNome(), matricula.trim(), p.getTurmaPretendida().getId()));
        UUID alunoId = (UUID) alunoCriado.get("id");
        if (p.getResponsavel() != null) {
            cadastroService.vincularResponsavelAluno(
                    alunoId, new VincularResponsavelRequest(p.getResponsavel().getId()));
        }
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow();
        p.setAluno(aluno);
        p.setMatriculaSugerida(matricula.trim());
        p.setStatus(StatusMatriculaProcesso.CONCLUIDO);
        p.setConcluidoEm(Instant.now());
        p.setAtualizadoEm(Instant.now());
        return toResumoMap(processoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> uploadDocumento(UUID processoId, TipoDocumentoMatricula tipo, MultipartFile file) {
        MatriculaProcesso p = carregarComDocumentos(processoId);
        if (p.getStatus() == StatusMatriculaProcesso.CONCLUIDO
                || p.getStatus() == StatusMatriculaProcesso.REJEITADO) {
            throw new IllegalArgumentException("Processo encerrado — documentos nao podem ser alterados");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        if (file.getSize() > MAX_DOC_BYTES) {
            throw new IllegalArgumentException("Arquivo excede 10 MB");
        }
        UUID docId = UUID.randomUUID();
        String nome = file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento";
        String contentType =
                file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String storageKey = "matricula-nova/" + processoId + "/" + docId + "/" + sanitizeFilename(nome);
        try {
            documentStorage.store(storageKey, file.getBytes(), contentType);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao salvar arquivo", ex);
        }
        MatriculaDocumento doc = new MatriculaDocumento();
        doc.setProcesso(p);
        doc.setTipo(tipo);
        doc.setNomeArquivo(nome);
        doc.setContentType(contentType);
        doc.setTamanhoBytes(file.getSize());
        doc.setStorageKey(storageKey);
        p.setAtualizadoEm(Instant.now());
        processoRepository.save(p);
        return toDocumentoMap(documentoRepository.save(doc));
    }

    @Transactional(readOnly = true)
    public DocumentoDownload downloadDocumento(UUID processoId, UUID documentoId) {
        MatriculaDocumento doc = documentoRepository
                .findByIdAndProcessoId(documentoId, processoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento nao encontrado"));
        byte[] bytes = documentStorage.load(doc.getStorageKey());
        return new DocumentoDownload(doc.getNomeArquivo(), doc.getContentType(), bytes);
    }

    @Transactional
    public void excluirDocumento(UUID processoId, UUID documentoId) {
        MatriculaProcesso p = carregarComDocumentos(processoId);
        if (p.getStatus() == StatusMatriculaProcesso.CONCLUIDO) {
            throw new IllegalArgumentException("Processo concluido — documentos nao podem ser removidos");
        }
        MatriculaDocumento doc = documentoRepository
                .findByIdAndProcessoId(documentoId, processoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento nao encontrado"));
        documentStorage.delete(doc.getStorageKey());
        documentoRepository.delete(doc);
    }

    public record DocumentoDownload(String filename, String contentType, byte[] bytes) {}

    private MatriculaProcesso carregarEditavel(UUID id) {
        MatriculaProcesso p = processoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo nao encontrado"));
        if (p.getStatus() != StatusMatriculaProcesso.RASCUNHO
                && p.getStatus() != StatusMatriculaProcesso.EM_ANALISE) {
            throw new IllegalArgumentException("Processo nao pode ser editado neste status");
        }
        return p;
    }

    private MatriculaProcesso carregarEmAnalise(UUID id) {
        MatriculaProcesso p = processoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo nao encontrado"));
        if (p.getStatus() != StatusMatriculaProcesso.EM_ANALISE) {
            throw new IllegalArgumentException("Processo nao esta em analise");
        }
        return p;
    }

    private MatriculaProcesso carregarComDocumentos(UUID id) {
        return processoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo nao encontrado"));
    }

    private AnoLetivo resolveAnoLetivo(UUID id) {
        return anoLetivoRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ano letivo nao encontrado"));
    }

    private Turma resolveTurma(UUID id) {
        if (id == null) return null;
        return turmaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
    }

    private Responsavel resolveResponsavel(UUID id) {
        if (id == null) return null;
        return responsavelRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Responsavel nao encontrado"));
    }

    private void aplicarDados(
            MatriculaProcesso p,
            String candidatoNome,
            String matricula,
            String respNome,
            String respEmail,
            String respTel,
            String obs) {
        p.setCandidatoNome(candidatoNome.trim());
        p.setMatriculaSugerida(blankToNull(matricula));
        p.setResponsavelNome(blankToNull(respNome));
        p.setResponsavelEmail(blankToNull(respEmail));
        p.setResponsavelTelefone(blankToNull(respTel));
        p.setObservacoes(blankToNull(obs));
    }

    private String gerarMatricula(int ano) {
        return "MAT" + ano + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(auth.getPrincipal().toString())
                .orElse(null);
    }

    private Map<String, Object> toResumoMap(MatriculaProcesso p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("status", p.getStatus().name());
        m.put("candidatoNome", p.getCandidatoNome());
        m.put("matriculaSugerida", p.getMatriculaSugerida());
        m.put("observacoes", p.getObservacoes());
        m.put("motivoRejeicao", p.getMotivoRejeicao());
        m.put("responsavelNome", p.getResponsavelNome());
        m.put("responsavelEmail", p.getResponsavelEmail());
        m.put("responsavelTelefone", p.getResponsavelTelefone());
        m.put("anoLetivoId", p.getAnoLetivo().getId());
        m.put("anoLetivo", p.getAnoLetivo().getAno());
        if (p.getTurmaPretendida() != null) {
            m.put("turmaPretendidaId", p.getTurmaPretendida().getId());
            m.put("turmaPretendidaNome", p.getTurmaPretendida().getNome());
        }
        if (p.getResponsavel() != null) {
            m.put("responsavelId", p.getResponsavel().getId());
        }
        if (p.getAluno() != null) {
            m.put("alunoId", p.getAluno().getId());
        }
        m.put("enviadoEm", p.getEnviadoEm() != null ? p.getEnviadoEm().toString() : null);
        m.put("aprovadoEm", p.getAprovadoEm() != null ? p.getAprovadoEm().toString() : null);
        m.put("concluidoEm", p.getConcluidoEm() != null ? p.getConcluidoEm().toString() : null);
        m.put("criadoEm", p.getCriadoEm().toString());
        return m;
    }

    private Map<String, Object> toDocumentoMap(MatriculaDocumento d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("tipo", d.getTipo().name());
        m.put("nomeArquivo", d.getNomeArquivo());
        m.put("contentType", d.getContentType());
        m.put("tamanhoBytes", d.getTamanhoBytes());
        m.put("enviadoEm", d.getEnviadoEm().toString());
        return m;
    }
}
