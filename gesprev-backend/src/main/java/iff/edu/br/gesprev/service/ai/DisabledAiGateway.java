package iff.edu.br.gesprev.service.ai;

import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;

@Service
@ConditionalOnProperty(prefix = "gesprev.ai", name = "provider", havingValue = "disabled")
public class DisabledAiGateway implements AiGateway {

    private static final String MENSAGEM = "IA desabilitada nesta inicializacao. Configure LLAMA_BASE_URL e use o perfil ai-llama para processar documentos ou usar o assistente.";

    @Override
    public String extrairDocumento(Path arquivo, TipoDocumento tipoDocumento, String prompt) {
        throw new IllegalStateException(MENSAGEM);
    }

    @Override
    public String responder(String instrucaoSistema, String pergunta) {
        throw new IllegalStateException(MENSAGEM);
    }

    @Override
    public String responderComFerramentas(String instrucaoSistema, String pergunta, Object... ferramentas) {
        throw new IllegalStateException(MENSAGEM);
    }

    @Override
    public boolean simulado() {
        return false;
    }

    @Override
    public String provedor() {
        return "disabled";
    }
}
