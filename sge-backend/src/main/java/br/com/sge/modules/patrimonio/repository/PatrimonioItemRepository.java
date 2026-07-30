package br.com.sge.modules.patrimonio.repository;

import br.com.sge.modules.patrimonio.entity.PatrimonioItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatrimonioItemRepository extends JpaRepository<PatrimonioItem, UUID> {

    List<PatrimonioItem> findAllByOrderByNomeAsc();
}
