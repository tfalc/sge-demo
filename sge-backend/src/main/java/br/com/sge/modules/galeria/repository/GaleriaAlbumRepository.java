package br.com.sge.modules.galeria.repository;

import br.com.sge.modules.galeria.entity.GaleriaAlbum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GaleriaAlbumRepository extends JpaRepository<GaleriaAlbum, UUID> {

    @Query(
            """
            select a from GaleriaAlbum a
            left join fetch a.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch a.turma
            order by a.publicadoEm desc
            """)
    List<GaleriaAlbum> findAllDetalhados();

    @Query(
            """
            select a from GaleriaAlbum a
            left join fetch a.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch a.turma
            where (
                a.visivelPara = 'TODOS'
                or a.visivelPara like concat('%', :audiencia, '%')
            )
            and (a.turma is null or a.turma.id = :turmaId)
            order by a.publicadoEm desc
            """)
    List<GaleriaAlbum> findVisiveisParaAudienciaETurma(
            @Param("audiencia") String audiencia, @Param("turmaId") UUID turmaId);

    @Query(
            """
            select a from GaleriaAlbum a
            left join fetch a.publicadoPor pu
            left join fetch pu.pessoa
            left join fetch a.turma
            where a.visivelPara = 'TODOS'
               or a.visivelPara like concat('%', :audiencia, '%')
            order by a.publicadoEm desc
            """)
    List<GaleriaAlbum> findVisiveisParaAudiencia(@Param("audiencia") String audiencia);
}
