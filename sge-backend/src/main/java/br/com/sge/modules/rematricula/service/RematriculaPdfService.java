package br.com.sge.modules.rematricula.service;

import br.com.sge.modules.rematricula.dto.CampoFormularioDto;
import br.com.sge.modules.rematricula.dto.FormularioRematriculaDto;
import br.com.sge.modules.rematricula.dto.SecaoFormularioDto;
import br.com.sge.modules.rematricula.entity.RematriculaSubmissao;
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
import org.springframework.stereotype.Service;

@Service
public class RematriculaPdfService {

    private final RematriculaFormularioMapper formularioMapper;

    public RematriculaPdfService(RematriculaFormularioMapper formularioMapper) {
        this.formularioMapper = formularioMapper;
    }

    public byte[] gerarPdfPreenchido(
            String tituloFormulario,
            RematriculaSubmissao submissao,
            FormularioRematriculaDto formulario) {
        Map<String, Object> respostas = formularioMapper.parseRespostas(submissao.getRespostasJson());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph(tituloFormulario, title));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Aluno: " + submissao.getAluno().getPessoa().getNome(), body));
            doc.add(new Paragraph("Matricula: " + submissao.getAluno().getMatricula(), body));
            doc.add(new Paragraph(" "));

            for (SecaoFormularioDto secao : formulario.secoes()) {
                doc.add(new Paragraph(secao.titulo(), section));
                for (CampoFormularioDto campo : secao.campos()) {
                    Object valor = respostas.get(campo.id());
                    doc.add(new Paragraph("  " + campo.rotulo() + ": " + formatarValor(campo, valor), body));
                }
                doc.add(new Paragraph(" "));
            }

            doc.add(new Paragraph(
                    "Documento gerado pelo SGE. Assinatura digital gov.br: fase futura.", body));

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF da rematricula", e);
        }
    }

    private String formatarValor(CampoFormularioDto campo, Object valor) {
        if (valor == null || String.valueOf(valor).isBlank()) {
            return "-";
        }
        if ("BOOLEAN".equals(campo.tipo())) {
            return Boolean.TRUE.equals(valor) || "true".equalsIgnoreCase(String.valueOf(valor)) ? "Sim" : "Nao";
        }
        return String.valueOf(valor);
    }
}
