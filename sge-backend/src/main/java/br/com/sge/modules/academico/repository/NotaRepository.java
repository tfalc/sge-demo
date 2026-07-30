package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.Nota;
import br.com.sge.modules.academico.entity.TipoNota;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotaRepository extends JpaRepository<Nota, UUID> {

    @Query(
            """
            select n from Nota n
            join fetch n.turmaDisciplinaProfessor tdp
            join fetch tdp.disciplina
            join fetch n.periodo
            where n.aluno.id = :alunoId
            order by n.periodo.dataInicio nulls last, tdp.disciplina.nome, n.tipo
            """)
    List<Nota> findByAlunoId(@Param("alunoId") UUID alunoId);

    Optional<Nota> findByAlunoIdAndTurmaDisciplinaProfessorIdAndPeriodoIdAndTipo(
            UUID alunoId, UUID turmaDisciplinaProfessorId, UUID periodoId, TipoNota tipo);

    @Query(
            """
            select n from Nota n
            join fetch n.aluno
            join fetch n.periodo
            where n.turmaDisciplinaProfessor.id = :tdpId
            order by n.aluno.pessoa.nome, n.periodo.dataInicio nulls last, n.tipo
            """)
    List<Nota> findByTurmaDisciplinaProfessorId(@Param("tdpId") UUID tdpId);
}
