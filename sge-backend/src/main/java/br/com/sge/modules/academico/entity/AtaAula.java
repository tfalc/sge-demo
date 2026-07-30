package br.com.sge.modules.academico.entity;

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
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ata_aula")
@Getter
@Setter
@NoArgsConstructor
public class AtaAula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_disciplina_professor_id", nullable = false)
    private TurmaDisciplinaProfessor turmaDisciplinaProfessor;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "tarefa_casa", columnDefinition = "TEXT")
    private String tarefaCasa;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();
}
