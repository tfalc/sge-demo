package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Turma;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

    @Query(
            """
            select distinct t from Turma t
            join fetch t.serie s
            join fetch s.nivel
            join fetch t.anoLetivo
            join br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor tdp on tdp.turma = t
            where tdp.professor.id = :professorId
            order by t.nome
            """)
    List<Turma> findByProfessorId(@Param("professorId") UUID professorId);

    @Query(
            """
            select t from Turma t
            join fetch t.serie s
            join fetch s.nivel
            join fetch t.anoLetivo
            order by t.nome
            """)
    List<Turma> findAllDetalhadas();

    @Query(
            """
            select t from Turma t
            join fetch t.serie s
            join fetch s.nivel
            join fetch t.anoLetivo
            where t.id = :id
            """)
    java.util.Optional<Turma> findDetalhadaById(@Param("id") UUID id);
}
