package br.com.horus.horus_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String salvarImagemExame(MultipartFile imagem, Long exameId, String sufixo);

    String salvarBase64Exame(String base64, Long exameId, String sufixo);

    void deletarPorUrl(String url);
}
