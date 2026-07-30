package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.MatrizCurricular;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatrizCurricularRepository extends JpaRepository<MatrizCurricular, UUID> {

  @Query(
      """
      select m from MatrizCurricular m
      left join fetch m.serie
      where m.ativo = true
      order by m.etapa, m.nome
      """)
  List<MatrizCurricular> findAllAtivas();

  @Query(
      """
      select m from MatrizCurricular m
      left join fetch m.serie
      left join fetch m.componentes c
      where m.id = :id
      order by c.ordem
      """)
  Optional<MatrizCurricular> findDetalhadaById(@Param("id") UUID id);

  @Query(
      """
      select m from MatrizCurricular m
      left join fetch m.serie
      left join fetch m.componentes c
      where m.serie.id = :serieId and m.ativo = true
      order by c.ordem
      """)
  Optional<MatrizCurricular> findAtivaBySerieId(@Param("serieId") UUID serieId);

  @Query(
      """
      select m from MatrizCurricular m
      left join fetch m.serie
      where m.escola.id = :escolaId and m.codigo = :codigo
      """)
  Optional<MatrizCurricular> findByEscolaIdAndCodigo(
      @Param("escolaId") UUID escolaId, @Param("codigo") String codigo);
}
