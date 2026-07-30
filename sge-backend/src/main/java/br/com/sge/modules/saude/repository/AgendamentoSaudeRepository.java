package br.com.sge.modules.saude.repository;

import br.com.sge.modules.saude.entity.AgendamentoSaude;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgendamentoSaudeRepository extends JpaRepository<AgendamentoSaude, UUID> {

    @Query(
            """
            select a from AgendamentoSaude a
            join fetch a.aluno al
            join fetch al.pessoa
            where a.profissional.id = :profissionalId
            order by a.dataHora
            """)
    List<AgendamentoSaude> findByProfissionalId(@Param("profissionalId") UUID profissionalId);

    @Query(
            """
            select a from AgendamentoSaude a
            join fetch a.profissional p
            join fetch p.pessoa
            where a.aluno.id = :alunoId
            order by a.dataHora desc
            """)
    List<AgendamentoSaude> findByAlunoId(@Param("alunoId") UUID alunoId);
}
