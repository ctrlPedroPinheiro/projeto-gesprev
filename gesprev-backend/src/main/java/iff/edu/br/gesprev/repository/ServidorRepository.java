package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.Servidor;

public interface ServidorRepository extends JpaRepository<Servidor, Long> {
    Servidor findByCpf(String cpf);
}
