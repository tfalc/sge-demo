package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.Professor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    Optional<Professor> findByUsuarioId(UUID usuarioId);
}
