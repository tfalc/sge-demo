package br.com.sge.modules.colegiado.repository;

import br.com.sge.modules.colegiado.entity.ColegiadoParticipante;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColegiadoParticipanteRepository extends JpaRepository<ColegiadoParticipante, UUID> {

    @Query("""
            select p from ColegiadoParticipante p
            left join fetch p.usuario u
            left join fetch u.pessoa
            where p.reuniao.id = :reuniaoId
            order by p.nomeExibicao
            """)
    List<ColegiadoParticipante> findByReuniaoId(@Param("reuniaoId") UUID reuniaoId);
}
