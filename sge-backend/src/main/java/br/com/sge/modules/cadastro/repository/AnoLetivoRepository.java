package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.AnoLetivo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnoLetivoRepository extends JpaRepository<AnoLetivo, UUID> {

    List<AnoLetivo> findAllByOrderByAnoDesc();
}
