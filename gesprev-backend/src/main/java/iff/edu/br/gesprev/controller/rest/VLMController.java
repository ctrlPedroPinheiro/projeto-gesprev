package iff.edu.br.gesprev.controller.rest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.dto.FichaFuncionalDTO;
import iff.edu.br.gesprev.service.ValidarDocumentoService;
import iff.edu.br.gesprev.service.VLMService;
import iff.edu.br.gesprev.service.ai.AiGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/vlm")
@Tag(name = "IA/VLM", description = "Endpoints de apoio a extracao e validacao por IA. Integracao final sera tratada em etapa posterior.")
@SecurityRequirement(name = "bearerAuth")
public class VLMController {

    private final VLMService vlmService;
    private final ValidarDocumentoService validarDocumentoService;
    private final AiGateway aiGateway;

    public VLMController(VLMService vlmService, ValidarDocumentoService validarDocumentoService, AiGateway aiGateway) {
        this.vlmService = vlmService;
        this.validarDocumentoService = validarDocumentoService;
        this.aiGateway = aiGateway;
    }

    @GetMapping("/status")
    @Operation(summary = "Consultar configuracao da IA", description = "Informa o provedor ativo e se as respostas sao simuladas, sem expor credenciais.")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "provedor", aiGateway.provedor(),
                "simulado", aiGateway.simulado()));
    }

    @PostMapping("/teste-conexao")
    @Operation(summary = "Testar conexao com a IA", description = "Envia uma mensagem neutra ao provedor ativo sem transmitir documentos ou dados pessoais.")
    public ResponseEntity<Map<String, Object>> testarConexao() {
        String resposta = aiGateway.responder(
                "Responda somente com a palavra OK.",
                "Teste de conectividade do sistema GESPREV.");
        return ResponseEntity.ok(Map.of(
                "provedor", aiGateway.provedor(),
                "resposta", resposta));
    }

    // Automático — chamado internamente no upload da ficha funcional
    @PostMapping("/ficha-funcional/{documentoId}")
    @Operation(summary = "Processar ficha funcional", description = "Extrai dados da ficha funcional usando VLM. Endpoint de apoio futuro ao fluxo.")
    public ResponseEntity<FichaFuncionalDTO> processarFichaFuncional(@PathVariable Long documentoId) {
        return ResponseEntity.ok(vlmService.processarFichaFuncional(documentoId));
    }

    // Manual — analista aciona para processar os demais documentos
    @PostMapping("/processar/{documentoId}")
    @Operation(summary = "Processar documento", description = "Executa extracao por VLM e grava JSON extraido no documento.")
    public ResponseEntity<String> processarDocumento(@PathVariable Long documentoId) {
        return ResponseEntity.ok(vlmService.processarDocumento(documentoId));
    }

    // Analista valida o JSON extraído
    @PatchMapping("/validar/{documentoId}")
    @Operation(summary = "Confirmar dados extraidos", description = "Confirma JSON extraido, persiste dados especializados e marca checklist como validado.")
    public ResponseEntity<Void> validarDocumento(@PathVariable Long documentoId) {
        validarDocumentoService.confirmarDadosExtraidos(documentoId);
        return ResponseEntity.noContent().build();
    }
}
