package br.com.horus.horus_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.exames-dir}")
    private String examesDir;

    @Value("${app.upload.public-path}")
    private String uploadPublicPath;

    public String salvarImagemExame(MultipartFile imagem, Long exameId, String sufixo) {
        validarImagem(imagem);

        try {
            Path diretorio = getDiretorioExames();
            Files.createDirectories(diretorio);

            String extensao = getExtensao(imagem.getOriginalFilename(), imagem.getContentType());
            String nomeArquivo = exameId + "-" + sufixo + extensao;
            Path destino = diretorio.resolve(nomeArquivo).normalize();

            if (!destino.startsWith(diretorio)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo invalido");
            }

            imagem.transferTo(destino);
            return getPublicPath(nomeArquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem do exame", e);
        }
    }

    public void deletarPorUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        String publicPath = normalizePublicPath();
        if (!url.startsWith(publicPath + "/")) {
            return;
        }

        String nomeArquivo = url.substring((publicPath + "/").length());
        try {
            Path diretorio = getDiretorioExames();
            Path arquivo = diretorio.resolve(nomeArquivo).normalize();
            if (arquivo.startsWith(diretorio)) {
                Files.deleteIfExists(arquivo);
            }
        } catch (IOException ignored) {
            // A exclusao do registro nao deve falhar apenas porque o arquivo local ja nao existe.
        }
    }

    public String salvarBase64Exame(String base64, Long exameId, String sufixo) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }

        try {
            Path diretorio = getDiretorioExames();
            Files.createDirectories(diretorio);

            Base64Image base64Image = parseBase64Image(base64);
            String nomeArquivo = exameId + "-" + sufixo + base64Image.extensao();
            Path destino = diretorio.resolve(nomeArquivo).normalize();

            if (!destino.startsWith(diretorio)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo invalido");
            }

            Files.write(destino, base64Image.bytes());
            return getPublicPath(nomeArquivo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Servico de IA retornou imagem anotada invalida", e);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem anotada do exame", e);
        }
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

    private Path getDiretorioExames() {
        return Paths.get(examesDir).toAbsolutePath().normalize();
    }

    private String getPublicPath(String nomeArquivo) {
        return normalizePublicPath() + "/" + nomeArquivo;
    }

    private String normalizePublicPath() {
        return uploadPublicPath.endsWith("/")
                ? uploadPublicPath.substring(0, uploadPublicPath.length() - 1)
                : uploadPublicPath;
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

        if (base64.startsWith("data:")) {
            int commaIndex = base64.indexOf(',');
            if (commaIndex < 0) {
                throw new IllegalArgumentException("Data URL invalida");
            }

            String metadata = base64.substring(0, commaIndex);
            payload = base64.substring(commaIndex + 1);
            if (metadata.contains("image/jpeg")) {
                extensao = ".jpg";
            } else if (metadata.contains("image/webp")) {
                extensao = ".webp";
            }
        }

        return new Base64Image(Base64.getDecoder().decode(payload), extensao);
    }

    private record Base64Image(byte[] bytes, String extensao) {
    }
}
