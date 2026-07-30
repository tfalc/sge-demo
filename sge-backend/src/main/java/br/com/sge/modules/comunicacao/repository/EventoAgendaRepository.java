package br.com.sge.modules.comunicacao.repository;

import br.com.sge.modules.comunicacao.entity.EventoAgenda;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoAgendaRepository extends JpaRepository<EventoAgenda, UUID> {

    @Query(
            """
            select e from EventoAgenda e
            left join fetch e.turma
            where e.dataInicio >= :inicio and e.dataInicio < :fim
            order by e.dataInicio
            """)
    List<EventoAgenda> findNoPeriodo(@Param("inicio") Instant inicio, @Param("fim") Instant fim);

    @Query(
            """
            select e from EventoAgenda e
            left join fetch e.turma
            where e.dataInicio >= :inicio and e.dataInicio < :fim
              and (e.turma is null or e.turma.id = :turmaId)
            order by e.dataInicio
            """)
    List<EventoAgenda> findNoPeriodoParaTurma(
            @Param("inicio") Instant inicio, @Param("fim") Instant fim, @Param("turmaId") UUID turmaId);
}
