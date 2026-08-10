package iff.edu.br.gesprev.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusProcesso;

public record InformacoesProcessoDTO(
        String nome,
        LocalDate dtNascimento,
        String cpf,
        String email,
        String matricula,
        String cargo,
        String orgao,
        LocalDate dtAdmissao,
        int numeroProcesso,
        LocalDateTime dtCriacao,
        StatusProcesso status,
        LocalDateTime dtAtualizacao
) {
}
