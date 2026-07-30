package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurmaDisciplinaProfessorRepository extends JpaRepository<TurmaDisciplinaProfessor, UUID> {

    @Query(
            """
            select tdp from TurmaDisciplinaProfessor tdp
            join fetch tdp.disciplina
            join fetch tdp.turma
            join fetch tdp.professor p
            join fetch p.pessoa
            where tdp.turma.id = :turmaId
              and (:professorId is null or tdp.professor.id = :professorId)
            order by tdp.disciplina.nome
            """)
    List<TurmaDisciplinaProfessor> findByTurmaId(
            @Param("turmaId") UUID turmaId, @Param("professorId") UUID professorId);

    @Query(
            """
            select tdp from TurmaDisciplinaProfessor tdp
            join fetch tdp.disciplina
            join fetch tdp.turma
            join fetch tdp.professor p
            join fetch p.pessoa
            where tdp.id = :id
            """)
    Optional<TurmaDisciplinaProfessor> findDetalhadoById(@Param("id") UUID id);
}
