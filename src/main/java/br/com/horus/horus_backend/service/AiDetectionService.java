package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.ai.AiPredictionItemDTO;
import br.com.horus.horus_backend.dto.ai.AiPredictionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDetectionService {

    private final WebClient webClient;

    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;

    @Value("${ai.api.predict-path}")
    private String predictPath;

    @Value("${ai.api.timeout-seconds:120}")
    private Long timeoutSeconds;

    public AiPredictionResponseDTO analisar(MultipartFile imagem) {
        String aiUrl = aiApiBaseUrl + predictPath;

        try {
            byte[] imagemBytes = imagem.getBytes();
            String contentType = resolveContentType(imagem);
            log.info(
                    "Iniciando chamada para IA. url={}, arquivo={}, contentType={}, tamanhoBytes={}, timeoutSeconds={}",
                    aiUrl,
                    imagem.getOriginalFilename(),
                    contentType,
                    imagemBytes.length,
                    timeoutSeconds);

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new ByteArrayResource(imagemBytes) {
                        @Override
                        public String getFilename() {
                            return imagem.getOriginalFilename();
                        }
                    })
                    .header(HttpHeaders.CONTENT_TYPE, contentType);

            AiPredictionResponseDTO response = webClient.post()
                    .uri(aiApiBaseUrl + predictPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> buildAiHttpException(clientResponse.statusCode().value(), body)))
                    .bodyToMono(AiPredictionResponseDTO.class)
                    .map(this::enriquecerDescricoes)
                    .block(Duration.ofSeconds(timeoutSeconds));

            log.info(
                    "Analise de IA concluida. task={}, modelSource={}, topPredictionLabel={}, topPredictionConfidence={}",
                    response != null ? response.getTask() : null,
                    response != null ? response.getModelSource() : null,
                    response != null && response.getTopPrediction() != null ? response.getTopPrediction().getLabel() : null,
                    response != null && response.getTopPrediction() != null ? response.getTopPrediction().getConfidence() : null);

            return response;
        } catch (IOException e) {
            log.error("Erro ao ler a imagem enviada para a IA. arquivo={}", imagem.getOriginalFilename(), e);
            throw new RuntimeException("Erro ao ler a imagem enviada para a IA", e);
        } catch (ResponseStatusException e) {
            log.warn("Servico de IA retornou erro tratado. status={}, reason={}", e.getStatusCode().value(), e.getReason(), e);
            throw e;
        } catch (Exception e) {
            log.error(
                    "Falha inesperada ao chamar servico de IA. url={}, arquivo={}, contentType={}, tamanhoBytes={}",
                    aiUrl,
                    imagem.getOriginalFilename(),
                    imagem.getContentType(),
                    imagem.getSize(),
                    e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Nao foi possivel concluir a analise pela IA. Tente novamente em instantes.",
                    e);
        }
    }

    private String resolveContentType(MultipartFile imagem) {
        return imagem.getContentType() != null
                ? imagem.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private AiPredictionResponseDTO enriquecerDescricoes(AiPredictionResponseDTO response) {
        if (response == null) {
            return null;
        }

        preencherDescricao(response.getTopPrediction());

        List<AiPredictionItemDTO> predictions = response.getPredictions();
        if (predictions != null) {
            predictions.forEach(this::preencherDescricao);
        }

        return response;
    }

    private void preencherDescricao(AiPredictionItemDTO prediction) {
        if (prediction == null) {
            return;
        }

        String descricao = descreverGrau(prediction.getLabel());
        prediction.setDescription(descricao);
        prediction.setDescricao(descricao);
    }

    private String descreverGrau(String label) {
        if (label == null) {
            return "Grau nao identificado";
        }

        return switch (label) {
            case "0" -> "Sem retinopatia diabetica";
            case "1" -> "Retinopatia leve";
            case "2" -> "Retinopatia moderada";
            case "3" -> "Retinopatia grave";
            case "4" -> "Retinopatia diabetica proliferativa";
            default -> "Grau nao identificado";
        };
    }

    private HttpStatus resolveHttpStatus(int statusCode) {
        return statusCode == 400
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.BAD_GATEWAY;
    }

    private ResponseStatusException buildAiHttpException(int statusCode, String body) {
        log.warn(
                "Servico de IA retornou HTTP {}. body={}",
                statusCode,
                limitarBody(body));

        return new ResponseStatusException(
                resolveHttpStatus(statusCode),
                resolveErrorMessage(statusCode));
    }

    private String resolveErrorMessage(int statusCode) {
        if (statusCode == 400) {
            return "A imagem enviada nao foi reconhecida como uma imagem valida para analise.";
        }

        return "O servico de IA nao conseguiu processar a analise neste momento.";
    }

    private String limitarBody(String body) {
        if (body == null || body.isBlank()) {
            return "<vazio>";
        }

        return body.length() <= 1000
                ? body
                : body.substring(0, 1000) + "...";
    }
}
