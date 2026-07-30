package br.com.sge.modules.relatorios.service;

import br.com.sge.modules.academico.service.AcademicoService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BoletimPdfService {

    private final AcademicoService academicoService;

    public BoletimPdfService(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    public byte[] gerarPdf(UUID alunoId) {
        Map<String, Object> boletim = academicoService.obterBoletim(alunoId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph("BOLETIM ESCOLAR — SGE", title));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Aluno: " + boletim.get("alunoNome"), body));
            doc.add(new Paragraph("Turma: " + boletim.get("turmaNome"), body));
            doc.add(new Paragraph(" "));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> periodos = (List<Map<String, Object>>) boletim.get("periodos");
            for (Map<String, Object> periodo : periodos) {
                doc.add(new Paragraph(String.valueOf(periodo.get("periodoNome")), title));
                doc.add(new Paragraph(
                        "Media geral: " + periodo.get("mediaGeral") + " — "
                                + (Boolean.TRUE.equals(periodo.get("aprovado")) ? "Aprovado" : "Atencao"),
                        body));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) periodo.get("disciplinas");
                for (Map<String, Object> d : disciplinas) {
                    doc.add(new Paragraph(
                            "  • " + d.get("disciplinaNome") + ": media " + d.get("media"), body));
                }
                doc.add(new Paragraph(" "));
            }

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF do boletim", e);
        }
    }
}
