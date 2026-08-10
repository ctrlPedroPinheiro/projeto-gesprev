package iff.edu.br.gesprev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.MemoriaCalculo;

public interface MemoriaCalculoRepository extends JpaRepository<MemoriaCalculo, Long> {
    Optional<MemoriaCalculo> findByProcessoId(Long processoId);
    void deleteByProcessoId(Long processoId);
}
