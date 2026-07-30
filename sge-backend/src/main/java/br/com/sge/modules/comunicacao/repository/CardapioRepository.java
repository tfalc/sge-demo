package br.com.sge.modules.comunicacao.repository;

import br.com.sge.modules.comunicacao.entity.Cardapio;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardapioRepository extends JpaRepository<Cardapio, UUID> {

    @Query(
            """
            select c from Cardapio c
            left join fetch c.nutricionista nu
            left join fetch nu.pessoa
            where c.dataRefeicao = :data
            order by c.tipoRefeicao
            """)
    List<Cardapio> findByDataRefeicao(@Param("data") LocalDate data);
}
