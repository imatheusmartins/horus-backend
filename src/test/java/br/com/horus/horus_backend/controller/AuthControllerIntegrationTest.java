package br.com.horus.horus_backend.controller;

import br.com.horus.horus_backend.model.Usuario;
import br.com.horus.horus_backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${local.server.port}")
    private int port;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setNome("Maria");
        usuario.setEmail("maria@horus.com");
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuarioRepository.save(usuario);
    }

    @Test
    void deveRealizarLoginComCampoPassword() throws Exception {
        Map<String, String> payload = Map.of(
                "email", "MARIA@HORUS.COM",
                "password", "123456"
        );

        HttpResponse<String> response = sendPost("/api/auth/login", payload);
        Map<?, ?> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertEquals(200, response.statusCode());
        assertEquals("maria@horus.com", responseBody.get("email"));
        assertEquals("Maria", responseBody.get("nome"));
        assertEquals("token-provisorio", responseBody.get("token"));
    }

    @Test
    void deveRetornar401QuandoSenhaForInvalida() throws Exception {
        Map<String, String> payload = Map.of(
                "email", "maria@horus.com",
                "senha", "errada"
        );

        HttpResponse<String> response = sendPost("/auth/login", payload);
        Map<?, ?> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertEquals(401, response.statusCode());
        assertEquals("Email ou senha inválidos", responseBody.get("message"));
    }

    private HttpResponse<String> sendPost(String path, Map<String, String> payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
