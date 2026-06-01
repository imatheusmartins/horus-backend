package br.com.horus.horus_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "supabase")
public class SupabaseStorageService implements FileStorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.public:true}")
    private boolean publicBucket;

    @Override
    public String salvarImagemExame(MultipartFile imagem, Long exameId, String sufixo) {
        validarImagem(imagem);

        try {
            String extensao = getExtensao(imagem.getOriginalFilename(), imagem.getContentType());
            String path = "exames/" + exameId + "/" + sufixo + extensao;
            upload(path, imagem.getBytes(), resolveContentType(imagem.getContentType()));
            return getPublicUrl(path);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler imagem para upload no Supabase", e);
        }
    }

    @Override
    public String salvarBase64Exame(String base64, Long exameId, String sufixo) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }

        Base64Image base64Image = parseBase64Image(base64);
        String path = "exames/" + exameId + "/" + sufixo + base64Image.extensao();
        upload(path, base64Image.bytes(), base64Image.contentType());
        return getPublicUrl(path);
    }

    @Override
    public void deletarPorUrl(String url) {
        String path = extractPathFromPublicUrl(url);
        if (path == null) {
            return;
        }

        webClient.method(HttpMethod.DELETE)
                .uri(getStorageBaseUrl() + "/object/" + bucket)
                .headers(this::addAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("prefixes", List.of(path)))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(error -> reactor.core.publisher.Mono.empty())
                .block();
    }

    private void upload(String path, byte[] bytes, MediaType contentType) {
        webClient.post()
                .uri(getStorageBaseUrl() + "/object/" + bucket + "/" + path)
                .headers(this::addAuthHeaders)
                .header("x-upsert", "true")
                .contentType(contentType)
                .bodyValue(new ByteArrayResource(bytes))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Sem detalhes")
                                .map(body -> new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Erro ao enviar imagem para o Supabase Storage: " + body)))
                .bodyToMono(Void.class)
                .block();
    }

    private void addAuthHeaders(HttpHeaders headers) {
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
    }

    private void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem do exame e obrigatoria");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo enviado deve ser uma imagem");
        }
    }

    private String getPublicUrl(String path) {
        if (!publicBucket) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bucket privado ainda nao esta configurado para gerar URL assinada");
        }

        return normalizeSupabaseUrl() + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    private String extractPathFromPublicUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String prefix = normalizeSupabaseUrl() + "/storage/v1/object/public/" + bucket + "/";
        if (!url.startsWith(prefix)) {
            return null;
        }

        return url.substring(prefix.length());
    }

    private String getStorageBaseUrl() {
        return normalizeSupabaseUrl() + "/storage/v1";
    }

    private String normalizeSupabaseUrl() {
        return supabaseUrl.endsWith("/")
                ? supabaseUrl.substring(0, supabaseUrl.length() - 1)
                : supabaseUrl;
    }

    private MediaType resolveContentType(String contentType) {
        return contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
    }

    private String getExtensao(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                String extensao = originalFilename.substring(dotIndex).toLowerCase();
                if (extensao.matches("\\.[a-z0-9]{1,10}")) {
                    return extensao;
                }
            }
        }

        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".img";
    }

    private Base64Image parseBase64Image(String base64) {
        String payload = base64;
        String extensao = ".png";
        MediaType contentType = MediaType.IMAGE_PNG;

        if (base64.startsWith("data:")) {
            int commaIndex = base64.indexOf(',');
            if (commaIndex < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Data URL de imagem anotada invalida");
            }

            String metadata = base64.substring(0, commaIndex);
            payload = base64.substring(commaIndex + 1);
            if (metadata.contains("image/jpeg")) {
                extensao = ".jpg";
                contentType = MediaType.IMAGE_JPEG;
            } else if (metadata.contains("image/webp")) {
                extensao = ".webp";
                contentType = MediaType.parseMediaType("image/webp");
            }
        }

        try {
            return new Base64Image(Base64.getDecoder().decode(payload), extensao, contentType);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Imagem anotada retornada pela IA e invalida", e);
        }
    }

    private record Base64Image(byte[] bytes, String extensao, MediaType contentType) {
    }
}
