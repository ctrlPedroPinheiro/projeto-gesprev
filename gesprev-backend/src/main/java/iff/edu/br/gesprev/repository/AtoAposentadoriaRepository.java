package iff.edu.br.gesprev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.AtoAposentadoria;

public interface AtoAposentadoriaRepository extends JpaRepository<AtoAposentadoria, Long> {
    Optional<AtoAposentadoria> findByProcessoId(Long processoId);
}
