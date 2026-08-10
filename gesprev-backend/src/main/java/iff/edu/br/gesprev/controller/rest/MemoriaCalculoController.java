package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.dto.MemoriaCalculoDTO;
import iff.edu.br.gesprev.service.MemoriaCalculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/memorias-calculo")
@Tag(name = "Memoria de calculo", description = "Geracao e consulta da memoria de calculo previdenciaria.")
@SecurityRequirement(name = "bearerAuth")
public class MemoriaCalculoController {

    private final MemoriaCalculoService memoriaCalculoService;

    public MemoriaCalculoController(MemoriaCalculoService memoriaCalculoService) {
        this.memoriaCalculoService = memoriaCalculoService;
    }

    @PostMapping("/calcular/{processoId}")
    @Operation(summary = "Gerar memoria de calculo", description = "Gera memoria apenas quando todos os documentos obrigatorios estiverem entregues e validados.")
    public ResponseEntity<MemoriaCalculoDTO> calcular(
            @PathVariable Long processoId,
            @Parameter(description = "Tipo de calculo: INTEGRAL ou PROPORCIONAL")
            @RequestParam String tipoCalculo) {
        return ResponseEntity.ok(memoriaCalculoService.calcular(processoId, tipoCalculo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter memoria por id", description = "Consulta uma memoria de calculo pelo id.")
    public ResponseEntity<MemoriaCalculoDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(memoriaCalculoService.obterMemoriaCalculoPorId(id));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Obter memoria por processo", description = "Consulta a memoria de calculo vinculada ao processo.")
    public ResponseEntity<MemoriaCalculoDTO> obterPorProcesso(@PathVariable Long processoId) {
        return ResponseEntity.ok(memoriaCalculoService.obterMemoriaCalculoPorProcessoId(processoId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir memoria", description = "Remove a memoria de calculo. Endpoint exclusivo do DIRETOR.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        memoriaCalculoService.deletarMemoriaCalculo(id);
        return ResponseEntity.noContent().build();
    }
}
