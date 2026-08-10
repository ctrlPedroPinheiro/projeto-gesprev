package iff.edu.br.gesprev.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${upload.dir}")
    private String uploadDir;

    public String salvarArquivo(MultipartFile arquivo, Long processoId) throws IOException {
        Path diretorio = Paths.get(uploadDir, "processo_" + processoId);
        Files.createDirectories(diretorio);

        return copiarArquivo(arquivo, diretorio);
    }

    public String salvarArquivoTemporario(MultipartFile arquivo, String subpasta) throws IOException {
        Path diretorio = Paths.get(uploadDir, subpasta);
        Files.createDirectories(diretorio);

        return copiarArquivo(arquivo, diretorio);
    }

    private String copiarArquivo(MultipartFile arquivo, Path diretorio) throws IOException {
        String nomeOriginal = arquivo.getOriginalFilename() == null ? "arquivo" : arquivo.getOriginalFilename();
        String nomeSeguro = Paths.get(nomeOriginal).getFileName().toString();
        String nomeUnico = UUID.randomUUID() + "_" + nomeSeguro;
        Path destino = diretorio.resolve(nomeUnico);
        Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return destino.toString();
    }

    public byte[] lerArquivo(String caminho) throws IOException {
        return Files.readAllBytes(Paths.get(caminho));
    }

    public void deletarArquivo(String caminho) throws IOException {
        Path arquivo = resolverCaminhoSeguro(caminho);
        Files.deleteIfExists(arquivo);
        Path diretorio = arquivo.getParent();
        if (diretorio != null && !diretorio.equals(raizUpload()) && Files.isDirectory(diretorio)) {
            try (var itens = Files.list(diretorio)) {
                if (itens.findAny().isEmpty()) {
                    Files.deleteIfExists(diretorio);
                }
            }
        }
    }

    public void deletarArquivosAposCommit(List<String> caminhos) {
        Runnable exclusao = () -> caminhos.forEach(caminho -> {
            try {
                deletarArquivo(caminho);
            } catch (IOException | IllegalArgumentException ignored) {
                // O banco ja confirmou a exclusao; uma rotina de limpeza pode tratar falhas de storage.
            }
        });

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    exclusao.run();
                }
            });
        } else {
            exclusao.run();
        }
    }

    private Path resolverCaminhoSeguro(String caminho) {
        Path raiz = raizUpload();
        Path informado = Paths.get(caminho);
        Path resolvido = informado.isAbsolute()
                ? informado.normalize()
                : Paths.get("").toAbsolutePath().resolve(informado).normalize();
        if (!resolvido.startsWith(raiz)) {
            throw new IllegalArgumentException("Arquivo fora do diretorio de uploads");
        }
        return resolvido;
    }

    private Path raizUpload() {
        Path raiz = Paths.get(uploadDir);
        return raiz.isAbsolute()
                ? raiz.normalize()
                : Paths.get("").toAbsolutePath().resolve(raiz).normalize();
    }
}
