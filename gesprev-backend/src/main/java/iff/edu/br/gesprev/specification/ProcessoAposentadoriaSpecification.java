package iff.edu.br.gesprev.specification;

import org.springframework.data.jpa.domain.Specification;
import iff.edu.br.gesprev.entity.ProcessoAposentadoria;
import iff.edu.br.gesprev.entity.enums.StatusProcesso;
import java.time.LocalDateTime;

public class ProcessoAposentadoriaSpecification {

    public static Specification<ProcessoAposentadoria> comFiltros(
            Integer numeroProcesso,
            StatusProcesso status,
            LocalDateTime dtCriacaoInicio,
            LocalDateTime dtCriacaoFim,
            String nomeServidor,
            String cpfServidor) {

        return Specification
            .where(porNumeroProcesso(numeroProcesso))
            .and(porStatus(status))
            .and(porDtCriacao(dtCriacaoInicio, dtCriacaoFim))
            .and(porNomeServidor(nomeServidor))
            .and(porCpfServidor(cpfServidor));
    }

    private static Specification<ProcessoAposentadoria> porNumeroProcesso(Integer numeroProcesso) {
        return (root, query, cb) -> numeroProcesso == null ? null
                : cb.equal(root.get("numeroProcesso"), numeroProcesso);
    }

    private static Specification<ProcessoAposentadoria> porStatus(StatusProcesso status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    private static Specification<ProcessoAposentadoria> porDtCriacao(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) -> {
            if (inicio == null && fim == null) return null;
            if (inicio == null) return cb.lessThanOrEqualTo(root.get("dtCriacao"), fim);
            if (fim == null) return cb.greaterThanOrEqualTo(root.get("dtCriacao"), inicio);
            return cb.between(root.get("dtCriacao"), inicio, fim);
        };
    }

    private static Specification<ProcessoAposentadoria> porNomeServidor(String nomeServidor) {
        return (root, query, cb) -> nomeServidor == null ? null
                : cb.like(cb.lower(root.get("servidor").get("nome")),
                          "%" + nomeServidor.toLowerCase() + "%");
    }

    private static Specification<ProcessoAposentadoria> porCpfServidor(String cpfServidor) {
        return (root, query, cb) -> cpfServidor == null ? null
                : cb.equal(root.get("servidor").get("cpf"), cpfServidor);
    }
}