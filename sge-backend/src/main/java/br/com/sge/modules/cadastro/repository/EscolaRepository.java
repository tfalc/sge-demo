package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Escola;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscolaRepository extends JpaRepository<Escola, UUID> {

    Optional<Escola> findFirstByOrderByCriadoEmAsc();
}
