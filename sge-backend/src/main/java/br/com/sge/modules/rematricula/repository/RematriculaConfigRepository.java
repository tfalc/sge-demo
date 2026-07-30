package br.com.sge.modules.rematricula.repository;

import br.com.sge.modules.rematricula.entity.RematriculaConfig;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RematriculaConfigRepository extends JpaRepository<RematriculaConfig, UUID> {

    Optional<RematriculaConfig> findFirstByOrderByAtualizadoEmDesc();
}
