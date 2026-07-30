package br.com.sge.modules.matriculanova.repository;

import br.com.sge.modules.matriculanova.entity.MatriculaProcesso;
import br.com.sge.modules.matriculanova.entity.StatusMatriculaProcesso;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaProcessoRepository extends JpaRepository<MatriculaProcesso, UUID> {

    List<MatriculaProcesso> findByStatusOrderByCriadoEmDesc(StatusMatriculaProcesso status);

    List<MatriculaProcesso> findAllByOrderByCriadoEmDesc();

    @Query("""
            select p from MatriculaProcesso p
            left join fetch p.anoLetivo
            left join fetch p.turmaPretendida t
            left join fetch t.serie
            left join fetch p.responsavel r
            left join fetch r.pessoa
            left join fetch p.aluno
            where p.id = :id
            """)
    Optional<MatriculaProcesso> findDetalhadoById(@Param("id") UUID id);
}
