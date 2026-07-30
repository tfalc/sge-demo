package br.com.sge.modules.admin.service;

import br.com.sge.modules.admin.dto.AtualizarUsuarioRequest;
import br.com.sge.modules.cadastro.entity.Usuario;
import br.com.sge.modules.cadastro.repository.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAdminService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarUsuarios() {
        return usuarioRepository.findAllComPessoa().stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> atualizarUsuario(UUID id, AtualizarUsuarioRequest req) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        String emailLogado = emailLogado();
        if (usuario.getEmail().equalsIgnoreCase(emailLogado) && !Boolean.TRUE.equals(req.ativo())) {
            throw new IllegalArgumentException("Voce nao pode desativar seu proprio usuario");
        }
        usuario.setPerfil(req.perfil());
        usuario.setAtivo(req.ativo());
        return toMap(usuarioRepository.save(usuario));
    }

    private Map<String, Object> toMap(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("email", u.getEmail());
        m.put("nome", u.getPessoa() != null ? u.getPessoa().getNome() : null);
        m.put("perfil", u.getPerfil().name());
        m.put("ativo", u.getAtivo());
        return m;
    }

    private String emailLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return "";
        }
        return auth.getPrincipal().toString();
    }
}
