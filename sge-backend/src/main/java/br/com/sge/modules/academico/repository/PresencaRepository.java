package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.Presenca;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {

    @Query(
            """
            select p from Presenca p
            join fetch p.turmaDisciplinaProfessor tdp
            join fetch tdp.disciplina
            where p.aluno.id = :alunoId
            order by p.dataAula desc
            """)
    List<Presenca> findByAlunoId(@Param("alunoId") UUID alunoId);

    Optional<Presenca> findByAlunoIdAndTurmaDisciplinaProfessorIdAndDataAula(
            UUID alunoId, UUID turmaDisciplinaProfessorId, LocalDate dataAula);

    @Query(
            """
            select p from Presenca p
            join fetch p.aluno a
            join fetch a.pessoa
            where p.turmaDisciplinaProfessor.id = :tdpId and p.dataAula = :dataAula
            """)
    List<Presenca> findByTurmaDisciplinaProfessorIdAndDataAula(
            @Param("tdpId") UUID tdpId, @Param("dataAula") LocalDate dataAula);

    @Query(
            """
            select p from Presenca p
            join fetch p.aluno a
            join fetch a.pessoa
            where p.turmaDisciplinaProfessor.id = :tdpId
            and p.dataAula >= :inicio and p.dataAula <= :fim
            order by p.dataAula, a.pessoa.nome
            """)
    List<Presenca> findByTurmaDisciplinaProfessorIdAndDataAulaBetween(
            @Param("tdpId") UUID tdpId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query(
            """
            select distinct p.dataAula from Presenca p
            where p.turmaDisciplinaProfessor.id = :tdpId
            and p.dataAula >= :inicio and p.dataAula <= :fim
            order by p.dataAula
            """)
    List<LocalDate> findDatasDistintasByTdpAndPeriodo(
            @Param("tdpId") UUID tdpId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);
}
