package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.PeriodoAvaliacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeriodoAvaliacaoRepository extends JpaRepository<PeriodoAvaliacao, UUID> {

    @Query(
            """
            select p from PeriodoAvaliacao p
            where p.anoLetivo.id = :anoLetivoId
            order by p.dataInicio nulls last, p.nome
            """)
    List<PeriodoAvaliacao> findByAnoLetivoId(@Param("anoLetivoId") UUID anoLetivoId);

    @Query("select p from PeriodoAvaliacao p order by p.dataInicio nulls last, p.nome")
    List<PeriodoAvaliacao> findAllOrdered();
}
