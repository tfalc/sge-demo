package br.com.sge.modules.academico.service;

import br.com.sge.modules.academico.dto.AtualizarDisciplinaRequest;
import br.com.sge.modules.academico.dto.AtualizarTurmaRequest;
import br.com.sge.modules.academico.dto.CriarDisciplinaRequest;
import br.com.sge.modules.academico.dto.CriarProfessorRequest;
import br.com.sge.modules.academico.dto.CriarTurmaRequest;
import br.com.sge.modules.academico.dto.VincularDisciplinaTurmaRequest;
import br.com.sge.modules.academico.entity.Disciplina;
import br.com.sge.modules.academico.entity.Professor;
import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.academico.repository.DisciplinaRepository;
import br.com.sge.modules.academico.repository.ProfessorRepository;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import br.com.sge.modules.cadastro.entity.AnoLetivo;
import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Pessoa;
import br.com.sge.modules.cadastro.entity.Serie;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AnoLetivoRepository;
import br.com.sge.modules.cadastro.repository.PessoaRepository;
import br.com.sge.modules.cadastro.repository.SerieRepository;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicoEstruturaService {

    private static final String SENHA_PADRAO_LOCAL = "admin123";

    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final SerieRepository serieRepository;
    private final AnoLetivoRepository anoLetivoRepository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AcademicoEstruturaService(
            DisciplinaRepository disciplinaRepository,
            ProfessorRepository professorRepository,
            TurmaRepository turmaRepository,
            SerieRepository serieRepository,
            AnoLetivoRepository anoLetivoRepository,
            TurmaDisciplinaProfessorRepository tdpRepository,
            PessoaRepository pessoaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
        this.turmaRepository = turmaRepository;
        this.serieRepository = serieRepository;
        this.anoLetivoRepository = anoLetivoRepository;
        this.tdpRepository = tdpRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarDisciplinas() {
        return disciplinaRepository.findAllByOrderByNomeAsc().stream().map(this::toDisciplinaMap).toList();
    }

    @Transactional
    public Map<String, Object> atualizarDisciplina(UUID id, AtualizarDisciplinaRequest req) {
        Disciplina d = disciplinaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada"));
        d.setNome(req.nome().trim());
        d.setCodigo(req.codigo() != null ? req.codigo().trim() : null);
        return toDisciplinaMap(disciplinaRepository.save(d));
    }

    @Transactional
    public void excluirDisciplina(UUID id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new IllegalArgumentException("Disciplina nao encontrada");
        }
        disciplinaRepository.deleteById(id);
    }

    @Transactional
    public void excluirTurma(UUID id) {
        if (!turmaRepository.existsById(id)) {
            throw new IllegalArgumentException("Turma nao encontrada");
        }
        turmaRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> atualizarTurma(UUID id, AtualizarTurmaRequest req) {
        Turma turma = turmaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
        Serie serie = serieRepository
                .findById(req.serieId())
                .orElseThrow(() -> new IllegalArgumentException("Serie nao encontrada"));
        turma.setNome(req.nome().trim());
        turma.setSerie(serie);
        return toTurmaMap(turmaRepository.save(turma));
    }

    @Transactional
    public void excluirVinculo(UUID vinculoId) {
        if (!tdpRepository.existsById(vinculoId)) {
            throw new IllegalArgumentException("Vinculo nao encontrado");
        }
        tdpRepository.deleteById(vinculoId);
    }

    @Transactional
    public Map<String, Object> criarDisciplina(CriarDisciplinaRequest req) {
        if (disciplinaRepository.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new IllegalArgumentException("Disciplina ja cadastrada");
        }
        Disciplina d = new Disciplina();
        d.setNome(req.nome().trim());
        d.setCodigo(req.codigo() != null ? req.codigo().trim() : null);
        return toDisciplinaMap(disciplinaRepository.save(d));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarProfessores() {
        return professorRepository.findAll().stream().map(this::toProfessorMap).toList();
    }

    @Transactional
    public Map<String, Object> criarProfessor(CriarProfessorRequest req) {
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
        usuario.setPerfil(PerfilUsuario.PROFESSOR);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Professor professor = new Professor();
        professor.setPessoa(pessoa);
        professor.setUsuario(usuario);
        professor.setRegistroMec(req.registroMec());

        return toProfessorMap(professorRepository.save(professor));
    }

    @Transactional
    public Map<String, Object> criarTurma(CriarTurmaRequest req) {
        Serie serie = serieRepository
                .findById(req.serieId())
                .orElseThrow(() -> new IllegalArgumentException("Serie nao encontrada"));
        AnoLetivo anoLetivo = anoLetivoRepository.findAllByOrderByAnoDesc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ano letivo nao configurado"));

        Turma turma = new Turma();
        turma.setNome(req.nome().trim());
        turma.setSerie(serie);
        turma.setAnoLetivo(anoLetivo);
        turma.setCapacidadeMax(req.capacidadeMax() != null ? req.capacidadeMax() : 30);

        Turma saved = turmaRepository.save(turma);
        return toTurmaMap(saved);
    }

    @Transactional
    public Map<String, Object> vincularDisciplina(UUID turmaId, VincularDisciplinaTurmaRequest req) {
        Turma turma = turmaRepository
                .findDetalhadaById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
        Disciplina disciplina = disciplinaRepository
                .findById(req.disciplinaId())
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada"));
        Professor professor = professorRepository
                .findById(req.professorId())
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado"));

        boolean exists = tdpRepository.findByTurmaId(turmaId, null).stream()
                .anyMatch(tdp -> tdp.getDisciplina().getId().equals(disciplina.getId()));
        if (exists) {
            throw new IllegalArgumentException("Disciplina ja vinculada a esta turma");
        }

        TurmaDisciplinaProfessor tdp = new TurmaDisciplinaProfessor();
        tdp.setTurma(turma);
        tdp.setDisciplina(disciplina);
        tdp.setProfessor(professor);
        tdp.setAnoLetivo(turma.getAnoLetivo());
        TurmaDisciplinaProfessor saved = tdpRepository.save(tdp);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("turmaId", turmaId);
        m.put("disciplinaId", disciplina.getId());
        m.put("disciplinaNome", disciplina.getNome());
        m.put("professorId", professor.getId());
        m.put("professorNome", professor.getPessoa().getNome());
        return m;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarSeries() {
        return serieRepository.findAllComNivel().stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("nome", s.getNome());
                    m.put("nivelNome", s.getNivel().getNome());
                    return m;
                })
                .toList();
    }

    private Map<String, Object> toDisciplinaMap(Disciplina d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("nome", d.getNome());
        m.put("codigo", d.getCodigo());
        return m;
    }

    private Map<String, Object> toProfessorMap(Professor p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("nome", p.getPessoa().getNome());
        m.put("email", p.getUsuario() != null ? p.getUsuario().getEmail() : p.getPessoa().getEmail());
        m.put("registroMec", p.getRegistroMec());
        return m;
    }

    private Map<String, Object> toTurmaMap(Turma t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("nome", t.getNome());
        m.put("serieId", t.getSerie().getId());
        m.put("serieNome", t.getSerie().getNome());
        m.put("nivelNome", t.getSerie().getNivel().getNome());
        m.put("anoLetivo", t.getAnoLetivo().getAno());
        return m;
    }
}
