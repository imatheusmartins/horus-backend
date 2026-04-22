package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.auth.*;
import br.com.horus.horus_backend.model.Usuario;
import br.com.horus.horus_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());

        usuarioRepository.save(usuario);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setToken("token-provisorio");

        return response;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getSenha().equals(dto.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        AuthResponseDTO response = new AuthResponseDTO();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setToken("token-provisorio");

        return response;
    }
}