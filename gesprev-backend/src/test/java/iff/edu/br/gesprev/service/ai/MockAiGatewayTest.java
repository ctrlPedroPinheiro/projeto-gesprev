package iff.edu.br.gesprev.service.ai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iff.edu.br.gesprev.dto.CtsDTO;
import iff.edu.br.gesprev.dto.FichaFinanceiraDTO;
import iff.edu.br.gesprev.dto.FichaFuncionalDTO;
import iff.edu.br.gesprev.dto.HoleriteDTO;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;

class MockAiGatewayTest {

    private final MockAiGateway gateway = new MockAiGateway();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void deveGerarJsonCompativelComTodosOsDocumentos() {
        validar(TipoDocumento.FICHA_FUNCIONAL, FichaFuncionalDTO.class);
        validar(TipoDocumento.HOLERITE, HoleriteDTO.class);
        validar(TipoDocumento.FICHA_FINANCEIRA, FichaFinanceiraDTO.class);
        validar(TipoDocumento.CTS, CtsDTO.class);
    }

    @Test
    void deveResponderSemServicoExterno() {
        String resposta = gateway.responder("instrucao", "Como testar?");

        assertTrue(gateway.simulado());
        assertTrue(resposta.contains("MODO MOCK"));
    }

    private <T> void validar(TipoDocumento tipo, Class<T> dtoType) {
        String json = gateway.extrairDocumento(Path.of("documento.pdf"), tipo, "prompt");
        T dto = assertDoesNotThrow(() -> objectMapper.readValue(json, dtoType));
        assertNotNull(dto);
    }
}
