package iff.edu.br.gesprev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.Holerite;

public interface HoleriteRepository extends JpaRepository<Holerite, Long> {
    List<Holerite> findByProcessoId(Long processoId);
}
