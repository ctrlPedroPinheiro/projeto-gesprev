package iff.edu.br.gesprev.dto;

import iff.edu.br.gesprev.entity.enums.EmendaConstitucional;
import iff.edu.br.gesprev.entity.enums.NaturezaAposentadoria;
import iff.edu.br.gesprev.entity.enums.TipoCalculo;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtoAposentadoriaDTO(
    @NotNull(message = "Processo e obrigatorio")
    Long processoId,

    @NotNull(message = "Natureza da aposentadoria e obrigatoria")
    NaturezaAposentadoria naturezaAposentadoria,

    @NotNull(message = "Tipo de calculo e obrigatorio")
    TipoCalculo tipoCalculo,

    @NotNull(message = "Emenda constitucional e obrigatoria")
    EmendaConstitucional emendaConstitucional,

    @NotBlank(message = "Referencia salarial e obrigatoria")
    String referenciaSalarial,

    @NotNull(message = "Data de finalizacao e obrigatoria")
    LocalDate dataFinalizacao
) {}
