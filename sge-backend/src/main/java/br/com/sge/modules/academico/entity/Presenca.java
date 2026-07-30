package br.com.sge.modules.academico.entity;

import br.com.sge.modules.cadastro.entity.Aluno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "presenca")
@Getter
@Setter
@NoArgsConstructor
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_disciplina_professor_id", nullable = false)
    private TurmaDisciplinaProfessor turmaDisciplinaProfessor;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Column(nullable = false)
    private Boolean presente;

    @Column(columnDefinition = "TEXT")
    private String justificativa;
}
