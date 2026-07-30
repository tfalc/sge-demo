package br.com.sge.modules.academico.service;

import br.com.sge.modules.academico.dto.SalvarAtaAulaRequest;
import br.com.sge.modules.academico.entity.AtaAula;
import br.com.sge.modules.academico.entity.TurmaDisciplinaProfessor;
import br.com.sge.modules.academico.repository.AtaAulaRepository;
import br.com.sge.modules.academico.repository.TurmaDisciplinaProfessorRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtaAulaService {

    private final AtaAulaRepository ataAulaRepository;
    private final TurmaDisciplinaProfessorRepository tdpRepository;

    public AtaAulaService(
            AtaAulaRepository ataAulaRepository, TurmaDisciplinaProfessorRepository tdpRepository) {
        this.ataAulaRepository = ataAulaRepository;
        this.tdpRepository = tdpRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterAta(UUID tdpId, LocalDate dataAula) {
        return ataAulaRepository
                .findByTurmaDisciplinaProfessorIdAndDataAula(tdpId, dataAula)
                .map(this::toMap)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarHistorico(UUID tdpId, LocalDate inicio, LocalDate fim) {
        if (!tdpRepository.existsById(tdpId)) {
            throw new IllegalArgumentException("Vinculo turma/disciplina nao encontrado");
        }
        return ataAulaRepository
                .findByTurmaDisciplinaProfessorIdAndDataAulaBetweenOrderByDataAulaDesc(tdpId, inicio, fim)
                .stream()
                .map(this::toResumoMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> salvarAta(SalvarAtaAulaRequest req) {
        TurmaDisciplinaProfessor tdp = tdpRepository
                .findById(req.turmaDisciplinaProfessorId())
                .orElseThrow(() -> new IllegalArgumentException("Vinculo turma/disciplina nao encontrado"));

        AtaAula ata = ataAulaRepository
                .findByTurmaDisciplinaProfessorIdAndDataAula(req.turmaDisciplinaProfessorId(), req.dataAula())
                .orElseGet(() -> {
                    AtaAula nova = new AtaAula();
                    nova.setTurmaDisciplinaProfessor(tdp);
                    nova.setDataAula(req.dataAula());
                    return nova;
                });

        ata.setConteudo(trimOrNull(req.conteudo()));
        ata.setTarefaCasa(trimOrNull(req.tarefaCasa()));
        ata.setObservacoes(trimOrNull(req.observacoes()));
        ata.setAtualizadoEm(Instant.now());

        return toMap(ataAulaRepository.save(ata));
    }

    private Map<String, Object> toMap(AtaAula ata) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ata.getId());
        m.put("turmaDisciplinaProfessorId", ata.getTurmaDisciplinaProfessor().getId());
        m.put("dataAula", ata.getDataAula().toString());
        m.put("conteudo", ata.getConteudo());
        m.put("tarefaCasa", ata.getTarefaCasa());
        m.put("observacoes", ata.getObservacoes());
        m.put("atualizadoEm", ata.getAtualizadoEm().toString());
        return m;
    }

    private Map<String, Object> toResumoMap(AtaAula ata) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ata.getId());
        m.put("dataAula", ata.getDataAula().toString());
        String conteudo = ata.getConteudo();
        if (conteudo != null && conteudo.length() > 120) {
            conteudo = conteudo.substring(0, 117) + "...";
        }
        m.put("conteudoResumo", conteudo);
        m.put("temTarefa", ata.getTarefaCasa() != null && !ata.getTarefaCasa().isBlank());
        m.put("atualizadoEm", ata.getAtualizadoEm().toString());
        return m;
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
