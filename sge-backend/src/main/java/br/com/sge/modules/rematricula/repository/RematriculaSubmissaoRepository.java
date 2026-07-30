package br.com.sge.modules.rematricula.repository;

import br.com.sge.modules.rematricula.entity.RematriculaSubmissao;
import br.com.sge.modules.rematricula.entity.StatusRematriculaSubmissao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RematriculaSubmissaoRepository extends JpaRepository<RematriculaSubmissao, UUID> {

    Optional<RematriculaSubmissao> findByAlunoIdAndAnoLetivoId(UUID alunoId, UUID anoLetivoId);

    @Query(
            """
            SELECT s FROM RematriculaSubmissao s
            JOIN FETCH s.aluno a
            JOIN FETCH a.pessoa
            LEFT JOIN FETCH a.turma t
            LEFT JOIN FETCH t.serie
            JOIN FETCH s.responsavel r
            JOIN FETCH r.pessoa
            WHERE s.status = :status
            ORDER BY s.enviadoEm DESC NULLS LAST, s.atualizadoEm DESC
            """)
    List<RematriculaSubmissao> findDetalhadasByStatus(@Param("status") StatusRematriculaSubmissao status);

    @Query(
            """
            SELECT s FROM RematriculaSubmissao s
            JOIN FETCH s.aluno a
            JOIN FETCH a.pessoa
            LEFT JOIN FETCH a.turma t
            LEFT JOIN FETCH t.serie
            JOIN FETCH s.responsavel r
            JOIN FETCH r.pessoa
            WHERE s.id = :id
            """)
    Optional<RematriculaSubmissao> findDetalhadaById(@Param("id") UUID id);

    @Query(
            """
            SELECT s FROM RematriculaSubmissao s
            JOIN FETCH s.aluno a
            JOIN FETCH a.pessoa
            WHERE s.aluno.id IN :alunoIds AND s.anoLetivo.id = :anoLetivoId
            """)
    List<RematriculaSubmissao> findByAlunoIdsAndAnoLetivoId(
            @Param("alunoIds") List<UUID> alunoIds, @Param("anoLetivoId") UUID anoLetivoId);
}
