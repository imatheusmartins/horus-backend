package br.com.horus.horus_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiDetectionService {

    private final WebClient webClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public String analisar(MultipartFile imagem) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imagem.getBytes()) {
                @Override
                public String getFilename() {
                    return imagem.getOriginalFilename();
                }
            });

            return webClient.post()
                    .uri(aiServiceUrl + "/analisar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar serviço de IA: " + e.getMessage());
        }
    }
}