package br.com.sge.shared.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FilesystemDocumentStorageService implements DocumentStorageService {

    private final Path basePath;

    public FilesystemDocumentStorageService(
            @Value("${app.storage.filesystem.path:./data/docs}") String basePathStr) {
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
    }

    @Override
    public String store(String relativeKey, byte[] content, String contentType) {
        try {
            Path target = resolve(relativeKey);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return relativeKey;
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao armazenar documento", ex);
        }
    }

    @Override
    public byte[] load(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Documento nao encontrado");
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao remover documento", ex);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = basePath.resolve(storageKey).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Chave de armazenamento invalida");
        }
        return resolved;
    }
}
