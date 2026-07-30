package br.com.sge.modules.colegiado.entity;

import br.com.sge.modules.cadastro.entity.Turma;
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
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "colegiado_reuniao")
@Getter
@Setter
public class ColegiadoReuniao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 30)
    private String tipo = "PEDAGOGICO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @Column(name = "data_reuniao", nullable = false)
    private LocalDate dataReuniao;

    @Column(name = "hora_reuniao")
    private LocalTime horaReuniao;

    @Column(columnDefinition = "TEXT")
    private String pauta;

    @Column(name = "ata_texto", columnDefinition = "TEXT")
    private String ataTexto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReuniaoColegiado status = StatusReuniaoColegiado.AGENDADA;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "concluida_em")
    private Instant concluidaEm;
}
