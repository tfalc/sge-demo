package br.com.sge.modules.cadastro.repository;

import br.com.sge.modules.cadastro.entity.Pessoa;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {}
