package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.dto.HistoricoProcessoDTO;
import iff.edu.br.gesprev.service.HistoricoProcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/historicos-processo")
@Tag(name = "Historico", description = "Consulta da trilha de estados do processo.")
@SecurityRequirement(name = "bearerAuth")
public class HistoricoProcessoController {

    private final HistoricoProcessoService historicoProcessoService;

    public HistoricoProcessoController(HistoricoProcessoService historicoProcessoService) {
        this.historicoProcessoService = historicoProcessoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter historico por id", description = "Consulta um registro especifico do historico.")
    public ResponseEntity<HistoricoProcessoDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historicoProcessoService.obterHistoricoPorId(id));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar historico do processo", description = "Retorna a trilha de alteracoes de status gerada automaticamente pelo fluxo.")
    public ResponseEntity<List<HistoricoProcessoDTO>> obterPorProcesso(@PathVariable Long processoId) {
        List<HistoricoProcessoDTO> historicos = historicoProcessoService.obterHistoricosPorProcessoId(processoId);
        return ResponseEntity.ok(historicos);
    }

}
