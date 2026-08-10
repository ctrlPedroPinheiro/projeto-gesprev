package iff.edu.br.gesprev.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import iff.edu.br.gesprev.entity.FatorAtualizacao;

public interface FatorAtualizacaoRepository extends JpaRepository<FatorAtualizacao, Long> {
    Optional<FatorAtualizacao> findByMesReferencia(LocalDate mesReferencia);
}