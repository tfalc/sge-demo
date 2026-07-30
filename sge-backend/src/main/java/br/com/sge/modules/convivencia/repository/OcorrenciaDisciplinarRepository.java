package br.com.sge.modules.convivencia.repository;

import br.com.sge.modules.convivencia.entity.OcorrenciaDisciplinar;
import br.com.sge.modules.convivencia.entity.StatusOcorrencia;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OcorrenciaDisciplinarRepository extends JpaRepository<OcorrenciaDisciplinar, UUID> {

    List<OcorrenciaDisciplinar> findByTurmaDisciplinaProfessorIdOrderByDataOcorrenciaDescCriadoEmDesc(
            UUID turmaDisciplinaProfessorId);

    @Query("""
            select o from OcorrenciaDisciplinar o
            join fetch o.aluno a
            join fetch a.pessoa
            join fetch o.turmaDisciplinaProfessor tdp
            join fetch tdp.disciplina
            where tdp.turma.id = :turmaId
            order by o.dataOcorrencia desc, o.criadoEm desc
            """)
    List<OcorrenciaDisciplinar> findByTurmaId(@Param("turmaId") UUID turmaId);

    long countByTurmaDisciplinaProfessorTurmaIdAndStatus(UUID turmaId, StatusOcorrencia status);
}
