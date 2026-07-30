package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.MatrizComponente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatrizComponenteRepository extends JpaRepository<MatrizComponente, UUID> {

  @Modifying
  @Query("delete from MatrizComponente c where c.matriz.id = :matrizId")
  void deleteByMatrizId(@Param("matrizId") UUID matrizId);
}
