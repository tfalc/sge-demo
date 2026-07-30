package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.AtaAula;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtaAulaRepository extends JpaRepository<AtaAula, UUID> {

    Optional<AtaAula> findByTurmaDisciplinaProfessorIdAndDataAula(
            UUID turmaDisciplinaProfessorId, LocalDate dataAula);

    List<AtaAula> findByTurmaDisciplinaProfessorIdAndDataAulaBetweenOrderByDataAulaDesc(
            UUID turmaDisciplinaProfessorId, LocalDate inicio, LocalDate fim);
}
