package br.com.sge.modules.admin.repository;

import br.com.sge.modules.admin.entity.PerfilAcessoArea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilAcessoAreaRepository extends JpaRepository<PerfilAcessoArea, PerfilAcessoArea.Pk> {

    List<PerfilAcessoArea> findByPerfil(String perfil);

    List<PerfilAcessoArea> findByPerfilAndHabilitadoTrue(String perfil);
}
