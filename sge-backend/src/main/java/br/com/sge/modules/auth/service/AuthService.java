package br.com.sge.modules.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.sge.config.JwtConfig;
import br.com.sge.config.JwtTokenService;
import br.com.sge.modules.academico.entity.Professor;
import br.com.sge.modules.academico.repository.ProfessorRepository;
import br.com.sge.modules.saude.entity.ProfissionalSaude;
import br.com.sge.modules.saude.repository.ProfissionalSaudeRepository;
import br.com.sge.modules.auth.dto.AtualizarPerfilRequest;
import br.com.sge.modules.auth.dto.AuthTokensResponse;
import br.com.sge.modules.auth.dto.EsqueciSenhaResponse;
import br.com.sge.modules.auth.dto.FilhoResumo;
import br.com.sge.modules.auth.dto.LoginRequest;
import br.com.sge.modules.auth.dto.TrocarSenhaRequest;
import br.com.sge.modules.auth.dto.UserMeResponse;
import br.com.sge.modules.admin.service.PerfilAcessoMenuService;
import br.com.sge.modules.cadastro.entity.Pessoa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.ResponsavelRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtTokenService jwtTokenService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;
    private final PerfilAcessoMenuService perfilAcessoMenuService;

    public AuthService(
            JwtTokenService jwtTokenService,
            JwtConfig jwtConfig,
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            ResponsavelRepository responsavelRepository,
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            ProfissionalSaudeRepository profissionalSaudeRepository,
            PerfilAcessoMenuService perfilAcessoMenuService) {
        this.jwtTokenService = jwtTokenService;
        this.jwtConfig = jwtConfig;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.responsavelRepository = responsavelRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
        this.perfilAcessoMenuService = perfilAcessoMenuService;
    }

    @Transactional(readOnly = true)
    public AuthTokensResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais invalidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }

        String role = usuario.getPerfil().name();
        String accessToken = jwtTokenService.generateAccessToken(usuario.getEmail(), role);
        String refreshToken = jwtTokenService.generateRefreshToken(usuario.getEmail());

        return new AuthTokensResponse(accessToken, refreshToken, "Bearer", jwtConfig.expirationMs() / 1000);
    }

    @Transactional(readOnly = true)
    public AuthTokensResponse refresh(String refreshToken) {
        String subject = jwtTokenService.parseClaims(refreshToken).getSubject();
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(subject)
                .orElseThrow(() -> new IllegalArgumentException("Token invalido"));

        String role = usuario.getPerfil().name();
        String accessToken = jwtTokenService.generateAccessToken(usuario.getEmail(), role);
        String newRefresh = jwtTokenService.generateRefreshToken(usuario.getEmail());
        return new AuthTokensResponse(accessToken, newRefresh, "Bearer", jwtConfig.expirationMs() / 1000);
    }

    @Transactional(readOnly = true)
    public EsqueciSenhaResponse esqueciSenha(String email) {
        String normalized = email != null ? email.trim().toLowerCase() : "";
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("E-mail e obrigatorio");
        }
        boolean existe = usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(normalized).isPresent();
        if (!existe) {
            throw new IllegalArgumentException("E-mail nao cadastrado");
        }
        log.info("[Auth] Recuperacao de senha (simulado) para {} — em producao enviaria e-mail com link", normalized);
        return new EsqueciSenhaResponse(
                normalized,
                "Ambiente local: use a senha definida no cadastro ou admin123 para usuarios de teste.",
                true);
    }

    @Transactional(readOnly = true)
    public UserMeResponse me(String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        String nome = usuario.getPessoa() != null ? usuario.getPessoa().getNome() : usuario.getEmail();
        UUID responsavelId = responsavelRepository
                .findByUsuarioId(usuario.getId())
                .map(Responsavel::getId)
                .orElse(null);

        UUID professorId = professorRepository
                .findByUsuarioId(usuario.getId())
                .map(Professor::getId)
                .orElse(null);

        UUID profissionalSaudeId = profissionalSaudeRepository
                .findByUsuarioId(usuario.getId())
                .map(ProfissionalSaude::getId)
                .orElse(null);

        List<FilhoResumo> filhos = responsavelId != null
                ? alunoRepository.findByResponsavelId(responsavelId).stream()
                        .map(this::toFilhoResumo)
                        .toList()
                : List.of();

        Aluno alunoLogado = alunoRepository.findByUsuarioId(usuario.getId()).orElse(null);
        UUID alunoId = alunoLogado != null ? alunoLogado.getId() : null;
        UUID turmaId = alunoLogado != null && alunoLogado.getTurma() != null
                ? alunoLogado.getTurma().getId()
                : null;
        String turmaNome = alunoLogado != null && alunoLogado.getTurma() != null
                ? alunoLogado.getTurma().getNome()
                : null;
        String telefone = usuario.getPessoa() != null ? usuario.getPessoa().getTelefone() : null;
        List<String> areasMenu = perfilAcessoMenuService.areasHabilitadas(usuario.getPerfil().name());

        return new UserMeResponse(
                usuario.getId(),
                nome,
                usuario.getEmail(),
                telefone,
                usuario.getPerfil().name(),
                responsavelId,
                professorId,
                profissionalSaudeId,
                alunoId,
                turmaId,
                turmaNome,
                filhos,
                areasMenu);
    }

    @Transactional
    public UserMeResponse atualizarPerfil(String email, AtualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        String novoEmail = request.email().trim().toLowerCase();
        if (!novoEmail.equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(novoEmail).isPresent()) {
            throw new IllegalArgumentException("E-mail ja cadastrado");
        }

        Pessoa pessoa = usuario.getPessoa();
        if (pessoa == null) {
            throw new IllegalStateException("Usuario sem pessoa vinculada");
        }
        pessoa.setNome(request.nome().trim());
        pessoa.setTelefone(request.telefone() != null && !request.telefone().isBlank()
                ? request.telefone().trim()
                : null);
        pessoa.setEmail(novoEmail);
        usuario.setEmail(novoEmail);

        return me(novoEmail);
    }

    @Transactional
    public void trocarSenha(String email, TrocarSenhaRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.senhaNova()));
    }

    private FilhoResumo toFilhoResumo(Aluno aluno) {
        String turmaNome = aluno.getTurma() != null ? aluno.getTurma().getNome() : null;
        UUID turmaId = aluno.getTurma() != null ? aluno.getTurma().getId() : null;
        return new FilhoResumo(
                aluno.getId(),
                aluno.getPessoa().getNome(),
                aluno.getMatricula(),
                turmaNome,
                turmaId,
                aluno.isAutorizaUsoImagem());
    }
}
