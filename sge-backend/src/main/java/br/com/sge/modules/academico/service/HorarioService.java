package br.com.sge.modules.academico.service;

import br.com.sge.modules.academico.dto.AtualizarHorarioRequest;
import br.com.sge.modules.academico.dto.CriarHorarioRequest;
import br.com.sge.modules.academico.entity.Disciplina;
import br.com.sge.modules.academico.entity.HorarioAula;
import br.com.sge.modules.academico.entity.Professor;
import br.com.sge.modules.academico.repository.DisciplinaRepository;
import br.com.sge.modules.academico.repository.HorarioAulaRepository;
import br.com.sge.modules.academico.repository.ProfessorRepository;
import br.com.sge.modules.cadastro.entity.Turma;
import br.com.sge.modules.cadastro.repository.TurmaRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorarioService {

    private final HorarioAulaRepository horarioAulaRepository;
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;

    public HorarioService(
            HorarioAulaRepository horarioAulaRepository,
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            ProfessorRepository professorRepository) {
        this.horarioAulaRepository = horarioAulaRepository;
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorTurma(UUID turmaId) {
        return horarioAulaRepository.findByTurmaId(turmaId).stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorProfessor(UUID professorId) {
        return horarioAulaRepository.findByProfessorId(professorId).stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> criar(CriarHorarioRequest req) {
        validarHorario(req.horaInicio(), req.horaFim());
        HorarioAula horario = new HorarioAula();
        horario.setTurma(loadTurma(req.turmaId()));
        horario.setDiaSemana(req.diaSemana());
        horario.setHoraInicio(req.horaInicio());
        horario.setHoraFim(req.horaFim());
        horario.setDisciplina(loadDisciplina(req.disciplinaId()));
        horario.setProfessor(loadProfessorOpcional(req.professorId()));
        return toMap(horarioAulaRepository.save(horario));
    }

    @Transactional
    public Map<String, Object> atualizar(UUID id, AtualizarHorarioRequest req) {
        validarHorario(req.horaInicio(), req.horaFim());
        HorarioAula horario = horarioAulaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario nao encontrado"));
        horario.setDiaSemana(req.diaSemana());
        horario.setHoraInicio(req.horaInicio());
        horario.setHoraFim(req.horaFim());
        horario.setDisciplina(loadDisciplina(req.disciplinaId()));
        horario.setProfessor(loadProfessorOpcional(req.professorId()));
        return toMap(horarioAulaRepository.save(horario));
    }

    @Transactional
    public void excluir(UUID id) {
        if (!horarioAulaRepository.existsById(id)) {
            throw new IllegalArgumentException("Horario nao encontrado");
        }
        horarioAulaRepository.deleteById(id);
    }

    private void validarHorario(java.time.LocalTime inicio, java.time.LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Hora fim deve ser posterior a hora inicio");
        }
    }

    private Turma loadTurma(UUID id) {
        return turmaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Turma nao encontrada"));
    }

    private Disciplina loadDisciplina(UUID id) {
        return disciplinaRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada"));
    }

    private Professor loadProfessorOpcional(UUID id) {
        if (id == null) {
            return null;
        }
        return professorRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado"));
    }

    private Map<String, Object> toMap(HorarioAula h) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", h.getId());
        map.put("turmaId", h.getTurma().getId());
        map.put("turmaNome", h.getTurma().getNome());
        map.put("diaSemana", h.getDiaSemana());
        map.put("horaInicio", h.getHoraInicio().toString());
        map.put("horaFim", h.getHoraFim().toString());
        map.put("disciplinaId", h.getDisciplina().getId());
        map.put("disciplinaNome", h.getDisciplina().getNome());
        if (h.getProfessor() != null) {
            map.put("professorId", h.getProfessor().getId());
            map.put(
                    "professorNome",
                    h.getProfessor().getPessoa() != null
                            ? h.getProfessor().getPessoa().getNome()
                            : null);
        } else {
            map.put("professorId", null);
            map.put("professorNome", null);
        }
        return map;
    }
}
