package iff.edu.br.gesprev.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iff.edu.br.gesprev.entity.FatorAtualizacao;
import iff.edu.br.gesprev.repository.FatorAtualizacaoRepository;

@Service
public class FatorAtualizacaoService {

    private final FatorAtualizacaoRepository fatorAtualizacaoRepository;

    public FatorAtualizacaoService(FatorAtualizacaoRepository fatorAtualizacaoRepository) {
        this.fatorAtualizacaoRepository = fatorAtualizacaoRepository;
    }

    public void importarPlanilha(MultipartFile arquivo, String portaria) throws Exception {
        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<FatorAtualizacao> fatores = new ArrayList<>();

            for (Row row : sheet) {
                Cell cellMes = null;
                Cell cellFator = null;

                // A planilha oficial pode começar em A/B ou B/C. Localiza o
                // primeiro par adjacente formado por uma data e um número.
                for (int coluna = row.getFirstCellNum(); coluna < row.getLastCellNum() - 1; coluna++) {
                    Cell candidataMes = row.getCell(coluna);
                    Cell candidataFator = row.getCell(coluna + 1);

                    if (candidataMes == null || candidataFator == null) continue;
                    if (candidataMes.getCellType() != CellType.NUMERIC) continue;
                    if (!DateUtil.isCellDateFormatted(candidataMes)) continue;
                    if (candidataFator.getCellType() != CellType.NUMERIC) continue;

                    cellMes = candidataMes;
                    cellFator = candidataFator;
                    break;
                }

                if (cellMes == null) continue;

                // Converte serial do Excel para LocalDate
                LocalDate mesReferencia = cellMes.getLocalDateTimeCellValue().toLocalDate();
                BigDecimal fator = BigDecimal.valueOf(cellFator.getNumericCellValue());

                // Usa o primeiro dia do mês como referência
                LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);

                FatorAtualizacao existente = fatorAtualizacaoRepository
                        .findByMesReferencia(primeiroDia).orElse(null);

                if (existente != null) {
                    existente.setFator(fator);
                    fatores.add(existente);
                } else {
                    fatores.add(new FatorAtualizacao(primeiroDia, fator));
                }
            }

            if (fatores.isEmpty()) {
                throw new IllegalArgumentException(
                        "Nenhum fator válido foi encontrado na planilha. Verifique as colunas de mês e fator.");
            }

            fatorAtualizacaoRepository.saveAll(fatores);
        }
    }

    public long contarFatores() {
        return fatorAtualizacaoRepository.count();
    }

    public BigDecimal obterFator(LocalDate mesReferencia) {
        LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
        return fatorAtualizacaoRepository.findByMesReferencia(primeiroDia)
                .map(FatorAtualizacao::getFator)
                .orElse(BigDecimal.ONE); // se não encontrar, fator 1 (sem correção)
    }
}
