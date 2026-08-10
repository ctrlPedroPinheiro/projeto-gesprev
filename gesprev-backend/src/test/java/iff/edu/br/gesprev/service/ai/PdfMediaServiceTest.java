package iff.edu.br.gesprev.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfMediaServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void deveConverterCadaPaginaDoPdfEmImagem() throws Exception {
        Path arquivo = tempDir.resolve("documento.pdf");
        try (PDDocument pdf = new PDDocument()) {
            pdf.addPage(new PDPage());
            pdf.addPage(new PDPage());
            pdf.save(arquivo.toFile());
        }

        PdfMediaService service = new PdfMediaService(10, 72);

        assertEquals(2, service.carregar(arquivo).size());
    }
}
