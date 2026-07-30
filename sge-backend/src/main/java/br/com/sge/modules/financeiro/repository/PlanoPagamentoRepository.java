package br.com.sge.modules.financeiro.repository;

import br.com.sge.modules.financeiro.entity.PlanoPagamento;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoPagamentoRepository extends JpaRepository<PlanoPagamento, UUID> {

    List<PlanoPagamento> findAllByOrderByNomeAsc();
}
