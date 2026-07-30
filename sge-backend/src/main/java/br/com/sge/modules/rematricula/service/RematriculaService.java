package br.com.sge.modules.rematricula.service;

import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.ResponsavelRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.notificacoes.entity.TipoNotificacao;
import br.com.sge.modules.notificacoes.service.NotificacaoService;
import br.com.sge.modules.rematricula.dto.AlunoRematriculaPortalDto;
import br.com.sge.modules.rematricula.dto.AtualizarRematriculaConfigRequest;
import br.com.sge.modules.rematricula.dto.CampoFormularioDto;
import br.com.sge.modules.rematricula.dto.CampoRevisaoDto;
import br.com.sge.modules.rematricula.dto.FormularioRematriculaDto;
import br.com.sge.modules.rematricula.dto.RematriculaConfigResponse;
import br.com.sge.modules.rematricula.dto.RematriculaPortalResponse;
import br.com.sge.modules.rematricula.dto.RematriculaRevisaoResponse;
import br.com.sge.modules.rematricula.dto.RematriculaSubmissaoResumo;
import br.com.sge.modules.rematricula.dto.SalvarRespostasRequest;
import br.com.sge.modules.rematricula.dto.SecaoFormularioDto;
import br.com.sge.modules.rematricula.dto.SecaoRevisaoDto;
import br.com.sge.modules.rematricula.entity.RematriculaConfig;
import br.com.sge.modules.rematricula.entity.RematriculaSubmissao;
import br.com.sge.modules.rematricula.entity.StatusRematriculaSubmissao;
import br.com.sge.modules.rematricula.repository.RematriculaConfigRepository;
import br.com.sge.modules.rematricula.repository.RematriculaSubmissaoRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RematriculaService {

    private final RematriculaConfigRepository configRepository;
    private final RematriculaSubmissaoRepository submissaoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final RematriculaFormularioMapper formularioMapper;
    private final RematriculaPdfImportService pdfImportService;
    private final RematriculaPdfService pdfService;

    public RematriculaService(
            RematriculaConfigRepository configRepository,
            RematriculaSubmissaoRepository submissaoRepository,
            ResponsavelRepository responsavelRepository,
            AlunoRepository alunoRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService,
            RematriculaFormularioMapper formularioMapper,
            RematriculaPdfImportService pdfImportService,
            RematriculaPdfService pdfService) {
        this.configRepository = configRepository;
        this.submissaoRepository = submissaoRepository;
        this.responsavelRepository = responsavelRepository;
        this.alunoRepository = alunoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.formularioMapper = formularioMapper;
        this.pdfImportService = pdfImportService;
        this.pdfService = pdfService;
    }

    @Transactional(readOnly = true)
    public RematriculaConfigResponse obterConfig() {
        return toConfigResponse(obterOuCriarConfig());
    }

    @Transactional
    public RematriculaConfigResponse atualizarConfig(AtualizarRematriculaConfigRequest request) {
        RematriculaConfig config = obterOuCriarConfig();
        if (request.titulo() != null && !request.titulo().isBlank()) {
            config.setTitulo(request.titulo().trim());
        }
        if (request.habilitada() != null) {
            config.setHabilitada(request.habilitada());
            if (Boolean.TRUE.equals(request.habilitada()) && config.getPublicadoEm() == null) {
                config.setPublicadoEm(Instant.now());
            }
        }
        if (request.formulario() != null) {
            validarFormulario(request.formulario());
            config.setFormularioJson(formularioMapper.toJson(request.formulario()));
        }
        config.setAtualizadoEm(Instant.now());
        return toConfigResponse(configRepository.save(config));
    }

    @Transactional
    public RematriculaConfigResponse uploadModeloPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo PDF obrigatorio");
        }
        String nome = file.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Envie um arquivo PDF");
        }
        byte[] conteudo;
        try {
            conteudo = file.getBytes();
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Falha ao ler o PDF");
        }
        List<String> sugestoes = pdfImportService.extrairSugestoes(conteudo);

        RematriculaConfig config = obterOuCriarConfig();
        config.setPdfModeloNome(nome);
        config.setPdfModeloConteudo(conteudo);
        config.setSugestoesExtracaoJson(formularioMapper.sugestoesToJson(sugestoes));
        config.setAtualizadoEm(Instant.now());
        configRepository.save(config);

        return toConfigResponse(config);
    }

    @Transactional(readOnly = true)
    public byte[] downloadModeloPdf() {
        RematriculaConfig config = obterOuCriarConfig();
        if (config.getPdfModeloConteudo() == null || config.getPdfModeloConteudo().length == 0) {
            throw new IllegalArgumentException("Nenhum PDF modelo cadastrado");
        }
        return config.getPdfModeloConteudo();
    }

    @Transactional(readOnly = true)
    public String nomeModeloPdf() {
        RematriculaConfig config = obterOuCriarConfig();
        return config.getPdfModeloNome() != null ? config.getPdfModeloNome() : "modelo-rematricula.pdf";
    }

    @Transactional(readOnly = true)
    public RematriculaPortalResponse portalParaResponsavel(String email) {
        RematriculaConfig config = obterOuCriarConfig();
        FormularioRematriculaDto formulario = formularioMapper.parseFormulario(config.getFormularioJson());

        Responsavel responsavel = responsavelExigido(email);

        List<Aluno> filhos = alunoRepository.findByResponsavelId(responsavel.getId());
        List<UUID> alunoIds = filhos.stream().map(Aluno::getId).toList();
        Map<UUID, RematriculaSubmissao> submissoes = config.getAnoLetivo() == null
                ? Map.of()
                : submissaoRepository
                        .findByAlunoIdsAndAnoLetivoId(alunoIds, config.getAnoLetivo().getId())
                        .stream()
                        .collect(Collectors.toMap(s -> s.getAluno().getId(), s -> s));

        List<AlunoRematriculaPortalDto> alunos = filhos.stream()
                .map(aluno -> {
                    RematriculaSubmissao sub = submissoes.get(aluno.getId());
                    String turmaNome = aluno.getTurma() != null ? aluno.getTurma().getNome() : null;
                    return new AlunoRematriculaPortalDto(
                            aluno.getId().toString(),
                            aluno.getPessoa().getNome(),
                            turmaNome,
                            sub != null ? sub.getStatus().name() : null,
                            sub != null ? formularioMapper.parseRespostas(sub.getRespostasJson()) : Map.of());
                })
                .toList();

        return new RematriculaPortalResponse(
                Boolean.TRUE.equals(config.getHabilitada()),
                config.getTitulo(),
                formulario,
                alunos);
    }

    @Transactional
    public Map<String, Object> salvarRascunho(UUID alunoId, String email, SalvarRespostasRequest request) {
        RematriculaConfig config = exigirRematriculaHabilitada();
        Responsavel responsavel = responsavelExigido(email);
        Aluno aluno = alunoDoResponsavel(alunoId, responsavel);

        RematriculaSubmissao submissao = obterOuCriarSubmissao(aluno, responsavel, config);
        if (submissao.getStatus() != StatusRematriculaSubmissao.RASCUNHO) {
            throw new IllegalArgumentException("Formulario ja enviado e nao pode ser alterado");
        }
        submissao.setRespostasJson(formularioMapper.respostasToJson(request.respostas()));
        submissao.setAtualizadoEm(Instant.now());
        submissaoRepository.save(submissao);
        return formularioMapper.parseRespostas(submissao.getRespostasJson());
    }

    @Transactional
    public RematriculaRevisaoResponse revisar(UUID alunoId, String email, SalvarRespostasRequest request) {
        RematriculaConfig config = exigirRematriculaHabilitada();
        Responsavel responsavel = responsavelExigido(email);
        Aluno aluno = alunoDoResponsavel(alunoId, responsavel);

        FormularioRematriculaDto formulario = formularioMapper.parseFormulario(config.getFormularioJson());
        Map<String, Object> respostas = request.respostas() != null ? request.respostas() : Map.of();
        List<String> erros = validarRespostas(formulario, respostas);

        List<SecaoRevisaoDto> secoes = new ArrayList<>();
        for (SecaoFormularioDto secao : formulario.secoes()) {
            List<CampoRevisaoDto> campos = secao.campos().stream()
                    .map(campo -> new CampoRevisaoDto(
                            campo.rotulo(),
                            formatarValorExibicao(campo, respostas.get(campo.id())),
                            campo.id()))
                    .toList();
            secoes.add(new SecaoRevisaoDto(secao.titulo(), campos));
        }

        if (erros.isEmpty()) {
            RematriculaSubmissao submissao = obterOuCriarSubmissao(aluno, responsavel, config);
            if (submissao.getStatus() == StatusRematriculaSubmissao.RASCUNHO) {
                submissao.setRespostasJson(formularioMapper.respostasToJson(respostas));
                submissao.setAtualizadoEm(Instant.now());
                submissaoRepository.save(submissao);
            }
        }

        return new RematriculaRevisaoResponse(
                aluno.getId().toString(),
                aluno.getPessoa().getNome(),
                config.getTitulo(),
                secoes,
                erros);
    }

    @Transactional
    public RematriculaSubmissaoResumo confirmarEnvio(UUID alunoId, String email) {
        RematriculaConfig config = exigirRematriculaHabilitada();
        Responsavel responsavel = responsavelExigido(email);
        Aluno aluno = alunoDoResponsavel(alunoId, responsavel);

        RematriculaSubmissao submissao = submissaoRepository
                .findByAlunoIdAndAnoLetivoId(aluno.getId(), config.getAnoLetivo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Salve o formulario antes de confirmar"));

        Aluno alunoDetalhe = alunoRepository
                .findDetalhadoById(aluno.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        submissao.setAluno(alunoDetalhe);

        if (submissao.getStatus() != StatusRematriculaSubmissao.RASCUNHO) {
            throw new IllegalArgumentException("Formulario ja foi enviado");
        }

        FormularioRematriculaDto formulario = formularioMapper.parseFormulario(config.getFormularioJson());
        Map<String, Object> respostas = formularioMapper.parseRespostas(submissao.getRespostasJson());
        List<String> erros = validarRespostas(formulario, respostas);
        if (!erros.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", erros));
        }

        byte[] pdf = pdfService.gerarPdfPreenchido(config.getTitulo(), submissao, formulario);
        submissao.setPdfPreenchido(pdf);
        submissao.setStatus(StatusRematriculaSubmissao.ENVIADO);
        submissao.setEnviadoEm(Instant.now());
        submissao.setAtualizadoEm(Instant.now());
        submissaoRepository.save(submissao);

        String alunoNome = aluno.getPessoa().getNome();
        notificacaoService.notificarPorPerfil(
                PerfilUsuario.SECRETARIA,
                TipoNotificacao.REMATRICULA_ENVIADA,
                "Rematricula enviada",
                "Formulario de rematricula de " + alunoNome + " aguarda validacao da secretaria.",
                "/secretaria/rematricula",
                submissao.getId());
        notificacaoService.notificarPorPerfil(
                PerfilUsuario.ADMIN,
                TipoNotificacao.REMATRICULA_ENVIADA,
                "Rematricula enviada",
                "Formulario de rematricula de " + alunoNome + " aguarda validacao.",
                "/secretaria/rematricula",
                submissao.getId());

        return toSubmissaoResumo(submissao);
    }

    @Transactional(readOnly = true)
    public List<RematriculaSubmissaoResumo> listarPendentesSecretaria() {
        return submissaoRepository.findDetalhadasByStatus(StatusRematriculaSubmissao.ENVIADO).stream()
                .map(this::toSubmissaoResumo)
                .toList();
    }

    @Transactional
    public RematriculaSubmissaoResumo validarSecretaria(UUID submissaoId) {
        RematriculaSubmissao submissao = submissaoRepository
                .findDetalhadaById(submissaoId)
                .orElseThrow(() -> new IllegalArgumentException("Submissao nao encontrada"));
        if (submissao.getStatus() != StatusRematriculaSubmissao.ENVIADO) {
            throw new IllegalArgumentException("Submissao nao esta aguardando validacao");
        }
        submissao.setStatus(StatusRematriculaSubmissao.VALIDADO_SECRETARIA);
        submissao.setValidadoSecretariaEm(Instant.now());
        submissao.setAtualizadoEm(Instant.now());
        return toSubmissaoResumo(submissaoRepository.save(submissao));
    }

    @Transactional(readOnly = true)
    public RematriculaRevisaoResponse detalheSubmissao(UUID submissaoId) {
        RematriculaSubmissao submissao = submissaoRepository
                .findDetalhadaById(submissaoId)
                .orElseThrow(() -> new IllegalArgumentException("Submissao nao encontrada"));
        RematriculaConfig config = obterOuCriarConfig();
        FormularioRematriculaDto formulario = formularioMapper.parseFormulario(config.getFormularioJson());
        Map<String, Object> respostas = formularioMapper.parseRespostas(submissao.getRespostasJson());

        List<SecaoRevisaoDto> secoes = formulario.secoes().stream()
                .map(secao -> new SecaoRevisaoDto(
                        secao.titulo(),
                        secao.campos().stream()
                                .map(campo -> new CampoRevisaoDto(
                                        campo.rotulo(),
                                        formatarValorExibicao(campo, respostas.get(campo.id())),
                                        campo.id()))
                                .toList()))
                .toList();

        return new RematriculaRevisaoResponse(
                submissao.getAluno().getId().toString(),
                submissao.getAluno().getPessoa().getNome(),
                config.getTitulo(),
                secoes,
                List.of());
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdfPreenchido(UUID submissaoId) {
        RematriculaSubmissao submissao = submissaoRepository
                .findDetalhadaById(submissaoId)
                .orElseThrow(() -> new IllegalArgumentException("Submissao nao encontrada"));
        if (submissao.getPdfPreenchido() == null || submissao.getPdfPreenchido().length == 0) {
            throw new IllegalArgumentException("PDF preenchido ainda nao disponivel");
        }
        return submissao.getPdfPreenchido();
    }

    private RematriculaConfig obterOuCriarConfig() {
        return configRepository.findFirstByOrderByAtualizadoEmDesc().orElseGet(() -> {
            RematriculaConfig config = new RematriculaConfig();
            config.setHabilitada(false);
            config.setTitulo("Rematricula");
            config.setFormularioJson("{\"secoes\":[]}");
            config.setAtualizadoEm(Instant.now());
            return configRepository.save(config);
        });
    }

    private RematriculaConfig exigirRematriculaHabilitada() {
        RematriculaConfig config = obterOuCriarConfig();
        if (!Boolean.TRUE.equals(config.getHabilitada())) {
            throw new IllegalArgumentException("Periodo de rematricula nao esta aberto");
        }
        if (config.getAnoLetivo() == null) {
            throw new IllegalStateException("Ano letivo da rematricula nao configurado");
        }
        return config;
    }

    private Responsavel responsavelExigido(String email) {
        var usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        return responsavelRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Apenas responsaveis podem preencher rematricula"));
    }

    private Aluno alunoDoResponsavel(UUID alunoId, Responsavel responsavel) {
        return alunoRepository.findByResponsavelId(responsavel.getId()).stream()
                .filter(a -> a.getId().equals(alunoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao vinculado ao responsavel"));
    }

    private RematriculaSubmissao obterOuCriarSubmissao(
            Aluno aluno, Responsavel responsavel, RematriculaConfig config) {
        return submissaoRepository
                .findByAlunoIdAndAnoLetivoId(aluno.getId(), config.getAnoLetivo().getId())
                .orElseGet(() -> {
                    RematriculaSubmissao sub = new RematriculaSubmissao();
                    sub.setAluno(aluno);
                    sub.setResponsavel(responsavel);
                    sub.setAnoLetivo(config.getAnoLetivo());
                    sub.setStatus(StatusRematriculaSubmissao.RASCUNHO);
                    sub.setRespostasJson("{}");
                    sub.setCriadoEm(Instant.now());
                    sub.setAtualizadoEm(Instant.now());
                    return submissaoRepository.save(sub);
                });
    }

    private void validarFormulario(FormularioRematriculaDto formulario) {
        if (formulario.secoes() == null || formulario.secoes().isEmpty()) {
            throw new IllegalArgumentException("Formulario precisa ter ao menos uma secao");
        }
        for (SecaoFormularioDto secao : formulario.secoes()) {
            if (secao.campos() == null || secao.campos().isEmpty()) {
                throw new IllegalArgumentException("Secao '" + secao.titulo() + "' precisa ter campos");
            }
        }
    }

    private List<String> validarRespostas(FormularioRematriculaDto formulario, Map<String, Object> respostas) {
        List<String> erros = new ArrayList<>();
        for (SecaoFormularioDto secao : formulario.secoes()) {
            for (CampoFormularioDto campo : secao.campos()) {
                Object valor = respostas.get(campo.id());
                if (campo.obrigatorio() && isVazio(valor)) {
                    erros.add("Campo obrigatorio: " + campo.rotulo());
                }
            }
        }
        return erros;
    }

    private boolean isVazio(Object valor) {
        if (valor == null) {
            return true;
        }
        if (valor instanceof Boolean) {
            return false;
        }
        return String.valueOf(valor).isBlank();
    }

    private String formatarValorExibicao(CampoFormularioDto campo, Object valor) {
        if (isVazio(valor)) {
            return "-";
        }
        if ("BOOLEAN".equals(campo.tipo())) {
            return Boolean.TRUE.equals(valor) || "true".equalsIgnoreCase(String.valueOf(valor)) ? "Sim" : "Nao";
        }
        return String.valueOf(valor);
    }

    private RematriculaConfigResponse toConfigResponse(RematriculaConfig config) {
        return new RematriculaConfigResponse(
                config.getId().toString(),
                config.getTitulo(),
                Boolean.TRUE.equals(config.getHabilitada()),
                config.getAnoLetivo() != null ? config.getAnoLetivo().getAno() : null,
                config.getAnoLetivo() != null ? config.getAnoLetivo().getId().toString() : null,
                config.getPdfModeloConteudo() != null && config.getPdfModeloConteudo().length > 0,
                config.getPdfModeloNome(),
                formularioMapper.parseFormulario(config.getFormularioJson()),
                formularioMapper.parseSugestoes(config.getSugestoesExtracaoJson()),
                config.getPublicadoEm() != null ? config.getPublicadoEm().toString() : null,
                config.getAtualizadoEm().toString());
    }

    private RematriculaSubmissaoResumo toSubmissaoResumo(RematriculaSubmissao submissao) {
        String turmaNome = null;
        if (submissao.getAluno().getTurma() != null) {
            turmaNome = submissao.getAluno().getTurma().getNome();
        }
        return new RematriculaSubmissaoResumo(
                submissao.getId().toString(),
                submissao.getAluno().getId().toString(),
                submissao.getAluno().getPessoa().getNome(),
                turmaNome,
                submissao.getStatus().name(),
                submissao.getEnviadoEm() != null ? submissao.getEnviadoEm().toString() : null,
                submissao.getValidadoSecretariaEm() != null
                        ? submissao.getValidadoSecretariaEm().toString()
                        : null);
    }
}
