package iff.edu.br.gesprev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;

public interface ProcessoAposentadoriaRepository extends JpaRepository<ProcessoAposentadoria, Long>, JpaSpecificationExecutor<ProcessoAposentadoria> {
    ProcessoAposentadoria findByNumeroProcesso(int numeroProcesso);
    long countByStatus(StatusProcesso status);
}
