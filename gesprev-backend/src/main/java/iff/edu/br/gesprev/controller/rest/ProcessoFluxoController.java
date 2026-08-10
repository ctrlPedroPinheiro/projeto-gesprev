package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.service.ProcessoFluxoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/processos-aposentadoria")
@Tag(name = "Fluxo do processo", description = "Transicoes manuais de excecao no fluxo do processo.")
@SecurityRequirement(name = "bearerAuth")
public class ProcessoFluxoController {

    private final ProcessoFluxoService processoFluxoService;

    public ProcessoFluxoController(ProcessoFluxoService processoFluxoService) {
        this.processoFluxoService = processoFluxoService;
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeitar processo", description = "Rejeita processo em calculo, registrando observacao no historico.")
    public ResponseEntity<Void> rejeitar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        processoFluxoService.rejeitar(id, body.get("observacao"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reabrir")
    @Operation(summary = "Reabrir processo", description = "Reabre processo rejeitado para analise, registrando observacao no historico.")
    public ResponseEntity<Void> reabrir(@PathVariable Long id, @RequestBody Map<String, String> body) {
        processoFluxoService.reabrir(id, body.get("observacao"));
        return ResponseEntity.noContent().build();
    }
}
