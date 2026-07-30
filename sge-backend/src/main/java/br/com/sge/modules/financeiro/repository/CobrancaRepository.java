package br.com.sge.modules.financeiro.repository;

import br.com.sge.modules.financeiro.entity.Cobranca;
import br.com.sge.modules.financeiro.entity.StatusCobranca;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CobrancaRepository extends JpaRepository<Cobranca, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cobranca c where c.id = :id")
    Optional<Cobranca> findByIdForUpdate(@Param("id") UUID id);

    @Query(
            """
            select distinct c from Cobranca c
            join fetch c.contrato ct
            join fetch ct.aluno a
            join fetch a.pessoa
            join a.responsaveis r
            where r.id = :responsavelId
            order by c.competencia desc
            """)
    List<Cobranca> findByResponsavelId(@Param("responsavelId") UUID responsavelId);

    /**
     * Cobranças vencidas e não quitadas: data de vencimento &lt; hoje e status não é PAGO nem CANCELADO.
     */
    @Query(
            """
            select distinct c from Cobranca c
            join fetch c.contrato ct
            join fetch ct.aluno a
            join fetch a.pessoa
            where c.vencimento < :hoje
              and c.status not in :excluirStatus
            order by c.vencimento asc, c.id
            """)
    List<Cobranca> findCobrancasVencidasNaoPagas(
            @Param("hoje") LocalDate hoje, @Param("excluirStatus") List<StatusCobranca> excluirStatus);

    @Query(
            """
            select distinct c from Cobranca c
            join fetch c.contrato ct
            join fetch ct.aluno a
            join fetch a.pessoa
            where c.status = :statusPendente
              and c.vencimento < :hoje
            """)
    List<Cobranca> findPendentesComVencimentoUltrapassado(
            @Param("statusPendente") StatusCobranca statusPendente, @Param("hoje") LocalDate hoje);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update Cobranca c
            set c.status = :novoStatus
            where c.status = :statusPendente
              and c.vencimento < :hoje
            """)
    int updateStatusPendenteParaVencido(
            @Param("statusPendente") StatusCobranca statusPendente,
            @Param("novoStatus") StatusCobranca novoStatus,
            @Param("hoje") LocalDate hoje);

    @Query(
            """
            select coalesce(sum(c.valor), 0)
            from Cobranca c
            where c.status = :status
              and c.pagoEm >= :inicio and c.pagoEm < :fim
            """)
    BigDecimal sumValorPagoEntre(
            @Param("status") StatusCobranca status,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    /** Em aberto no prazo: PENDENTE com vencimento &gt;= hoje. */
    @Query(
            """
            select coalesce(sum(c.valor), 0)
            from Cobranca c
            where c.status = :pendente
              and c.vencimento >= :hoje
            """)
    BigDecimal sumValorPendenteNoPrazo(
            @Param("pendente") StatusCobranca pendente, @Param("hoje") LocalDate hoje);

    /**
     * Valor atrasado não pago: VENCIDO ou PENDENTE com vencimento &lt; hoje (antes do job do dia, ou
     * consistência explícita).
     */
    @Query(
            """
            select coalesce(sum(c.valor), 0)
            from Cobranca c
            where c.status not in :excluir
              and c.vencimento < :hoje
            """)
    BigDecimal sumValorVencidoOuAtrasado(
            @Param("hoje") LocalDate hoje, @Param("excluir") List<StatusCobranca> excluir);

    boolean existsByContratoIdAndCompetencia(UUID contratoId, LocalDate competencia);
}
