package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import iff.edu.br.gesprev.entity.Cts;

public interface CtsRepository extends JpaRepository<Cts, Long> {
    Optional<Cts> findByProcessoId(Long processoId);

}
