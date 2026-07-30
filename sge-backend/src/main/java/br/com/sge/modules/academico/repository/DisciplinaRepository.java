package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.Disciplina;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, UUID> {

    List<Disciplina> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
