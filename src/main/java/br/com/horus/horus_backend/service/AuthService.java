package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.auth.AuthResponseDTO;
import br.com.horus.horus_backend.dto.auth.LoginRequestDTO;
import br.com.horus.horus_backend.dto.auth.RegisterRequestDTO;
import br.com.horus.horus_backend.exception.AuthException;
import br.com.horus.horus_backend.exception.ConflictException;
import br.com.horus.horus_backend.model.Usuario;
import br.com.horus.horus_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        String normalizedEmail = normalizeEmail(dto.getEmail());

        if (usuarioRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(normalizedEmail);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        usuarioRepository.save(usuario);

        return buildAuthResponse(usuario);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(dto.getEmail()))
                .orElseThrow(() -> new AuthException("Email ou senha inválidos"));

        if (!passwordMatches(dto.getSenha(), usuario)) {
            throw new AuthException("Email ou senha inválidos");
        }

        return buildAuthResponse(usuario);
    }

    private boolean passwordMatches(String rawPassword, Usuario usuario) {
        String storedPassword = usuario.getSenha();

        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }

        if (storedPassword.equals(rawPassword)) {
            usuario.setSenha(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }

    private AuthResponseDTO buildAuthResponse(Usuario usuario) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setToken("token-provisorio");
        return response;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
