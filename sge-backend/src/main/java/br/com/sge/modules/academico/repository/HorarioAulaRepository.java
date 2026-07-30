package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.HorarioAula;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioAulaRepository extends JpaRepository<HorarioAula, UUID> {

    @Query(
            """
            select h from HorarioAula h
            join fetch h.disciplina
            left join fetch h.professor p
            left join fetch p.pessoa
            where h.turma.id = :turmaId
            order by h.diaSemana, h.horaInicio
            """)
    List<HorarioAula> findByTurmaId(@Param("turmaId") UUID turmaId);

    @Query(
            """
            select h from HorarioAula h
            join fetch h.turma
            join fetch h.disciplina
            left join fetch h.professor p
            left join fetch p.pessoa
            where h.professor.id = :professorId
            order by h.diaSemana, h.horaInicio
            """)
    List<HorarioAula> findByProfessorId(@Param("professorId") UUID professorId);
}
