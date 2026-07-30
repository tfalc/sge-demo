package br.com.sge.modules.colegiado.repository;

import br.com.sge.modules.colegiado.entity.ColegiadoReuniao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColegiadoReuniaoRepository extends JpaRepository<ColegiadoReuniao, UUID> {

    @Query("""
            select r from ColegiadoReuniao r
            left join fetch r.turma t
            left join fetch t.serie
            where (:turmaId is null or r.turma.id = :turmaId)
            order by r.dataReuniao desc, r.criadoEm desc
            """)
    List<ColegiadoReuniao> findAllComTurma(@Param("turmaId") UUID turmaId);

    @Query("""
            select r from ColegiadoReuniao r
            left join fetch r.turma t
            left join fetch t.serie
            where r.id = :id
            """)
    Optional<ColegiadoReuniao> findDetalhadaById(@Param("id") UUID id);
}
