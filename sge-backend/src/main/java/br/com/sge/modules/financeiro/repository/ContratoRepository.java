package br.com.sge.modules.financeiro.repository;

import br.com.sge.modules.financeiro.entity.Contrato;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContratoRepository extends JpaRepository<Contrato, UUID> {

    @Query(
            """
            select c from Contrato c
            join fetch c.aluno a
            join fetch a.pessoa
            join fetch c.plano
            where c.status = 'ATIVO'
            order by a.pessoa.nome
            """)
    List<Contrato> findAllAtivosComAluno();

    boolean existsByAlunoIdAndStatus(UUID alunoId, String status);
}
