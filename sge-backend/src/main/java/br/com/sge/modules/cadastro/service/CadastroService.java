package br.com.sge.modules.cadastro.service;

import br.com.sge.modules.cadastro.dto.AtualizarEscolaRequest;
import br.com.sge.modules.cadastro.dto.CriarAlunoRequest;
import br.com.sge.modules.cadastro.dto.CriarResponsavelRequest;
import br.com.sge.modules.cadastro.dto.VincularResponsavelRequest;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.Escola;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Pessoa;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.EscolaRepository;
import br.com.sge.modules.cadastro.repository.PessoaRepository;
import br.com.sge.modules.cadastro.repository.ResponsavelRepository;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroService {

    private static final String SENHA_PADRAO_LOCAL = "admin123";

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final PessoaRepository pessoaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(
            AlunoRepository alunoRepository,
            TurmaRepository turmaRepository,
            PessoaRepository pessoaRepository,
            ResponsavelRepository responsavelRepository,
            UsuarioRepository usuarioRepository,
            EscolaRepository escolaRepository,
            PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.pessoaRepository = pessoaRepository;
        this.responsavelRepository = responsavelRepository;
        this.usuarioRepository = usuarioRepository;
        this.escolaRepository = escolaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterEscola() {
        Escola escola = escolaRepository
                .findFirstByOrderByCriadoEmAsc()
                .orElseThrow(() -> new IllegalArgumentException("Escola nao configurada"));
        return toEscolaMap(escola);
    }

    @Transactional
    public Map<String, Object> atualizarEscola(AtualizarEscolaRequest req) {
        Escola escola = escolaRepository
                .findFirstByOrderByCriadoEmAsc()
                .orElseThrow(() -> new IllegalArgumentException("Escola nao configurada"));
        escola.setNome(req.nome().trim());
        escola.setCnpj(req.cnpj() != null ? req.cnpj().trim() : null);
        escola.setNotaMinimaAprovacao(
                BigDecimal.valueOf(req.notaMinimaAprovacao()).setScale(2, RoundingMode.HALF_UP));
        escola.setFrequenciaMinima(
                BigDecimal.valueOf(req.frequenciaMinima()).setScale(2, RoundingMode.HALF_UP));
        return toEscolaMap(escolaRepository.save(escola));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAlunosDetalhados() {
        return alunoRepository.findAllDetalhados().stream().map(this::toAlunoMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarResponsaveis() {
        return responsavelRepository.findAll().stream()
                .map(r -> {
                    Map<String, Object> m = toResponsavelMap(r);
                    List<Map<String, Object>> alunos = alunoRepository.findByResponsavelId(r.getId()).stream()
                            .map(a -> Map.<String, Object>of("alunoId", a.getId(), "nome", a.getPessoa().getNome()))
                            .toList();
                    m.put("alunos", alunos);
                    return m;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> criarAluno(CriarAlunoRequest req) {
        if (alunoRepository.findAll().stream().anyMatch(a -> a.getMatricula().equals(req.matricula()))) {
            throw new IllegalArgumentException("Matricula ja cadastrada");
        }
        Turma turma = turmaRepository
                .findById(req.turmaId())
                .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));

        Pessoa pessoa = new Pessoa();
        pessoa.setNome(req.nome().trim());
        pessoa = pessoaRepository.save(pessoa);

        Aluno aluno = new Aluno();
        aluno.setPessoa(pessoa);
        aluno.setMatricula(req.matricula().trim());
        aluno.setTurma(turma);
        aluno.setStatus("ATIVO");

        return toAlunoMap(alunoRepository.save(aluno));
    }

    @Transactional
    public Map<String, Object> criarFilhoResponsavelLogado(CriarAlunoRequest req) {
        Responsavel responsavel =
                responsavelRepository
                        .findByUsuarioId(usuarioLogadoId())
                        .orElseThrow(() -> new IllegalArgumentException("Responsavel nao encontrado"));

        Map<String, Object> criado = criarAluno(req);
        UUID alunoId = (UUID) criado.get("id");
        Aluno aluno =
                alunoRepository
                        .findDetalhadoComResponsaveis(alunoId)
                        .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        boolean jaVinculado =
                aluno.getResponsaveis().stream().anyMatch(r -> r.getId().equals(responsavel.getId()));
        if (!jaVinculado) {
            aluno.getResponsaveis().add(responsavel);
            alunoRepository.save(aluno);
        }
        return criado;
    }

    private UUID usuarioLogadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Usuario nao autenticado");
        }
        String email = auth.getPrincipal().toString();
        return usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao autenticado"));
    }

    @Transactional
    public Map<String, Object> criarResponsavel(CriarResponsavelRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue(email).isPresent()) {
            throw new IllegalArgumentException("E-mail ja cadastrado");
        }

        Pessoa pessoa = new Pessoa();
        pessoa.setNome(req.nome().trim());
        pessoa.setEmail(email);
        pessoa = pessoaRepository.save(pessoa);

        String senha = req.senha() != null && !req.senha().isBlank() ? req.senha() : SENHA_PADRAO_LOCAL;
        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(PerfilUsuario.PAI);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Responsavel responsavel = new Responsavel();
        responsavel.setPessoa(pessoa);
        responsavel.setUsuario(usuario);
        responsavel.setGrauParentesco(req.grauParentesco());
        responsavel = responsavelRepository.save(responsavel);

        if (req.alunoId() != null) {
            Aluno aluno = alunoRepository
                    .findById(req.alunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
            aluno.getResponsaveis().add(responsavel);
            alunoRepository.save(aluno);
        }

        return toResponsavelMap(responsavel);
    }

    @Transactional
    public Map<String, Object> vincularResponsavelAluno(UUID alunoId, VincularResponsavelRequest req) {
        Aluno aluno = alunoRepository
                .findDetalhadoComResponsaveis(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        Responsavel responsavel = responsavelRepository
                .findById(req.responsavelId())
                .orElseThrow(() -> new IllegalArgumentException("Responsavel nao encontrado"));
        aluno.getResponsaveis().add(responsavel);
        return toAlunoMap(alunoRepository.save(aluno));
    }

    @Transactional
    public Map<String, Object> desvincularResponsavelAluno(UUID alunoId, UUID responsavelId) {
        Aluno aluno = alunoRepository
                .findDetalhadoComResponsaveis(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        boolean removed = aluno.getResponsaveis().removeIf(r -> r.getId().equals(responsavelId));
        if (!removed) {
            throw new IllegalArgumentException("Vinculo nao encontrado");
        }
        return toAlunoMap(alunoRepository.save(aluno));
    }

    private Map<String, Object> toEscolaMap(Escola e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("nome", e.getNome());
        m.put("cnpj", e.getCnpj());
        m.put("slug", e.getSlug());
        m.put("municipio", e.getMunicipio());
        m.put("uf", e.getUf());
        m.put("packageId", e.getPackageId());
        m.put("notaMinimaAprovacao", e.getNotaMinimaAprovacao());
        m.put("frequenciaMinima", e.getFrequenciaMinima());
        return m;
    }

    private Map<String, Object> toResponsavelMap(Responsavel r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("nome", r.getPessoa().getNome());
        m.put("email", r.getPessoa().getEmail());
        m.put("grauParentesco", r.getGrauParentesco());
        m.put(
                "usuarioEmail",
                r.getUsuario() != null ? r.getUsuario().getEmail() : null);
        return m;
    }

    private Map<String, Object> toAlunoMap(Aluno a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("nome", a.getPessoa().getNome());
        m.put("matricula", a.getMatricula());
        m.put("status", a.getStatus());
        m.put("turmaId", a.getTurma() != null ? a.getTurma().getId() : null);
        m.put("turmaNome", a.getTurma() != null ? a.getTurma().getNome() : null);
        List<Map<String, Object>> responsaveis = new ArrayList<>();
        if (a.getResponsaveis() != null) {
            for (Responsavel r : a.getResponsaveis()) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("responsavelId", r.getId());
                rm.put("nome", r.getPessoa().getNome());
                rm.put("grauParentesco", r.getGrauParentesco());
                responsaveis.add(rm);
            }
        }
        m.put("responsaveis", responsaveis);
        return m;
    }
}
