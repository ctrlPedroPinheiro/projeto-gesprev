package iff.edu.br.gesprev.dto;

import java.time.LocalDateTime;

import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoDTO(
    Long id,

    @NotBlank(message = "Nome do arquivo e obrigatorio")
    String nomeArquivo,

    @NotBlank(message = "Caminho do arquivo e obrigatorio")
    String caminhoArquivo,

    @NotNull(message = "Tipo do documento e obrigatorio")
    TipoDocumento tipoDocumento,

    StatusVLM statusVLM,
    LocalDateTime dtUpload,

    @NotNull(message = "Processo e obrigatorio")
    Long processoId,

    String nomeOriginal,
    String jsonExtraido
) {

}
