package br.com.sge.modules.academico.repository;

import br.com.sge.modules.academico.entity.DiarioFrequenciaMeta;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiarioFrequenciaMetaRepository extends JpaRepository<DiarioFrequenciaMeta, UUID> {

    Optional<DiarioFrequenciaMeta> findByTurmaDisciplinaProfessorIdAndPeriodo_Id(
            UUID turmaDisciplinaProfessorId, UUID periodoId);
}
