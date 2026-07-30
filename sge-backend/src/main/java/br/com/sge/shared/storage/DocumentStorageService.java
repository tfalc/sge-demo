package br.com.sge.shared.storage;

public interface DocumentStorageService {

    String store(String relativeKey, byte[] content, String contentType);

    byte[] load(String storageKey);

    void delete(String storageKey);
}
