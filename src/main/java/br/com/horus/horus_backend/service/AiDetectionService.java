package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.ai.AiPredictionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AiDetectionService {

    private final WebClient webClient;

    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;

    @Value("${ai.api.predict-path}")
    private String predictPath;

    public AiPredictionResponseDTO analisar(MultipartFile imagem) {
        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new ByteArrayResource(imagem.getBytes()) {
                        @Override
                        public String getFilename() {
                            return imagem.getOriginalFilename();
                        }
                    })
                    .header(HttpHeaders.CONTENT_TYPE, resolveContentType(imagem));

            return webClient.post()
                    .uri(aiApiBaseUrl + predictPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(AiPredictionResponseDTO.class)
                    .block();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler a imagem enviada para a IA", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar servico de IA", e);
        }
    }

    private String resolveContentType(MultipartFile imagem) {
        return imagem.getContentType() != null
                ? imagem.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
