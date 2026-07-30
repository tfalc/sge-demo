package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Responsavel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {

    Optional<Responsavel> findByUsuarioId(UUID usuarioId);
}
