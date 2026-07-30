package br.com.sge.modules.comunicacao.repository;

import br.com.sge.modules.comunicacao.entity.Comunicado;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComunicadoRepository extends JpaRepository<Comunicado, UUID> {

    @Query(
            """
            select c from Comunicado c
            left join fetch c.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch c.turma
            order by c.publicadoEm desc
            """)
    List<Comunicado> findAllDetalhados();

    @Query(
            """
            select c from Comunicado c
            left join fetch c.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch c.turma
            where (
                c.visivelPara = 'TODOS'
                or c.visivelPara like concat('%', :audiencia, '%')
            )
            and (c.turma is null or c.turma.id = :turmaId)
            order by c.publicadoEm desc
            """)
    List<Comunicado> findVisiveisParaAudienciaETurma(
            @Param("audiencia") String audiencia, @Param("turmaId") UUID turmaId);

    @Query(
            """
            select c from Comunicado c
            left join fetch c.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch c.turma
            where c.visivelPara = 'TODOS'
               or c.visivelPara like concat('%', :audiencia, '%')
            order by c.publicadoEm desc
            """)
    List<Comunicado> findVisiveisParaAudiencia(@Param("audiencia") String audiencia);
}
