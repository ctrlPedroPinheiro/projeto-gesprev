package iff.edu.br.gesprev.service.ai;

import java.nio.file.Path;

import iff.edu.br.gesprev.entity.enums.TipoDocumento;

public interface AiGateway {

    String extrairDocumento(Path arquivo, TipoDocumento tipoDocumento, String prompt);

    String responder(String instrucaoSistema, String pergunta);

    String responderComFerramentas(String instrucaoSistema, String pergunta, Object... ferramentas);

    boolean simulado();

    String provedor();
}
