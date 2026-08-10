package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.dto.ChecklistDocumentoDTO;
import iff.edu.br.gesprev.service.ChecklistDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/checklist-documentos")
@Tag(name = "Checklist", description = "Consulta do checklist de documentos obrigatorios do processo.")
@SecurityRequirement(name = "bearerAuth")
public class ChecklistDocumentoController {

    private final ChecklistDocumentoService checklistDocumentoService;

    public ChecklistDocumentoController(ChecklistDocumentoService checklistDocumentoService) {
        this.checklistDocumentoService = checklistDocumentoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter item do checklist", description = "Consulta um item do checklist pelo id.")
    public ResponseEntity<ChecklistDocumentoDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(checklistDocumentoService.obterChecklistDocumentoPorId(id));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar checklist do processo", description = "Retorna os documentos esperados, entregues e validados. O checklist e atualizado pelo fluxo interno.")
    public ResponseEntity<List<ChecklistDocumentoDTO>> obterPorProcesso(@PathVariable Long processoId) {
        List<ChecklistDocumentoDTO> checklist = checklistDocumentoService.obterChecklistDocumentosPorProcessoId(processoId);
        return ResponseEntity.ok(checklist);
    }

}
