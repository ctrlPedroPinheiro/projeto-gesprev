package iff.edu.br.gesprev.service.ai;

import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;

@Service
@ConditionalOnProperty(prefix = "gesprev.ai", name = "provider", havingValue = "mock")
public class MockAiGateway implements AiGateway {

    @Override
    public String extrairDocumento(Path arquivo, TipoDocumento tipoDocumento, String prompt) {
        return switch (tipoDocumento) {
            case FICHA_FUNCIONAL -> """
                    {"matricula":"MOCK-001","nome":"Servidor Simulado","dtNascimento":"1980-01-15",
                    "cpf":"","sexo":"MASCULINO","email":"servidor.mock@example.com","cargo":"Analista",
                    "orgao":"Orgao Municipal","dtAdmissao":"2005-03-01"}
                    """;
            case HOLERITE -> """
                    {"mesReferencia":"01/2026","proventos":[{"id":null,"descricao":"Vencimento base",
                    "referencia":"1.0","valor":5000.00,"vencimento":true}],"totalVencimentos":5000.00}
                    """;
            case FICHA_FINANCEIRA -> """
                    {"folhas":[{"anoReferencia":2026,"competencia":"01/2026","vencimentos":5000.00,
                    "descontos":750.00,"liquido":4250.00}]}
                    """;
            case CTS -> """
                    {"inicioContribuicao":"01/03/2005","fimContribuicao":"31/12/2025",
                    "tempoAverbacao":0,"totalBruto":7609,"faltas":0,"totalDias":7609,
                    "tempoLegivel":"20 anos e 10 meses"}
                    """;
        };
    }

    @Override
    public String responder(String instrucaoSistema, String pergunta) {
        throw new IllegalStateException("O modo mock de chat foi desativado. Configure AI_PROVIDER=openai-compatible e LLAMA_BASE_URL para usar o assistente.");
    }

    @Override
    public String responderComFerramentas(String instrucaoSistema, String pergunta, Object... ferramentas) {
        return responder(instrucaoSistema, pergunta);
    }

    @Override
    public boolean simulado() {
        return true;
    }

    @Override
    public String provedor() {
        return "mock";
    }
}
