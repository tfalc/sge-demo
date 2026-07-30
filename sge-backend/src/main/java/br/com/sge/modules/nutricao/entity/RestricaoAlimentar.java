package br.com.sge.modules.nutricao.entity;

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
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "restricao_alimentar")
@Getter
@Setter
public class RestricaoAlimentar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String severidade = "MODERADA";

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
}
