package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import iff.edu.br.gesprev.dto.ServidorDTO;
import iff.edu.br.gesprev.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/servidores")
@Tag(name = "Servidores", description = "Consulta de servidores vinculados aos processos.")
@SecurityRequirement(name = "bearerAuth")
public class ServidorController {

    private final ServidorService servidorService;

    public ServidorController(ServidorService servidorService) {
        this.servidorService = servidorService;
    }

    @GetMapping
    @Operation(summary = "Listar servidores", description = "Lista servidores cadastrados. A criacao ocorre pelo endpoint de processo com servidor.")
    public ResponseEntity<List<ServidorDTO>> listarTodos() {
        return ResponseEntity.ok(servidorService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter servidor por id", description = "Consulta os dados do servidor.")
    public ResponseEntity<ServidorDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servidorService.obterServidorPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar servidor", description = "Atualiza os dados cadastrais do servidor vinculado ao processo.")
    public ResponseEntity<ServidorDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ServidorDTO dto) {
        return ResponseEntity.ok(servidorService.atualizarServidor(id, dto));
    }

}
