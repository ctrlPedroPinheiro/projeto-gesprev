package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iff.edu.br.gesprev.entity.HistoricoProcesso;
import java.util.List;

public interface HistoricoProcessoRepository extends JpaRepository<HistoricoProcesso, Long> {
    List<HistoricoProcesso> findByProcessoId(Long processoId);
    void deleteByProcessoId(Long processoId);
}
