package br.com.sge.modules.rematricula.entity;

import br.com.sge.modules.cadastro.entity.AnoLetivo;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rematricula_config")
@Getter
@Setter
@NoArgsConstructor
public class RematriculaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ano_letivo_id")
    private AnoLetivo anoLetivo;

    @Column(nullable = false)
    private Boolean habilitada = false;

    @Column(nullable = false, length = 200)
    private String titulo = "Rematricula";

    @Column(name = "pdf_modelo_nome", length = 255)
    private String pdfModeloNome;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_modelo_conteudo")
    private byte[] pdfModeloConteudo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formulario_json", nullable = false, columnDefinition = "jsonb")
    private String formularioJson = "{\"secoes\":[]}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sugestoes_extracao_json", columnDefinition = "jsonb")
    private String sugestoesExtracaoJson;

    @Column(name = "publicado_em")
    private Instant publicadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();
}
