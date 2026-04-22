package br.com.horus.horus_backend.service;

import br.com.horus.horus_backend.dto.paciente.*;
import br.com.horus.horus_backend.model.Paciente;
import br.com.horus.horus_backend.model.Usuario;
import br.com.horus.horus_backend.repository.PacienteRepository;
import br.com.horus.horus_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public PacienteResponseDTO criar(PacienteRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Paciente paciente = new Paciente();
        paciente.setNome(dto.getNome());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setCpf(dto.getCpf());
        paciente.setUsuario(usuario);

        pacienteRepository.save(paciente);
        return toResponse(paciente);
    }

    public List<PacienteResponseDTO> listarPorUsuario(Long usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PacienteResponseDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        return toResponse(paciente);
    }

    public PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        paciente.setNome(dto.getNome());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setCpf(dto.getCpf());

        pacienteRepository.save(paciente);
        return toResponse(paciente);
    }

    public void deletar(Long id) {
        pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        pacienteRepository.deleteById(id);
    }

    private PacienteResponseDTO toResponse(Paciente paciente) {
        PacienteResponseDTO response = new PacienteResponseDTO();
        response.setId(paciente.getId());
        response.setNome(paciente.getNome());
        response.setDataNascimento(paciente.getDataNascimento());
        response.setCpf(paciente.getCpf());
        return response;
    }
}