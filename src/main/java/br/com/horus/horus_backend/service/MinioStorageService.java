package br.com.horus.horus_backend.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.public-base-url:}")
    private String publicBaseUrl;

    public String upload(MultipartFile file, String pasta) {
        try {
            String nomeArquivo = pasta + "/" + UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(nomeArquivo)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
                return buildPublicUrl(nomeArquivo);
            }

            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(nomeArquivo)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }

    private String buildPublicUrl(String objectName) {
        String normalizedBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        return UriComponentsBuilder.fromUriString(normalizedBaseUrl)
                .pathSegment(objectName.split("/"))
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "arquivo";
        }

        String normalized = Normalizer.normalize(originalFileName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .replaceAll("[\\\\/]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
