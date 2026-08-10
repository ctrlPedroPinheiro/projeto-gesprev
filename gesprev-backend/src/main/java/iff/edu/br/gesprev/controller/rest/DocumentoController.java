package iff.edu.br.gesprev.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iff.edu.br.gesprev.dto.DocumentoDTO;
import iff.edu.br.gesprev.dto.AtualizarJsonDocumentoDTO;
import iff.edu.br.gesprev.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import iff.edu.br.gesprev.entity.Documento;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Upload, download e consulta de documentos vinculados ao processo.")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter documento por id", description = "Consulta metadados do documento. O arquivo fisico fica no storage local.")
    public ResponseEntity<DocumentoDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.obterDocumentoPorId(id));
    }

    @GetMapping("/processo/{processoId}")
    @Operation(summary = "Listar documentos do processo", description = "Retorna todos os documentos anexados ao processo.")
    public ResponseEntity<List<DocumentoDTO>> obterPorProcesso(@PathVariable Long processoId) {
        List<DocumentoDTO> documentos = documentoService.obterDocumentosPorProcessoId(processoId);
        return ResponseEntity.ok(documentos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir documento", description = "Remove documento e arquivo fisico. Endpoint exclusivo do DIRETOR.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        documentoService.deletarDocumento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/json-extraido")
    @Operation(summary = "Corrigir JSON extraido", description = "Permite ao analista revisar e corrigir manualmente os dados extraidos pela IA antes da validacao.")
    public ResponseEntity<DocumentoDTO> atualizarJsonExtraido(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarJsonDocumentoDTO dto) throws Exception {
        return ResponseEntity.ok(documentoService.atualizarJsonExtraido(id, dto.jsonExtraido()));
    }

    @PostMapping("/upload")
    @Operation(summary = "Anexar documento", description = "Upload real do arquivo. Tambem marca o item correspondente do checklist como entregue.")
    public ResponseEntity<DocumentoDTO> upload(
            @Parameter(description = "Arquivo PDF a ser anexado")
            @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Id do processo")
            @RequestParam("processoId") Long processoId,
            @Parameter(description = "Tipo do documento: FICHA_FUNCIONAL, FICHA_FINANCEIRA, HOLERITE ou CTS")
            @RequestParam("tipoDocumento") String tipoDocumento) throws Exception {
        DocumentoDTO dto = documentoService.salvarComArquivo(arquivo, processoId, tipoDocumento);
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Baixar documento", description = "Retorna o arquivo fisico associado ao documento.")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {
        byte[] arquivo = documentoService.baixarArquivo(id);
        Documento doc = documentoService.obterDocumentoEntidadePorId(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=" + doc.getNomeOriginal())
                .body(arquivo);
    }
}
