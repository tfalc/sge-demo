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
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matriz_componente")
@Getter
@Setter
@NoArgsConstructor
public class MatrizComponente {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "matriz_id", nullable = false)
  private MatrizCurricular matriz;

  @Column(nullable = false, length = 120)
  private String componente;

  @Column(length = 80)
  private String area;

  @Column(name = "aulas_semanais", nullable = false)
  private int aulasSemanais;

  @Column(name = "aulas_semanais_min")
  private Integer aulasSemanaisMin;

  @Column(name = "aulas_semanais_max")
  private Integer aulasSemanaisMax;

  @Column(name = "base_nacional_comum", nullable = false)
  private boolean baseNacionalComum = true;

  @Column(nullable = false)
  private int ordem;
}
