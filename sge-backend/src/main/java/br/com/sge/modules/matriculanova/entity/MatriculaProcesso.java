package br.com.sge.modules.matriculanova.entity;

import br.com.sge.modules.cadastro.entity.Aluno;
import br.com.sge.modules.cadastro.entity.AnoLetivo;
import br.com.sge.modules.cadastro.entity.Responsavel;
import br.com.sge.modules.cadastro.entity.Turma;
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
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matricula_processo")
@Getter
@Setter
@NoArgsConstructor
public class MatriculaProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ano_letivo_id", nullable = false)
    private AnoLetivo anoLetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_pretendida_id")
    private Turma turmaPretendida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Responsavel responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusMatriculaProcesso status = StatusMatriculaProcesso.RASCUNHO;

    @Column(name = "candidato_nome", nullable = false, length = 200)
    private String candidatoNome;

    @Column(name = "matricula_sugerida", length = 20)
    private String matriculaSugerida;

    @Column(name = "responsavel_nome", length = 200)
    private String responsavelNome;

    @Column(name = "responsavel_email", length = 200)
    private String responsavelEmail;

    @Column(name = "responsavel_telefone", length = 30)
    private String responsavelTelefone;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_usuario_id")
    private Usuario criadoPorUsuario;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    @Column(name = "aprovado_em")
    private Instant aprovadoEm;

    @Column(name = "rejeitado_em")
    private Instant rejeitadoEm;

    @Column(name = "concluido_em")
    private Instant concluidoEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();
}
