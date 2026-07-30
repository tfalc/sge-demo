package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Aluno;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    @Query(
            """
            select distinct a from Aluno a
            join fetch a.pessoa
            left join fetch a.turma t
            left join fetch t.serie
            join a.responsaveis r
            where r.id = :responsavelId
            order by a.pessoa.nome
            """)
    List<Aluno> findByResponsavelId(@Param("responsavelId") UUID responsavelId);

    @Query(
            """
            select a from Aluno a
            join fetch a.pessoa
            where a.turma.id = :turmaId and a.status = 'ATIVO'
            order by a.pessoa.nome
            """)
    List<Aluno> findAtivosByTurmaId(@Param("turmaId") UUID turmaId);

    @Query(
            """
            select a from Aluno a
            join fetch a.pessoa
            left join fetch a.turma t
            left join fetch t.serie
            where a.id = :id
            """)
    java.util.Optional<Aluno> findDetalhadoById(@Param("id") UUID id);

    @Query(
            """
            select distinct a from Aluno a
            join fetch a.pessoa
            left join fetch a.turma
            left join fetch a.responsaveis r
            left join fetch r.pessoa
            order by a.pessoa.nome
            """)
    List<Aluno> findAllDetalhados();

    @Query(
            """
            select a from Aluno a
            join fetch a.pessoa
            left join fetch a.turma
            left join fetch a.responsaveis r
            left join fetch r.pessoa
            where a.id = :id
            """)
    java.util.Optional<Aluno> findDetalhadoComResponsaveis(@Param("id") UUID id);

    @Query(
            """
            select a from Aluno a
            join fetch a.pessoa
            left join fetch a.turma
            where a.usuario.id = :usuarioId
            """)
    java.util.Optional<Aluno> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
