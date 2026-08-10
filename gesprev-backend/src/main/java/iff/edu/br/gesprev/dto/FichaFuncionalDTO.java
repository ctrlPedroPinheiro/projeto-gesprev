package iff.edu.br.gesprev.dto;

import java.time.LocalDate;

import iff.edu.br.gesprev.entity.enums.Sexo;

public record FichaFuncionalDTO(
    String matricula,       
    String nome,            
    LocalDate dtNascimento, 
    String cpf,             
    String pis,
    Sexo sexo,
    String email,           
    String cargo,           
    String orgao,           
    LocalDate dtAdmissao    
) {}
