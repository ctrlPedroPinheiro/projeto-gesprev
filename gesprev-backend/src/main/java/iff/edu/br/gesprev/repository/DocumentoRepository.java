package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import iff.edu.br.gesprev.entity.Documento;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByProcessoId(Long processoId);
    @Query("select d.caminhoArquivo from Documento d where d.processo.id = :processoId")
    List<String> findCaminhosByProcessoId(@Param("processoId") Long processoId);
    boolean existsByProcessoIdAndTipoDocumento(Long processoId, TipoDocumento tipoDocumento);
    void deleteByProcessoId(Long processoId);
}
