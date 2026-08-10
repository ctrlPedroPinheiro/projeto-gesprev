package iff.edu.br.gesprev.controller.rest;

import iff.edu.br.gesprev.dto.AtoAposentadoriaDTO;
import iff.edu.br.gesprev.service.AtoAposentadoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atos-aposentadoria")
@Tag(name = "Ato de aposentadoria", description = "Geracao do PDF final que encerra o processo.")
@SecurityRequirement(name = "bearerAuth")
public class AtoAposentadoriaController {

    private final AtoAposentadoriaService atoAposentadoriaService;

    public AtoAposentadoriaController(AtoAposentadoriaService atoAposentadoriaService) {
        this.atoAposentadoriaService = atoAposentadoriaService;
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar ato de aposentadoria", description = "Gera PDF do ato e finaliza o processo. Endpoint exclusivo do DIRETOR.")
    public ResponseEntity<byte[]> gerarAto(@Valid @RequestBody AtoAposentadoriaDTO dto) {
        byte[] pdf = atoAposentadoriaService.gerarAto(dto);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=ato_aposentadoria.pdf")
                .body(pdf);
    }
}
