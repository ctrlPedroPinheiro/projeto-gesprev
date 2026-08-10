package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCpf(String cpf);
}
