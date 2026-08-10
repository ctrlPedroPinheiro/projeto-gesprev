package iff.edu.br.gesprev.service.ai;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Service
public class PdfMediaService {

    private final int maxPages;
    private final float renderDpi;

    public PdfMediaService(
            @Value("${gesprev.ai.vlm.max-pages:30}") int maxPages,
            @Value("${gesprev.ai.vlm.render-dpi:120}") float renderDpi) {
        this.maxPages = maxPages;
        this.renderDpi = renderDpi;
    }

    public List<Media> carregar(Path arquivo) throws IOException {
        String contentType = Files.probeContentType(arquivo);
        if (contentType == null) {
            contentType = arquivo.toString().toLowerCase().endsWith(".pdf")
                    ? "application/pdf"
                    : "application/octet-stream";
        }

        if ("application/pdf".equals(contentType)) {
            return renderizarPdf(arquivo);
        }
        if (contentType.startsWith("image/")) {
            return List.of(new Media(MimeType.valueOf(contentType), recurso(Files.readAllBytes(arquivo), arquivo.getFileName().toString())));
        }
        throw new IllegalArgumentException("Formato nao suportado pelo VLM: " + contentType);
    }

    private List<Media> renderizarPdf(Path arquivo) throws IOException {
        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {
            if (documento.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("O PDF nao possui paginas");
            }

            int total = Math.min(documento.getNumberOfPages(), maxPages);
            PDFRenderer renderer = new PDFRenderer(documento);
            List<Media> paginas = new ArrayList<>(total);

            for (int pagina = 0; pagina < total; pagina++) {
                BufferedImage imagem = renderer.renderImageWithDPI(pagina, renderDpi, ImageType.RGB);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(imagem, "png", output);
                paginas.add(new Media(MimeTypeUtils.IMAGE_PNG,
                        recurso(output.toByteArray(), "pagina-" + (pagina + 1) + ".png")));
            }
            return paginas;
        }
    }

    private ByteArrayResource recurso(byte[] conteudo, String nome) {
        return new ByteArrayResource(conteudo) {
            @Override
            public String getFilename() {
                return nome;
            }
        };
    }
}
