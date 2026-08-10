package iff.edu.br.gesprev.controller.rest;

import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iff.edu.br.gesprev.dto.CriarProcessoComServidorDTO;
import iff.edu.br.gesprev.dto.ProcessoAposentadoriaDTO;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import iff.edu.br.gesprev.security.UsuarioAutenticado;
import iff.edu.br.gesprev.service.FileStorageService;
import iff.edu.br.gesprev.service.ProcessoAposentadoriaService;
import iff.edu.br.gesprev.service.VLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/processos-aposentadoria")
@Tag(name = "Processos", description = "Criacao, consulta e exclusao dos processos de aposentadoria.")
@SecurityRequirement(name = "bearerAuth")
public class ProcessoAposentadoriaController {

    private final ProcessoAposentadoriaService processoAposentadoriaService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final FileStorageService fileStorageService;
    private final VLMService vlmService;

    public ProcessoAposentadoriaController(
            ProcessoAposentadoriaService processoAposentadoriaService,
            UsuarioAutenticado usuarioAutenticado,
            FileStorageService fileStorageService,
            VLMService vlmService) {
        this.processoAposentadoriaService = processoAposentadoriaService;
        this.usuarioAutenticado = usuarioAutenticado;
        this.fileStorageService = fileStorageService;
        this.vlmService = vlmService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter processo por id", description = "Consulta os dados principais do processo.")
    public ResponseEntity<ProcessoAposentadoriaDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(processoAposentadoriaService.obterProcessoAposentadoriaPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir processo", description = "Remove o processo e seus registros auxiliares. Endpoint exclusivo do DIRETOR.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        processoAposentadoriaService.deletarProcessoAposentadoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatisticas", description = "Retorna total de processos e contagens por status usados no painel.")
    public ResponseEntity<Map<String, Long>> estatisticas() {
        Map<String, Long> stats = Map.of(
            "total", processoAposentadoriaService.contarTotal(),
            "finalizados", processoAposentadoriaService.contarPorStatus(StatusProcesso.FINALIZADO),
            "pendentes", processoAposentadoriaService.contarPorStatus(StatusProcesso.PENDENTE_DOCUMENTO),
            "emAnalise", processoAposentadoriaService.contarPorStatus(StatusProcesso.EM_ANALISE)
        );
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/com-servidor")
    @Operation(summary = "Criar processo com servidor", description = "Endpoint oficial de abertura: cria servidor, processo, checklist e historico inicial em uma unica operacao.")
    public ResponseEntity<ProcessoAposentadoriaDTO> criarComServidor(
            @Valid @RequestBody CriarProcessoComServidorDTO dto) {
        Long usuarioId = usuarioAutenticado.obterUsuarioId();
        ProcessoAposentadoriaDTO criado = processoAposentadoriaService.criarProcessoComServidor(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/preprocessar-ficha-funcional")
    @Operation(summary = "Preprocessar ficha funcional para abertura", description = "Extrai dados de uma ficha funcional antes da criacao do processo. O retorno deve ser revisado pelo analista antes da abertura.")
    public ResponseEntity<Map<String, Object>> preprocessarFichaFuncional(
            @Parameter(description = "Arquivo PDF ou imagem da ficha funcional")
            @RequestParam("arquivo") MultipartFile arquivo) throws Exception {
        String caminho = fileStorageService.salvarArquivoTemporario(arquivo, "preprocessamento_ficha_funcional");
        try {
            Map<String, Object> dados = vlmService.preprocessarFichaFuncionalAbertura(Path.of(caminho));
            return ResponseEntity.ok(dados);
        } finally {
            fileStorageService.deletarArquivo(caminho);
        }
    }

    @GetMapping
    @Operation(summary = "Buscar processos", description = "Lista processos com filtros opcionais por numero, status, periodo e dados do servidor.")
    public ResponseEntity<List<ProcessoAposentadoriaDTO>> buscar(
            @Parameter(description = "Numero exato do processo")
            @RequestParam(required = false) Integer numeroProcesso,
            @Parameter(description = "Status atual do processo")
            @RequestParam(required = false) StatusProcesso status,
            @Parameter(description = "Data/hora inicial de criacao")
            @RequestParam(required = false) LocalDateTime dtCriacaoInicio,
            @Parameter(description = "Data/hora final de criacao")
            @RequestParam(required = false) LocalDateTime dtCriacaoFim,
            @Parameter(description = "Trecho do nome do servidor")
            @RequestParam(required = false) String nomeServidor,
            @Parameter(description = "CPF do servidor")
            @RequestParam(required = false) String cpfServidor) {

        List<ProcessoAposentadoriaDTO> processos = processoAposentadoriaService.buscarProcessos(
                numeroProcesso, status, dtCriacaoInicio, dtCriacaoFim, nomeServidor, cpfServidor);

        return ResponseEntity.ok(processos);
    }
}
