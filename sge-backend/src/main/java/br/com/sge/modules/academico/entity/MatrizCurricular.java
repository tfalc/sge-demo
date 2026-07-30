package br.com.sge.modules.academico.entity;

import br.com.sge.modules.cadastro.entity.Escola;
import br.com.sge.modules.cadastro.entity.Serie;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matriz_curricular")
@Getter
@Setter
@NoArgsConstructor
public class MatrizCurricular {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "escola_id", nullable = false)
  private Escola escola;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "serie_id")
  private Serie serie;

  @Column(nullable = false, length = 80)
  private String codigo;

  @Column(nullable = false, length = 200)
  private String nome;

  @Column(nullable = false, length = 40)
  private String etapa;

  @Column(nullable = false, length = 40)
  private String modalidade;

  @Column(name = "modo_validacao", nullable = false, length = 20)
  private String modoValidacao = "NORMATIVO";

  @Column(name = "aulas_semanais_total", nullable = false)
  private int aulasSemanaisTotal;

  @Column(name = "aulas_semanais_total_min")
  private Integer aulasSemanaisTotalMin;

  @Column(name = "aulas_semanais_total_max")
  private Integer aulasSemanaisTotalMax;

  @Column(name = "minutos_aula", nullable = false)
  private int minutosAula = 50;

  @Column(name = "horas_anuais_minimas", nullable = false)
  private int horasAnuaisMinimas = 800;

  @Column(name = "normativa_ref")
  private String normativaRef;

  @Column(nullable = false)
  private boolean ativo = true;

  @Column(name = "sincronizada_normativa_em")
  private Instant sincronizadaNormativaEm;

  @Column(name = "criado_em", nullable = false)
  private Instant criadoEm = Instant.now();

  @OneToMany(mappedBy = "matriz")
  private List<MatrizComponente> componentes = new ArrayList<>();
}
