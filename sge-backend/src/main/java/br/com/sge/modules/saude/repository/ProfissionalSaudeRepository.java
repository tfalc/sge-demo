package br.com.sge.modules.saude.repository;

import br.com.sge.modules.saude.entity.ProfissionalSaude;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {

    Optional<ProfissionalSaude> findByUsuarioId(UUID usuarioId);
}
