package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iff.edu.br.gesprev.dto.PerguntaChatDTO;
import iff.edu.br.gesprev.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "IA/RAG", description = "Endpoint experimental de perguntas ao mecanismo RAG.")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    @Operation(summary = "Perguntar ao assistente", description = "Responde usando a legislacao indexada e ferramentas de consulta somente leitura da API GESPREV.")
    public ResponseEntity<String> chat(@Valid @RequestBody PerguntaChatDTO body) {
        return ResponseEntity.ok(ragService.responder(body.pergunta(), body.numeroProcesso()));
    }
}
