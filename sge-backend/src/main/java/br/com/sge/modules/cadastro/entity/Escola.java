package br.com.sge.modules.cadastro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "escola")
@Getter
@Setter
@NoArgsConstructor
public class Escola {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 18)
    private String cnpj;

    @Column(length = 80)
    private String slug;

    @Column(length = 100)
    private String municipio;

    @Column(length = 2)
    private String uf;

    @Column(name = "package_id", length = 80)
    private String packageId;

    @Column(name = "nota_minima_aprovacao", nullable = false, precision = 4, scale = 2)
    private BigDecimal notaMinimaAprovacao = new BigDecimal("6.00");

    @Column(name = "frequencia_minima", nullable = false, precision = 5, scale = 2)
    private BigDecimal frequenciaMinima = new BigDecimal("75.00");

    @Column(name = "normativa_consultada_em")
    private Instant normativaConsultadaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
}
