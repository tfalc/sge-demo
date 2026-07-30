package br.com.sge.modules.rematricula.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class RematriculaPdfImportService {

    public List<String> extrairSugestoes(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return List.of();
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);
            Set<String> linhas = new LinkedHashSet<>();
            for (String linha : texto.split("\\R")) {
                String limpa = linha.trim();
                if (limpa.length() >= 4 && limpa.length() <= 200 && !isLinhaRuido(limpa)) {
                    linhas.add(limpa);
                }
            }
            return new ArrayList<>(linhas).stream().limit(80).toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Nao foi possivel ler o PDF: " + e.getMessage());
        }
    }

    private boolean isLinhaRuido(String linha) {
        if (linha.matches("^\\d+$")) {
            return true;
        }
        if (linha.matches("(?i)^(pagina|page)\\s*\\d+.*")) {
            return true;
        }
        return linha.chars().filter(ch -> Character.isLetter(ch)).count() < 3;
    }
}
