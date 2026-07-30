package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import br.com.sge.modules.cadastro.entity.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailIgnoreCaseAndAtivoTrue(String email);

    List<Usuario> findByPerfilAndAtivoTrue(PerfilUsuario perfil);

    List<Usuario> findByAtivoTrue();

    @org.springframework.data.jpa.repository.Query("""
            select u from Usuario u
            left join fetch u.pessoa
            order by u.email
            """)
    List<Usuario> findAllComPessoa();
}
