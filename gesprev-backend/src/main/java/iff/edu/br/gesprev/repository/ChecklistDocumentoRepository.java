package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iff.edu.br.gesprev.entity.ChecklistDocumento;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import java.util.List;
import java.util.Optional;

public interface ChecklistDocumentoRepository extends JpaRepository<ChecklistDocumento, Long> {
    List<ChecklistDocumento> findByProcessoId(Long processoId);
    Optional<ChecklistDocumento> findByProcessoIdAndTipoDocumento(Long processoId, TipoDocumento tipoDocumento);
    void deleteByProcessoId(Long processoId);
}
