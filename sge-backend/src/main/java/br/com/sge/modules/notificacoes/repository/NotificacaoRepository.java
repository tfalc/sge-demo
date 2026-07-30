package br.com.sge.modules.notificacoes.repository;

import br.com.sge.modules.notificacoes.entity.Notificacao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    @Query(
            """
            select n from Notificacao n
            where n.usuario.id = :usuarioId
            order by n.criadoEm desc
            """)
    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(@Param("usuarioId") UUID usuarioId);

    long countByUsuarioIdAndLidaFalse(UUID usuarioId);

    Optional<Notificacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notificacao n set n.lida = true where n.usuario.id = :usuarioId and n.lida = false")
    int marcarTodasLidas(@Param("usuarioId") UUID usuarioId);
}
