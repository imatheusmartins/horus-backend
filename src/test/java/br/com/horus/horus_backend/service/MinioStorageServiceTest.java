package br.com.horus.horus_backend.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MinioStorageServiceTest {

    @Test
    void deveRetornarUrlPublicaQuandoConfigurada() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MinioStorageService service = new MinioStorageService(minioClient);
        ReflectionTestUtils.setField(service, "bucket", "horus");
        ReflectionTestUtils.setField(service, "publicBaseUrl", "https://pub-123.r2.dev");

        MockMultipartFile file = new MockMultipartFile(
                "imagem",
                "raio x.png",
                "image/png",
                "teste".getBytes()
        );

        String url = service.upload(file, "original");

        assertTrue(url.startsWith("https://pub-123.r2.dev/original/"));
        assertTrue(url.endsWith("_raio_x.png"));
    }
}
