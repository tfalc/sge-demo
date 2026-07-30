package br.com.sge.config;

import br.com.sge.modules.academico.repository.ProfessorRepository;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.AlunoRepository;
import br.com.sge.modules.cadastro.repository.ResponsavelRepository;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import br.com.sge.modules.financeiro.repository.CobrancaRepository;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("sgeAuth")
public class SgeAuthorization {

    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaDisciplinaProfessorRepository turmaDisciplinaProfessorRepository;
    private final CobrancaRepository cobrancaRepository;

    public SgeAuthorization(
            UsuarioRepository usuarioRepository,
            ResponsavelRepository responsavelRepository,
            AlunoRepository alunoRepository,
            ProfessorRepository professorRepository,
            TurmaDisciplinaProfessorRepository turmaDisciplinaProfessorRepository,
            CobrancaRepository cobrancaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.responsavelRepository = responsavelRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.turmaDisciplinaProfessorRepository = turmaDisciplinaProfessorRepository;
        this.cobrancaRepository = cobrancaRepository;
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return Arrays.stream(roles)
                .anyMatch(role -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)));
    }

    public boolean canAccessAluno(UUID alunoId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR", "PROFESSOR", "PSICOLOGA")) {
            return true;
        }
        if (hasRole("PAI")) {
            return isFilhoDoResponsavelLogado(alunoId);
        }
        if (hasRole("ALUNO")) {
            return isAlunoLogado(alunoId);
        }
        return false;
    }

    public boolean canAccessTurmaHorario(UUID turmaId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR", "PROFESSOR")) {
            return true;
        }
        if (hasRole("PAI")) {
            return responsavelRepository
                    .findByUsuarioId(currentUsuarioId())
                    .map(responsavel -> alunoRepository.findByResponsavelId(responsavel.getId()).stream()
                            .anyMatch(aluno -> aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId)))
                    .orElse(false);
        }
        if (hasRole("ALUNO")) {
            return alunoRepository
                    .findByUsuarioId(currentUsuarioId())
                    .map(aluno -> aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId))
                    .orElse(false);
        }
        return false;
    }

    public boolean canAccessProfessorHorario(UUID professorId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR")) {
            return true;
        }
        if (hasRole("PROFESSOR")) {
            return professorRepository
                    .findByUsuarioId(currentUsuarioId())
                    .map(professor -> professor.getId().equals(professorId))
                    .orElse(false);
        }
        return false;
    }

    public boolean canAccessTurmaDisciplinaProfessor(UUID turmaDisciplinaProfessorId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR", "COORDENADOR")) {
            return true;
        }
        if (hasRole("PROFESSOR")) {
            return professorRepository
                    .findByUsuarioId(currentUsuarioId())
                    .flatMap(professor -> turmaDisciplinaProfessorRepository
                            .findById(turmaDisciplinaProfessorId)
                            .map(tdp -> tdp.getProfessor().getId().equals(professor.getId())))
                    .orElse(false);
        }
        return false;
    }

    public boolean canListCobrancasResponsavel(String responsavelId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")) {
            return true;
        }
        if (hasRole("PAI")) {
            try {
                UUID rid = UUID.fromString(responsavelId);
                return isResponsavelLogado(rid);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return false;
    }

    public boolean canAccessCobranca(String cobrancaId) {
        if (hasAnyRole("ADMIN", "SECRETARIA", "DIRETOR")) {
            return true;
        }
        if (hasRole("PAI")) {
            UUID id;
            try {
                id = UUID.fromString(cobrancaId.trim());
            } catch (IllegalArgumentException ex) {
                return false;
            }
            return responsavelRepository
                    .findByUsuarioId(currentUsuarioId())
                    .map(responsavel -> cobrancaRepository.findByResponsavelId(responsavel.getId()).stream()
                            .anyMatch(cobranca -> cobranca.getId().equals(id)))
                    .orElse(false);
        }
        return false;
    }

    private boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    private boolean isResponsavelLogado(UUID responsavelId) {
        return responsavelRepository
                .findByUsuarioId(currentUsuarioId())
                .map(Responsavel::getId)
                .filter(responsavelId::equals)
                .isPresent();
    }

    private boolean isFilhoDoResponsavelLogado(UUID alunoId) {
        return responsavelRepository
                .findByUsuarioId(currentUsuarioId())
                .map(responsavel -> alunoRepository.findByResponsavelId(responsavel.getId()).stream()
                        .anyMatch(aluno -> aluno.getId().equals(alunoId)))
                .orElse(false);
    }

    private boolean isAlunoLogado(UUID alunoId) {
        return alunoRepository
                .findByUsuarioId(currentUsuarioId())
                .map(Aluno::getId)
                .filter(alunoId::equals)
                .isPresent();
    }

    private UUID currentUsuarioId() {
        String email = currentEmail();
        return usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao autenticado"));
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Usuario nao autenticado");
        }
        return auth.getPrincipal().toString();
    }
}
