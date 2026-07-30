package br.com.sge.modules.colegiado.repository;

import br.com.sge.modules.colegiado.entity.ColegiadoEncaminhamento;
import br.com.sge.modules.colegiado.entity.StatusEncaminhamentoColegiado;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColegiadoEncaminhamentoRepository extends JpaRepository<ColegiadoEncaminhamento, UUID> {

    @Query("""
            select e from ColegiadoEncaminhamento e
            join fetch e.reuniao r
            left join fetch e.responsavelUsuario u
            left join fetch u.pessoa
            where e.reuniao.id = :reuniaoId
            order by e.criadoEm
            """)
    List<ColegiadoEncaminhamento> findByReuniaoId(@Param("reuniaoId") UUID reuniaoId);

    @Query("""
            select e from ColegiadoEncaminhamento e
            join fetch e.reuniao r
            left join fetch r.turma
            left join fetch e.responsavelUsuario u
            left join fetch u.pessoa
            where e.status = :status
            and (:turmaId is null or r.turma.id = :turmaId)
            order by e.prazo nulls last, e.criadoEm desc
            """)
    List<ColegiadoEncaminhamento> findPendentes(
            @Param("status") StatusEncaminhamentoColegiado status, @Param("turmaId") UUID turmaId);
}
