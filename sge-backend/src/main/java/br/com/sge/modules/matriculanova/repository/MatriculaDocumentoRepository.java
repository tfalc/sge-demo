package br.com.sge.modules.matriculanova.repository;

import br.com.sge.modules.matriculanova.entity.MatriculaDocumento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaDocumentoRepository extends JpaRepository<MatriculaDocumento, UUID> {

    List<MatriculaDocumento> findByProcessoIdOrderByEnviadoEmDesc(UUID processoId);

    Optional<MatriculaDocumento> findByIdAndProcessoId(UUID id, UUID processoId);
}
