package br.com.sge.modules.patrimonio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "patrimonio_item")
@Getter
@Setter
public class PatrimonioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 80)
    private String categoria;

    @Column(length = 200)
    private String localizacao;

    @Column(name = "numero_patrimonio", length = 50)
    private String numeroPatrimonio;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(name = "valor_aquisicao", precision = 12, scale = 2)
    private BigDecimal valorAquisicao;

    @Column(nullable = false, length = 20)
    private String status = "ATIVO";

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
}
