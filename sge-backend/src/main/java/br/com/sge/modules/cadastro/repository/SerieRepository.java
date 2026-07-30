package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Serie;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SerieRepository extends JpaRepository<Serie, UUID> {

    @Query("select s from Serie s join fetch s.nivel order by s.ordem")
    List<Serie> findAllComNivel();
}
