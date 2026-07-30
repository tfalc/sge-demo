package br.com.sge.modules.academico.entity;

import br.com.sge.modules.academico.entity.DiarioFrequenciaMeta;
import br.com.sge.modules.academico.entity.PeriodoAvaliacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "diario_frequencia_meta")
@Getter
@Setter
public class DiarioFrequenciaMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_disciplina_professor_id", nullable = false)
    private TurmaDisciplinaProfessor turmaDisciplinaProfessor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_avaliacao_id", nullable = false)
    private PeriodoAvaliacao periodo;

    @Column(name = "aulas_previstas")
    private Integer aulasPrevistas;

    @Column(name = "assinatura_em")
    private Instant assinaturaEm;
}
