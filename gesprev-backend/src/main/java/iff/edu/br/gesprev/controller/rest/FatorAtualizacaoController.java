package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import iff.edu.br.gesprev.service.FatorAtualizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fatores-atualizacao")
@Tag(name = "Fatores de atualizacao", description = "Importacao e verificacao dos fatores usados no calculo.")
@SecurityRequirement(name = "bearerAuth")
public class FatorAtualizacaoController {

    private final FatorAtualizacaoService fatorAtualizacaoService;

    public FatorAtualizacaoController(FatorAtualizacaoService fatorAtualizacaoService) {
        this.fatorAtualizacaoService = fatorAtualizacaoService;
    }

    @PostMapping("/importar")
    @Operation(summary = "Importar fatores", description = "Importa planilha de fatores de atualizacao. Endpoint exclusivo do DIRETOR.")
    public ResponseEntity<String> importar(
            @Parameter(description = "Planilha XLSX com fatores")
            @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Identificacao da portaria ou origem dos fatores")
            @RequestParam("portaria") String portaria) {
        try {
            fatorAtualizacaoService.importarPlanilha(arquivo, portaria);
            return ResponseEntity.ok("Fatores importados com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao importar fatores: " + e.getMessage());
        }
    }

    @GetMapping("/verificar")
    @Operation(summary = "Verificar fatores", description = "Indica se existem fatores importados e informa o total cadastrado.")
    public ResponseEntity<Map<String, Object>> verificar() {
        long total = fatorAtualizacaoService.contarFatores();
        return ResponseEntity.ok(Map.of(
            "existem", total > 0,
            "total", total
        ));
    }
}
