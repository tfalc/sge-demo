package br.com.sge.modules.nutricao.repository;

import br.com.sge.modules.nutricao.entity.RestricaoAlimentar;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestricaoAlimentarRepository extends JpaRepository<RestricaoAlimentar, UUID> {

    @Query("""
            select r from RestricaoAlimentar r
            join fetch r.aluno a
            join fetch a.pessoa
            order by a.pessoa.nome, r.criadoEm desc
            """)
    List<RestricaoAlimentar> findAllComAluno();

    @Query("""
            select r from RestricaoAlimentar r
            join fetch r.aluno a
            join fetch a.pessoa
            where a.id = :alunoId
            order by r.criadoEm desc
            """)
    List<RestricaoAlimentar> findByAlunoId(@Param("alunoId") UUID alunoId);
}
