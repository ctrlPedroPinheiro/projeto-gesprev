package iff.edu.br.gesprev.service.ai;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DocumentoTextoService {

    private static final Pattern ARGUMENTO = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)");

    private final boolean ocrEnabled;
    private final String ocrCommand;
    private final int ocrTimeoutSeconds;
    private final int maxPages;
    private final float ocrRenderDpi;

    public DocumentoTextoService(
            @Value("${gesprev.ai.ocr.enabled:false}") boolean ocrEnabled,
            @Value("${gesprev.ai.ocr.command:}") String ocrCommand,
            @Value("${gesprev.ai.ocr.timeout-seconds:120}") int ocrTimeoutSeconds,
            @Value("${gesprev.ai.vlm.max-pages:30}") int maxPages,
            @Value("${gesprev.ai.ocr.render-dpi:150}") float ocrRenderDpi) {
        this.ocrEnabled = ocrEnabled;
        this.ocrCommand = ocrCommand;
        this.ocrTimeoutSeconds = ocrTimeoutSeconds;
        this.maxPages = maxPages;
        this.ocrRenderDpi = ocrRenderDpi;
    }

    public String extrairTexto(Path arquivo) {
        String textoOcr = extrairComOcr(arquivo);
        return textoSuficiente(textoOcr) ? textoOcr : extrairTextoPesquisavel(arquivo);
    }

    public String extrairComOcr(Path arquivo) {
        try {
            String contentType = Files.probeContentType(arquivo);
            if (contentType == null) {
                contentType = arquivo.toString().toLowerCase().endsWith(".pdf")
                        ? "application/pdf"
                        : "application/octet-stream";
            }

            if ("application/pdf".equals(contentType)) {
                return ocrPdfEscaneado(arquivo);
            }

            if (contentType.startsWith("image/")) {
                return executarOcr(arquivo);
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public String extrairTextoPesquisavel(Path arquivo) {
        if (!arquivo.toString().toLowerCase().endsWith(".pdf")) {
            return "";
        }
        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setEndPage(Math.min(documento.getNumberOfPages(), maxPages));
            return normalizarTexto(stripper.getText(documento));
        } catch (Exception e) {
            return "";
        }
    }

    private String ocrPdfEscaneado(Path arquivo) throws IOException {
        if (!ocrDisponivel()) {
            return "";
        }
        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {
            int total = Math.min(documento.getNumberOfPages(), maxPages);
            PDFRenderer renderer = new PDFRenderer(documento);
            StringBuilder texto = new StringBuilder();

            for (int pagina = 0; pagina < total; pagina++) {
                Path imagemTemporaria = Files.createTempFile("gesprev-ocr-pagina-" + (pagina + 1), ".png");
                try {
                    BufferedImage imagem = renderer.renderImageWithDPI(pagina, ocrRenderDpi, ImageType.RGB);
                    ImageIO.write(imagem, "png", imagemTemporaria.toFile());
                    String textoPagina = executarOcr(imagemTemporaria);
                    if (!textoPagina.isBlank()) {
                        texto.append("\n\n[PAGINA ").append(pagina + 1).append("]\n").append(textoPagina);
                    }
                } finally {
                    Files.deleteIfExists(imagemTemporaria);
                }
            }
            return limitar(normalizarTexto(texto.toString()));
        }
    }

    private String executarOcr(Path arquivo) {
        if (!ocrDisponivel()) {
            return "";
        }
        try {
            List<String> comando = montarComando(arquivo);
            Process processo = new ProcessBuilder(comando)
                    .redirectErrorStream(true)
                    .start();

            boolean terminou = processo.waitFor(ocrTimeoutSeconds, TimeUnit.SECONDS);
            if (!terminou) {
                processo.destroyForcibly();
                return "";
            }

            String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (processo.exitValue() != 0) {
                return "";
            }
            return limitar(normalizarTexto(saida));
        } catch (Exception e) {
            return "";
        }
    }

    private boolean ocrDisponivel() {
        return ocrEnabled && ocrCommand != null && !ocrCommand.isBlank();
    }

    private List<String> montarComando(Path arquivo) {
        List<String> partes = dividirArgumentos(ocrCommand);
        String caminho = arquivo.toAbsolutePath().toString();
        List<String> comando = new ArrayList<>(partes.size());
        for (String parte : partes) {
            comando.add(parte.replace("{input}", caminho));
        }
        return comando;
    }

    private List<String> dividirArgumentos(String comando) {
        List<String> partes = new ArrayList<>();
        Matcher matcher = ARGUMENTO.matcher(comando);
        while (matcher.find()) {
            String parte = matcher.group(1);
            if (parte == null) {
                parte = matcher.group(2);
            }
            if (parte == null) {
                parte = matcher.group(3);
            }
            partes.add(parte);
        }
        return partes;
    }

    private boolean textoSuficiente(String texto) {
        return texto != null && texto.replaceAll("\\s+", "").length() >= 80;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\u0000", "")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String limitar(String texto) {
        if (texto == null) {
            return "";
        }
        int limite = 60_000;
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    public Duration timeoutOcr() {
        return Duration.ofSeconds(ocrTimeoutSeconds);
    }
}
