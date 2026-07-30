package br.com.sge.modules.comunicacao.entity;

import br.com.sge.modules.cadastro.entity.Usuario;
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
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cardapio")
@Getter
@Setter
@NoArgsConstructor
public class Cardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_refeicao", nullable = false)
    private LocalDate dataRefeicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_refeicao", nullable = false, length = 30)
    private TipoRefeicao tipoRefeicao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    private Integer calorias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id")
    private Usuario nutricionista;
}
