package br.com.sge.modules.galeria.entity;

import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.entity.Usuario;
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

@Entity
@Table(name = "galeria_album")
@Getter
@Setter
@NoArgsConstructor
public class GaleriaAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "visivel_para", nullable = false, length = 100)
    private String visivelPara = "TODOS";

    /** Se true, álbum só deve ser exibido a famílias/alunos com autoriza_uso_imagem. */
    @Column(name = "exigir_consentimento_imagem", nullable = false)
    private boolean exigirConsentimentoImagem = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicado_por")
    private Usuario publicadoPor;

    @Column(name = "publicado_em", nullable = false)
    private Instant publicadoEm = Instant.now();
}
