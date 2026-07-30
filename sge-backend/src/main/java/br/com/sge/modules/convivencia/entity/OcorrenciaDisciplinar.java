package br.com.sge.modules.convivencia.entity;

import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.cadastro.entity.Aluno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ocorrencia_disciplinar")
@Getter
@Setter
@NoArgsConstructor
public class OcorrenciaDisciplinar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_disciplina_professor_id", nullable = false)
    private TurmaDisciplinaProfessor turmaDisciplinaProfessor;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOcorrencia tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOcorrencia status = StatusOcorrencia.REGISTRADA;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
}
