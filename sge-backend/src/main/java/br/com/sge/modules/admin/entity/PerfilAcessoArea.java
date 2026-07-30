package br.com.sge.modules.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfil_acesso_area")
@IdClass(PerfilAcessoArea.Pk.class)
@Getter
@Setter
@NoArgsConstructor
public class PerfilAcessoArea {

    @Id
    @Column(nullable = false, length = 30)
    private String perfil;

    @Id
    @Column(nullable = false, length = 40)
    private String area;

    @Column(nullable = false)
    private boolean habilitado = true;

    public PerfilAcessoArea(String perfil, String area, boolean habilitado) {
        this.perfil = perfil;
        this.area = area;
        this.habilitado = habilitado;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private String perfil;
        private String area;

        public Pk(String perfil, String area) {
            this.perfil = perfil;
            this.area = area;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(perfil, pk.perfil) && Objects.equals(area, pk.area);
        }

        @Override
        public int hashCode() {
            return Objects.hash(perfil, area);
        }
    }
}
