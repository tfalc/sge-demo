package br.com.sge.modules.patrimonio.service;

import br.com.sge.modules.patrimonio.dto.AtualizarPatrimonioItemRequest;
import br.com.sge.modules.patrimonio.dto.CriarPatrimonioItemRequest;
import br.com.sge.modules.patrimonio.entity.PatrimonioItem;
import br.com.sge.modules.patrimonio.repository.PatrimonioItemRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatrimonioService {

    private final PatrimonioItemRepository patrimonioItemRepository;

    public PatrimonioService(PatrimonioItemRepository patrimonioItemRepository) {
        this.patrimonioItemRepository = patrimonioItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar() {
        return patrimonioItemRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> criar(CriarPatrimonioItemRequest req) {
        PatrimonioItem item = new PatrimonioItem();
        item.setNome(req.nome().trim());
        item.setCategoria(trimOrNull(req.categoria()));
        item.setLocalizacao(trimOrNull(req.localizacao()));
        item.setNumeroPatrimonio(trimOrNull(req.numeroPatrimonio()));
        item.setDataAquisicao(req.dataAquisicao());
        item.setValorAquisicao(req.valorAquisicao());
        item.setStatus(normalizarStatus(req.status()));
        item.setObservacoes(trimOrNull(req.observacoes()));
        return toMap(patrimonioItemRepository.save(item));
    }

    @Transactional
    public Map<String, Object> atualizar(UUID id, AtualizarPatrimonioItemRequest req) {
        PatrimonioItem item = patrimonioItemRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item nao encontrado"));
        if (req.nome() != null && !req.nome().isBlank()) {
            item.setNome(req.nome().trim());
        }
        if (req.categoria() != null) {
            item.setCategoria(trimOrNull(req.categoria()));
        }
        if (req.localizacao() != null) {
            item.setLocalizacao(trimOrNull(req.localizacao()));
        }
        if (req.numeroPatrimonio() != null) {
            item.setNumeroPatrimonio(trimOrNull(req.numeroPatrimonio()));
        }
        if (req.dataAquisicao() != null) {
            item.setDataAquisicao(req.dataAquisicao());
        }
        if (req.valorAquisicao() != null) {
            item.setValorAquisicao(req.valorAquisicao());
        }
        if (req.status() != null && !req.status().isBlank()) {
            item.setStatus(normalizarStatus(req.status()));
        }
        if (req.observacoes() != null) {
            item.setObservacoes(trimOrNull(req.observacoes()));
        }
        return toMap(patrimonioItemRepository.save(item));
    }

    @Transactional
    public void excluir(UUID id) {
        if (!patrimonioItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Item nao encontrado");
        }
        patrimonioItemRepository.deleteById(id);
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ATIVO";
        }
        return status.trim().toUpperCase();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Map<String, Object> toMap(PatrimonioItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", item.getId());
        m.put("nome", item.getNome());
        m.put("categoria", item.getCategoria());
        m.put("localizacao", item.getLocalizacao());
        m.put("numeroPatrimonio", item.getNumeroPatrimonio());
        m.put("dataAquisicao", item.getDataAquisicao());
        m.put("valorAquisicao", item.getValorAquisicao());
        m.put("status", item.getStatus());
        m.put("observacoes", item.getObservacoes());
        m.put("criadoEm", item.getCriadoEm());
        return m;
    }
}
