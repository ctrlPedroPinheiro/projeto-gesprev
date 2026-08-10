package iff.edu.br.gesprev.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import iff.edu.br.gesprev.entity.enums.StatusVLM;
import iff.edu.br.gesprev.entity.enums.TipoDocumento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Representa a ficha financeira de um processo de aposentadoria, contendo informações sobre as folhas de pagamento e o ano de referência.
 */
@Entity
@Table(name = "ficha_financeira")
public class FichaFinanceira extends Documento {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "folha_id")
    private List<Folha> folhas;

    public FichaFinanceira() {
        super();
        this.folhas = new ArrayList<>();
    }

    public FichaFinanceira(Long id, String nomeArquivo, String caminhoArquivo, TipoDocumento tipoDocumento, StatusVLM statusVLM, LocalDateTime dtUpload, ProcessoAposentadoria processo, String nomeOriginal, String jsonExtraido, List<Folha> folhas) {
        super(id, nomeArquivo, caminhoArquivo, tipoDocumento, statusVLM, dtUpload, processo, nomeOriginal, jsonExtraido);
        this.folhas = folhas;
    }

    public List<Folha> getFolhas() {
        return folhas;
    }

    public void setFolhas(List<Folha> folhas) {
        this.folhas = folhas;
    }
}
